package de.sasd.mustelalog.client.ui;

import de.sasd.mustelalog.client.api.ApiClientException;
import de.sasd.mustelalog.client.api.MustelaLogApiClient;
import de.sasd.mustelalog.client.config.AppSettings;
import de.sasd.mustelalog.client.json.SimpleJson;
import de.sasd.mustelalog.client.logging.ClientLogger;
import de.sasd.mustelalog.client.model.EventListPage;
import de.sasd.mustelalog.client.model.EventQueryFilter;
import de.sasd.mustelalog.client.model.HealthStatus;
import de.sasd.mustelalog.client.model.LogEventRecord;
import de.sasd.mustelalog.client.model.SeveritySummary;
import de.sasd.mustelalog.client.model.SourceSummary;
import de.sasd.mustelalog.client.model.TestLogEventRequest;
import de.sasd.mustelalog.client.service.EventAggregationService;
import de.sasd.mustelalog.client.service.EventExportService;
import de.sasd.mustelalog.client.service.TimeService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Main Swing window.
 *
 * The frame deliberately keeps networking and UI wiring readable instead of hiding everything
 * behind additional abstractions. That makes the client easier to review and easier to align
 * with the server contract when the API evolves.
 */
public final class MainFrame extends JFrame {
    private final AppSettings settings;
    private final MustelaLogApiClient apiClient;
    private final ClientLogger logger;
    private final TimeService timeService;
    private final EventAggregationService aggregationService;
    private final EventExportService exportService;

    private final DiagnosticsWindow diagnosticsWindow;

    private final EventTableModel eventTableModel = new EventTableModel();
    private final JTable eventTable = new JTable(eventTableModel);
    private final SourceTableModel sourceTableModel = new SourceTableModel();
    private final JTable sourceTable = new JTable(sourceTableModel);
    private final DefaultTableModel severitySummaryModel = new DefaultTableModel(new Object[]{"Severity", "Count"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable severityTable = new JTable(severitySummaryModel);

    private final JTextArea eventDetailArea = createReadOnlyArea();
    private final JTextArea relatedEventsArea = createReadOnlyArea();
    private final JTextArea sourceDetailArea = createReadOnlyArea();
    private final JTextArea ingestPayloadPreview = createReadOnlyArea();

    private final JLabel statusLabel = new JLabel("Ready.");

    private final JTextField sourceKeyField = new JTextField(12);
    private final JTextField severityField = new JTextField(10);
    private final JTextField traceIdField = new JTextField(14);
    private final JTextField correlationIdField = new JTextField(14);
    private final JTextField fromField = new JTextField(14);
    private final JTextField toField = new JTextField(14);
    private final JTextField pageField = new JTextField("1", 4);
    private final JTextField pageSizeField = new JTextField(6);
    private final JComboBox<String> sortFieldCombo = new JComboBox<>(new String[]{"occurredAt", "severityNumber", "sourceKey"});
    private final JComboBox<String> directionCombo = new JComboBox<>(new String[]{"DESC", "ASC"});

    private final JTextField ingestOccurredAtField = new JTextField(24);
    private final JTextField ingestObservedAtField = new JTextField(24);
    private final JTextField ingestSeverityTextField = new JTextField("INFO", 12);
    private final JTextField ingestSeverityNumberField = new JTextField("9", 6);
    private final JTextField ingestEventNameField = new JTextField("client.test", 16);
    private final JTextField ingestEventCategoryField = new JTextField("diagnostics", 16);
    private final JTextField ingestEventActionField = new JTextField("manual-test", 16);
    private final JTextField ingestCorrelationIdField = new JTextField(18);
    private final JTextField ingestTraceIdField = new JTextField(18);
    private final JTextField ingestHostField = new JTextField(18);
    private final JTextField ingestServiceField = new JTextField(18);
    private final JTextArea ingestMessageArea = new JTextArea(4, 50);
    private final JTextArea ingestAttributesArea = new JTextArea(8, 50);

    private List<LogEventRecord> currentEvents = new ArrayList<>();
    private List<SourceSummary> currentSources = new ArrayList<>();
    private EventListPage lastPage = new EventListPage(List.of(), 0, 1, 0);

    public MainFrame(AppSettings settings,
                     MustelaLogApiClient apiClient,
                     ClientLogger logger,
                     TimeService timeService,
                     EventAggregationService aggregationService,
                     EventExportService exportService) {
        super("Mustralla LogAPI - Java Client");
        this.settings = settings;
        this.apiClient = apiClient;
        this.logger = logger;
        this.timeService = timeService;
        this.aggregationService = aggregationService;
        this.exportService = exportService;
        this.diagnosticsWindow = new DiagnosticsWindow(this, logger);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(settings.getUi().getWindowWidth(), settings.getUi().getWindowHeight());
        setMinimumSize(new Dimension(1200, 780));
        setLocationRelativeTo(null);
        pageSizeField.setText(String.valueOf(settings.getApi().getDefaultPageSize()));
        ingestOccurredAtField.setText(timeService.currentUtcTimestamp());
        ingestObservedAtField.setText(timeService.currentUtcTimestamp());
        ingestMessageArea.setText("Client initiated integration test.");
        ingestAttributesArea.setText("{\n  \"clientName\": \"mustralla-java-client\",\n  \"purpose\": \"manual-test\"\n}");

        buildUi();
        wireSelectionBehaviour();
        refreshIngestPreview();
    }

    public void startup() {
        if (settings.getApi().isHealthOnStartup()) {
            runHealthCheck();
        }
        if (settings.getUi().isAutoLoadSourcesOnStartup()) {
            loadSources();
        }
        if (settings.getUi().isAutoLoadEventsOnStartup()) {
            loadEvents();
        }
    }

    private void buildUi() {
        setLayout(new BorderLayout(8, 8));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton healthButton = new JButton("Health");
        healthButton.addActionListener(event -> runHealthCheck());
        JButton eventsButton = new JButton("Load Events");
        eventsButton.addActionListener(event -> loadEvents());
        JButton relatedButton = new JButton("Load Related");
        relatedButton.addActionListener(event -> loadRelatedEvents());
        JButton sourcesButton = new JButton("Load Sources");
        sourcesButton.addActionListener(event -> loadSources());
        JButton diagnosticsButton = new JButton("Diagnostics");
        diagnosticsButton.addActionListener(event -> diagnosticsWindow.setVisible(true));
        JButton exportCsvButton = new JButton("Export CSV");
        exportCsvButton.addActionListener(event -> exportCurrentEvents("csv"));
        JButton exportJsonButton = new JButton("Export JSON");
        exportJsonButton.addActionListener(event -> exportCurrentEvents("json"));
        JButton sendTestButton = new JButton("Send Test Event");
        sendTestButton.addActionListener(event -> sendTestEvent());

        panel.add(healthButton);
        panel.add(eventsButton);
        panel.add(relatedButton);
        panel.add(sourcesButton);
        panel.add(sendTestButton);
        panel.add(exportCsvButton);
        panel.add(exportJsonButton);
        panel.add(diagnosticsButton);
        return panel;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Events", buildEventsTab());
        tabs.addTab("Sources", buildSourcesTab());
        tabs.addTab("Write Test Event", buildIngestTab());
        tabs.addTab("Aggregations", buildAggregationsTab());
        return tabs;
    }

    private JPanel buildEventsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(buildEventFilterPanel(), BorderLayout.NORTH);

        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.setAutoCreateRowSorter(true);
        if (eventTable.getRowSorter() instanceof TableRowSorter<?> sorter) {
            sorter.setSortsOnUpdates(false);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(eventTable),
                buildEventDetailPanel());
        splitPane.setResizeWeight(0.58);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildEventFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addLabeledField(panel, c, row++, "Source Key", sourceKeyField, "Severity", severityField, "Trace ID", traceIdField);
        addLabeledField(panel, c, row++, "Correlation ID", correlationIdField, "From", fromField, "To", toField);
        addLabeledField(panel, c, row++, "Page", pageField, "Page Size", pageSizeField, "Sort", sortFieldCombo);

        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Direction"), c);
        c.gridx = 1;
        panel.add(directionCombo, c);

        JButton clearButton = new JButton("Clear Filters");
        clearButton.addActionListener(event -> clearEventFilters());
        JButton loadButton = new JButton("Reload Events");
        loadButton.addActionListener(event -> loadEvents());
        c.gridx = 2;
        panel.add(clearButton, c);
        c.gridx = 3;
        panel.add(loadButton, c);

        panel.setBorder(BorderFactory.createTitledBorder("Server-supported event filters"));
        return panel;
    }

    private JPanel buildEventDetailPanel() {
        JPanel panel = new JPanel(new GridLayoutBuilder().rows(1).cols(2).build());
        JPanel detailPanel = new JPanel(new BorderLayout(4, 4));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Selected event"));
        detailPanel.add(new JScrollPane(eventDetailArea), BorderLayout.CENTER);

        JPanel relatedPanel = new JPanel(new BorderLayout(4, 4));
        relatedPanel.setBorder(BorderFactory.createTitledBorder("Related events (same correlationId or traceId)"));
        relatedPanel.add(new JScrollPane(relatedEventsArea), BorderLayout.CENTER);

        panel.add(detailPanel);
        panel.add(relatedPanel);
        return panel;
    }

    private JPanel buildSourcesTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        sourceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sourceTable.setAutoCreateRowSorter(true);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(sourceTable),
                buildTextPanel("Selected source", sourceDetailArea));
        splitPane.setResizeWeight(0.62);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildIngestTab() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Payload fields aligned to POST /api/v1/ingest/events"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addLabeledField(form, c, row++, "Occurred At", ingestOccurredAtField, "Observed At", ingestObservedAtField, "Severity Text", ingestSeverityTextField);
        addLabeledField(form, c, row++, "Severity Number", ingestSeverityNumberField, "Event Name", ingestEventNameField, "Category", ingestEventCategoryField);
        addLabeledField(form, c, row++, "Action", ingestEventActionField, "Correlation ID", ingestCorrelationIdField, "Trace ID", ingestTraceIdField);
        addLabeledField(form, c, row++, "Host", ingestHostField, "Service", ingestServiceField, "", new JLabel(""));

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        form.add(new JLabel("Message"), c);
        c.gridx = 1;
        c.gridwidth = 5;
        JScrollPane messageScroll = new JScrollPane(ingestMessageArea);
        messageScroll.setPreferredSize(new Dimension(200, 90));
        form.add(messageScroll, c);
        row++;

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        form.add(new JLabel("Attributes JSON"), c);
        c.gridx = 1;
        c.gridwidth = 5;
        JScrollPane attributesScroll = new JScrollPane(ingestAttributesArea);
        attributesScroll.setPreferredSize(new Dimension(200, 150));
        form.add(attributesScroll, c);
        row++;

        JButton previewButton = new JButton("Refresh Preview");
        previewButton.addActionListener(event -> refreshIngestPreview());
        JButton sendButton = new JButton("Send Test Event");
        sendButton.addActionListener(event -> sendTestEvent());
        JButton nowButton = new JButton("Use current UTC timestamps");
        nowButton.addActionListener(event -> {
            ingestOccurredAtField.setText(timeService.currentUtcTimestamp());
            ingestObservedAtField.setText(timeService.currentUtcTimestamp());
            refreshIngestPreview();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(previewButton);
        buttons.add(nowButton);
        buttons.add(sendButton);

        JPanel previewPanel = buildTextPanel("Preview of JSON body sent to the server", ingestPayloadPreview);
        wrapper.add(form, BorderLayout.NORTH);
        wrapper.add(buttons, BorderLayout.CENTER);
        wrapper.add(previewPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildAggregationsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        severityTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(severityTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(statusLabel);
        return panel;
    }

    private void wireSelectionBehaviour() {
        eventTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int viewRow = eventTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = eventTable.convertRowIndexToModel(viewRow);
                    LogEventRecord record = eventTableModel.getRow(modelRow);
                    renderEventDetail(record);
                }
            }
        });
        sourceTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int viewRow = sourceTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = sourceTable.convertRowIndexToModel(viewRow);
                    renderSourceDetail(sourceTableModel.getRow(modelRow));
                }
            }
        });

        ingestMessageArea.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestAttributesArea.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestOccurredAtField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestObservedAtField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestSeverityTextField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestSeverityNumberField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestEventNameField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestEventCategoryField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestEventActionField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestCorrelationIdField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestTraceIdField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestHostField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
        ingestServiceField.getDocument().addDocumentListener(SimpleDocumentListener.of(this::refreshIngestPreview));
    }

    private void runHealthCheck() {
        executeBackground("Health", new BackgroundJob<HealthStatus>() {
            @Override
            public HealthStatus run() throws Exception {
                return apiClient.getHealth();
            }

            @Override
            public void apply(HealthStatus result) {
                setStatus("Health: ok=" + result.ok() + ", service=" + result.service() + ", version=" + result.version() + ", requestId=" + result.requestId());
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Health check succeeded.\nService: " + result.service() + "\nVersion: " + result.version() + "\nTimestamp: " + result.timestamp() + "\nRequestId: " + result.requestId(),
                        "Health", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void loadEvents() {
        EventQueryFilter filter = collectEventFilter();
        int page = parseInteger(pageField.getText(), 1);
        int pageSize = parseInteger(pageSizeField.getText(), settings.getApi().getDefaultPageSize());
        String sortField = String.valueOf(sortFieldCombo.getSelectedItem());
        boolean ascending = "ASC".equals(String.valueOf(directionCombo.getSelectedItem()));

        executeBackground("Events", new BackgroundJob<EventListPage>() {
            @Override
            public EventListPage run() throws Exception {
                logger.information("Loading events", Map.of(
                        "page", page,
                        "pageSize", pageSize,
                        "filters", filter.describeActiveFilters(),
                        "sortField", sortField,
                        "ascending", ascending));
                return apiClient.getEvents(filter, page, pageSize, sortField, ascending);
            }

            @Override
            public void apply(EventListPage result) {
                lastPage = result;
                currentEvents = new ArrayList<>(result.items());
                eventTableModel.setRows(result.items());
                eventDetailArea.setText("");
                relatedEventsArea.setText("");
                refreshSeverityAggregation();
                setStatus("Loaded " + result.items().size() + " event(s). Total=" + result.total() + ", page=" + result.page() + ", pageSize=" + result.pageSize());
            }
        });
    }

    private void loadRelatedEvents() {
        LogEventRecord selected = getSelectedEvent();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an event first.", "Related events", JOptionPane.WARNING_MESSAGE);
            return;
        }
        executeBackground("Related events", new BackgroundJob<List<LogEventRecord>>() {
            @Override
            public List<LogEventRecord> run() throws Exception {
                return apiClient.getRelatedEvents(selected, settings.getApi().getDefaultPageSize());
            }

            @Override
            public void apply(List<LogEventRecord> result) {
                StringBuilder builder = new StringBuilder();
                if (result.isEmpty()) {
                    builder.append("No related events found or the selected event has no correlationId/traceId.");
                } else {
                    for (LogEventRecord item : result) {
                        builder.append(item.getOccurredAt())
                                .append(" | ")
                                .append(item.getSeverityText())
                                .append(" | ")
                                .append(item.getEventName())
                                .append(" | ")
                                .append(item.getMessageText())
                                .append(System.lineSeparator());
                    }
                }
                relatedEventsArea.setText(builder.toString());
                relatedEventsArea.setCaretPosition(0);
                setStatus("Loaded " + result.size() + " related event(s).");
            }
        });
    }

    private void loadSources() {
        executeBackground("Sources", new BackgroundJob<List<SourceSummary>>() {
            @Override
            public List<SourceSummary> run() throws Exception {
                return apiClient.getSources();
            }

            @Override
            public void apply(List<SourceSummary> result) {
                currentSources = new ArrayList<>(result);
                sourceTableModel.setRows(result);
                sourceDetailArea.setText("");
                setStatus("Loaded " + result.size() + " source(s).");
            }
        });
    }

    private void sendTestEvent() {
        TestLogEventRequest request = collectTestRequest();
        refreshIngestPreview();
        executeBackground("Send test event", new BackgroundJob<String>() {
            @Override
            public String run() throws Exception {
                return apiClient.sendTestLogEvent(request);
            }

            @Override
            public void apply(String ingestRequestId) {
                setStatus("Test event accepted. ingestRequestId=" + ingestRequestId);
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Test event accepted by the server.\nIngest request id: " + ingestRequestId,
                        "Ingest", JOptionPane.INFORMATION_MESSAGE);
                loadEvents();
            }
        });
    }

    private void exportCurrentEvents(String extension) {
        if (currentEvents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no events to export.", "Export", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser(settings.getExport().getDefaultDirectory());
        chooser.setSelectedFile(new java.io.File(exportService.defaultFileName("events", extension)));
        int choice = chooser.showSaveDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        executeBackground("Export " + extension.toUpperCase(), new BackgroundJob<Path>() {
            @Override
            public Path run() throws Exception {
                if ("csv".equalsIgnoreCase(extension)) {
                    exportService.exportToCsv(path, currentEvents);
                } else {
                    exportService.exportToJson(path, currentEvents);
                }
                return path;
            }

            @Override
            public void apply(Path result) {
                setStatus("Export written to " + result.toAbsolutePath());
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Export completed.\n" + result.toAbsolutePath(),
                        "Export", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void renderEventDetail(LogEventRecord record) {
        if (record == null) {
            eventDetailArea.setText("");
            return;
        }
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>(record.toMap());
        detail.put("occurredAt_display", timeService.formatForDisplay(record.getOccurredAt()));
        detail.put("observedAt_display", timeService.formatForDisplay(record.getObservedAt()));
        detail.put("receivedAt_display", timeService.formatForDisplay(record.getReceivedAt()));
        detail.put("ingestedAt_display", timeService.formatForDisplay(record.getIngestedAt()));
        eventDetailArea.setText(SimpleJson.pretty(detail));
        eventDetailArea.setCaretPosition(0);
    }

    private void renderSourceDetail(SourceSummary summary) {
        if (summary == null) {
            sourceDetailArea.setText("");
            return;
        }
        sourceDetailArea.setText(SimpleJson.pretty(summary.toMap()));
        sourceDetailArea.setCaretPosition(0);
    }

    private void refreshSeverityAggregation() {
        List<SeveritySummary> summaries = aggregationService.summarizeBySeverity(currentEvents);
        severitySummaryModel.setRowCount(0);
        for (SeveritySummary summary : summaries) {
            severitySummaryModel.addRow(new Object[]{summary.severityText(), summary.count()});
        }
    }

    private void refreshIngestPreview() {
        try {
            TestLogEventRequest request = collectTestRequest();
            Object attributes = SimpleJson.parse(request.getAttributesJsonText());
            Map<String, Object> payload = Map.of("events", List.of(request.toMap(attributes)));
            ingestPayloadPreview.setText(SimpleJson.pretty(payload));
        } catch (Exception exception) {
            ingestPayloadPreview.setText("Preview not available: " + exception.getMessage());
        }
        ingestPayloadPreview.setCaretPosition(0);
    }

    private EventQueryFilter collectEventFilter() {
        EventQueryFilter filter = new EventQueryFilter();
        filter.setSourceKey(sourceKeyField.getText());
        filter.setSeverityText(severityField.getText());
        filter.setTraceId(traceIdField.getText());
        filter.setCorrelationId(correlationIdField.getText());
        filter.setFrom(fromField.getText());
        filter.setTo(toField.getText());
        return filter;
    }

    private void clearEventFilters() {
        sourceKeyField.setText("");
        severityField.setText("");
        traceIdField.setText("");
        correlationIdField.setText("");
        fromField.setText("");
        toField.setText("");
        pageField.setText("1");
        pageSizeField.setText(String.valueOf(settings.getApi().getDefaultPageSize()));
        sortFieldCombo.setSelectedItem("occurredAt");
        directionCombo.setSelectedItem("DESC");
    }

    private TestLogEventRequest collectTestRequest() {
        TestLogEventRequest request = new TestLogEventRequest();
        request.setOccurredAt(ingestOccurredAtField.getText());
        request.setObservedAt(ingestObservedAtField.getText());
        request.setSeverityText(ingestSeverityTextField.getText());
        request.setSeverityNumber(parseInteger(ingestSeverityNumberField.getText(), 9));
        request.setEventName(ingestEventNameField.getText());
        request.setEventCategory(ingestEventCategoryField.getText());
        request.setEventAction(ingestEventActionField.getText());
        request.setCorrelationId(ingestCorrelationIdField.getText());
        request.setTraceId(ingestTraceIdField.getText());
        request.setHostName(ingestHostField.getText());
        request.setServiceName(ingestServiceField.getText());
        request.setMessage(ingestMessageArea.getText());
        request.setAttributesJsonText(ingestAttributesArea.getText());
        return request;
    }

    private LogEventRecord getSelectedEvent() {
        int viewRow = eventTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return eventTableModel.getRow(eventTable.convertRowIndexToModel(viewRow));
    }

    private <T> void executeBackground(String label, BackgroundJob<T> job) {
        setStatus(label + " running...");
        new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                return job.run();
            }

            @Override
            protected void done() {
                try {
                    Object result = get();
                    job.applyUnchecked(result);
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    handleFailure(label, cause);
                }
            }
        }.execute();
    }

    private void handleFailure(String label, Throwable throwable) {
        Map<String, Object> context = new LinkedHashMap<>();
        if (throwable instanceof ApiClientException apiException) {
            context.put("statusCode", apiException.getStatusCode());
            context.put("errorCode", apiException.getErrorCode());
            context.put("requestId", apiException.getRequestId());
            if (!apiException.getResponseBody().isBlank()) {
                context.put("responseBody", apiException.getResponseBody());
            }
        }
        logger.error(label + " failed", context, throwable);
        setStatus(label + " failed: " + throwable.getMessage());
        JOptionPane.showMessageDialog(this,
                buildErrorText(label, throwable),
                label + " failed",
                JOptionPane.ERROR_MESSAGE);
    }

    private String buildErrorText(String label, Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        builder.append(label).append(" failed.");
        builder.append(System.lineSeparator()).append(System.lineSeparator());
        builder.append(throwable.getMessage());
        if (throwable instanceof ApiClientException apiException) {
            if (apiException.getStatusCode() > 0) {
                builder.append(System.lineSeparator()).append("HTTP status: ").append(apiException.getStatusCode());
            }
            if (!apiException.getErrorCode().isBlank()) {
                builder.append(System.lineSeparator()).append("Error code: ").append(apiException.getErrorCode());
            }
            if (!apiException.getRequestId().isBlank()) {
                builder.append(System.lineSeparator()).append("Request ID: ").append(apiException.getRequestId());
            }
        }
        return builder.toString();
    }

    private JPanel buildTextPanel(String title, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private JTextArea createReadOnlyArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(false);
        area.setWrapStyleWord(false);
        return area;
    }

    private void addLabeledField(JPanel panel,
                                 GridBagConstraints c,
                                 int row,
                                 String label1,
                                 java.awt.Component field1,
                                 String label2,
                                 java.awt.Component field2,
                                 String label3,
                                 java.awt.Component field3) {
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridx = 0;
        panel.add(new JLabel(label1), c);
        c.gridx = 1;
        c.weightx = 0.33;
        panel.add(field1, c);
        c.weightx = 0;
        c.gridx = 2;
        panel.add(new JLabel(label2), c);
        c.gridx = 3;
        c.weightx = 0.33;
        panel.add(field2, c);
        c.weightx = 0;
        c.gridx = 4;
        panel.add(new JLabel(label3), c);
        c.gridx = 5;
        c.weightx = 0.34;
        panel.add(field3, c);
    }

    private int parseInteger(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void setStatus(String text) {
        statusLabel.setText(text + "   |   last page total=" + lastPage.total() + ", current events=" + currentEvents.size() + ", current sources=" + currentSources.size());
    }

    private interface BackgroundJob<T> {
        T run() throws Exception;
        void apply(T result);

        @SuppressWarnings("unchecked")
        default void applyUnchecked(Object result) {
            apply((T) result);
        }
    }

    /**
     * Tiny helper for readable split layout creation without extra dependencies.
     */
    private static final class GridLayoutBuilder {
        private int rows = 1;
        private int cols = 1;
        GridLayoutBuilder rows(int rows) { this.rows = rows; return this; }
        GridLayoutBuilder cols(int cols) { this.cols = cols; return this; }
        java.awt.GridLayout build() { return new java.awt.GridLayout(rows, cols, 8, 8); }
    }

    private static final class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable runnable;

        private SimpleDocumentListener(Runnable runnable) { this.runnable = runnable; }
        public static SimpleDocumentListener of(Runnable runnable) { return new SimpleDocumentListener(runnable); }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { runnable.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { runnable.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { runnable.run(); }
    }
}
