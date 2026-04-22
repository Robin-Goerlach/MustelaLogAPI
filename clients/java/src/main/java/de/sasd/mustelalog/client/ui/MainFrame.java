package de.sasd.mustelalog.client.ui;

import de.sasd.mustelalog.client.api.ApiClientException;
import de.sasd.mustelalog.client.api.MustelaLogApiClient;
import de.sasd.mustelalog.client.config.ClientSettings;
import de.sasd.mustelalog.client.json.SimpleJson;
import de.sasd.mustelalog.client.logging.ClientLogger;
import de.sasd.mustelalog.client.logging.MemoryClientLogger;
import de.sasd.mustelalog.client.model.AggregationBucket;
import de.sasd.mustelalog.client.model.EventListResponse;
import de.sasd.mustelalog.client.model.EventQueryFilter;
import de.sasd.mustelalog.client.model.LogEventRecord;
import de.sasd.mustelalog.client.model.SavedViewDefinition;
import de.sasd.mustelalog.client.model.SourceSummary;
import de.sasd.mustelalog.client.model.TimeBucket;
import de.sasd.mustelalog.client.model.TimeMode;
import de.sasd.mustelalog.client.service.AggregationService;
import de.sasd.mustelalog.client.service.ExportService;
import de.sasd.mustelalog.client.service.RelatedEventsService;
import de.sasd.mustelalog.client.service.SavedViewService;
import de.sasd.mustelalog.client.service.TimeDisplayService;
import de.sasd.mustelalog.client.ui.dialog.DiagnosticsDialog;
import de.sasd.mustelalog.client.ui.dialog.SendTestLogDialog;
import de.sasd.mustelalog.client.ui.table.AggregationTableModel;
import de.sasd.mustelalog.client.ui.table.EventTableModel;
import de.sasd.mustelalog.client.util.SwingWorkerRunner;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main desktop window of the MustelaLog Swing client.
 *
 * <p>The class is intentionally larger than a pure enterprise-style presenter. That is a conscious
 * V1 trade-off. The UI stays understandable because related responsibilities are grouped into clear
 * helper methods, and the non-trivial technical logic still lives in dedicated services.</p>
 */
public final class MainFrame extends JFrame
{
    private static final String[] QUICK_RANGES =
    {
        "Last 5 minutes",
        "Last 15 minutes",
        "Last hour",
        "Today",
        "Last 24 hours",
        "Last 7 days"
    };

    private final ClientSettings settings;
    private final MustelaLogApiClient apiClient;
    private final AggregationService aggregationService;
    private final ExportService exportService;
    private final SavedViewService savedViewService;
    private final RelatedEventsService relatedEventsService;
    private final TimeDisplayService timeDisplayService;
    private final ClientLogger logger;
    private final MemoryClientLogger memoryLogger;

    private final EventTableModel eventTableModel;
    private final JTable eventTable;
    private final TableRowSorter<EventTableModel> eventSorter;
    private final EventTableModel relatedTableModel;
    private final JTable relatedTable;

    private final AggregationTableModel severityAggregationModel = new AggregationTableModel("Severity");
    private final AggregationTableModel sourceAggregationModel = new AggregationTableModel("Source / Host");
    private final AggregationTableModel serviceAggregationModel = new AggregationTableModel("Service");
    private final AggregationTableModel categoryAggregationModel = new AggregationTableModel("Category");
    private final AggregationTableModel outcomeAggregationModel = new AggregationTableModel("Outcome");
    private final AggregationTableModel timeAggregationModel = new AggregationTableModel("Time Bucket");

    private final JComboBox<String> quickRangeCombo = new JComboBox<>(QUICK_RANGES);
    private final JSpinner fromSpinner = createDateTimeSpinner();
    private final JSpinner toSpinner = createDateTimeSpinner();
    private final JTextField sourceFilterField = new JTextField();
    private final JTextField hostnameFilterField = new JTextField();
    private final JTextField serviceFilterField = new JTextField();
    private final JTextField severityFilterField = new JTextField();
    private final JTextField categoryFilterField = new JTextField();
    private final JTextField actionFilterField = new JTextField();
    private final JTextField outcomeFilterField = new JTextField();
    private final JTextField textSearchFilterField = new JTextField();
    private final JTextField correlationFilterField = new JTextField();
    private final JTextField traceFilterField = new JTextField();
    private final JTextField requestFilterField = new JTextField();
    private final JTextField componentFilterField = new JTextField();
    private final JTextField actorUserFilterField = new JTextField();
    private final JTextField actorPrincipalFilterField = new JTextField();
    private final JTextField sessionFilterField = new JTextField();
    private final JTextField clientIpFilterField = new JTextField();
    private final JTextField serverIpFilterField = new JTextField();
    private final JCheckBox onlyPayloadCheckBox = new JCheckBox("Only events with payload");
    private final JCheckBox onlyCorrelationCheckBox = new JCheckBox("Only events with correlation / trace");
    private final JCheckBox onlyActorCheckBox = new JCheckBox("Only events with actor");
    private final JPanel activeFiltersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    private final JLabel localFilterNoticeLabel = new JLabel(" ");

    private final JComboBox<TimeMode> timeModeCombo = new JComboBox<>(TimeMode.values());
    private final JComboBox<SavedViewDefinition> savedViewsCombo = new JComboBox<>();
    private final JTextField savedViewNameField = new JTextField(18);
    private final JLabel connectionStatusLabel = new JLabel("Disconnected");
    private final JLabel statusMessageLabel = new JLabel("Ready");
    private final JLabel lastRefreshLabel = new JLabel("Last refresh: -");
    private final JLabel lastResponseLabel = new JLabel("Last response: -");
    private final JLabel timeModeStatusLabel = new JLabel("Time mode: -");
    private final JLabel pageSummaryLabel = new JLabel("Page 1");
    private final JLabel relatedTitleLabel = new JLabel("Related events");

    private final JTextField eventIdField = readonlyField();
    private final JTextField occurredAtField = readonlyField();
    private final JTextField observedAtField = readonlyField();
    private final JTextField receivedAtField = readonlyField();
    private final JTextField ingestedAtField = readonlyField();
    private final JTextField severityField = readonlyField();
    private final JTextField eventNameField = readonlyField();
    private final JTextField categoryActionOutcomeField = readonlyField();
    private final JTextField sourceNameField = readonlyField();
    private final JTextField sourceKeyField = readonlyField();
    private final JTextField hostField = readonlyField();
    private final JTextField serviceField = readonlyField();
    private final JTextField componentField = readonlyField();
    private final JTextField versionEnvironmentField = readonlyField();
    private final JTextField actorUserField = readonlyField();
    private final JTextField actorPrincipalField = readonlyField();
    private final JTextField sessionField = readonlyField();
    private final JTextField clientIpField = readonlyField();
    private final JTextField serverIpField = readonlyField();
    private final JTextField traceIdField = readonlyField();
    private final JTextField spanIdField = readonlyField();
    private final JTextField correlationIdField = readonlyField();
    private final JTextField requestIdField = readonlyField();
    private final JTextField classificationField = readonlyField();
    private final JTextField retentionField = readonlyField();
    private final JTextField legalHoldField = readonlyField();
    private final JTextArea messageArea = readonlyArea(4);
    private final JTextArea attributesArea = readonlyArea(8);
    private final JTextArea payloadArea = readonlyArea(8);
    private final JTextArea integrityArea = readonlyArea(4);

    private final Map<String, Integer> columnDefaultWidths = new LinkedHashMap<>();
    private final Map<String, JCheckBox> columnVisibilityChecks = new LinkedHashMap<>();
    private final Map<String, TableColumn> columnsByHeader = new LinkedHashMap<>();

    private TimeMode timeMode;
    private List<LogEventRecord> currentPageRows = new ArrayList<>();
    private List<LogEventRecord> visibleRows = new ArrayList<>();
    private List<SourceSummary> sources = new ArrayList<>();
    private LogEventRecord selectedEvent;
    private int currentPage = 1;
    private int pageSize;
    private int totalCount;
    private long lastResponseMillis;
    private Timer autoRefreshTimer;

    public MainFrame(ClientSettings settings,
                     MustelaLogApiClient apiClient,
                     AggregationService aggregationService,
                     ExportService exportService,
                     SavedViewService savedViewService,
                     RelatedEventsService relatedEventsService,
                     TimeDisplayService timeDisplayService,
                     ClientLogger logger,
                     MemoryClientLogger memoryLogger)
    {
        this.settings = settings;
        this.apiClient = apiClient;
        this.aggregationService = aggregationService;
        this.exportService = exportService;
        this.savedViewService = savedViewService;
        this.relatedEventsService = relatedEventsService;
        this.timeDisplayService = timeDisplayService;
        this.logger = logger;
        this.memoryLogger = memoryLogger;
        this.timeMode = settings.getUi().getDefaultTimeMode();
        this.pageSize = Math.max(10, settings.getApi().getDefaultPageSize());

        this.eventTableModel = new EventTableModel(timeDisplayService);
        this.relatedTableModel = new EventTableModel(timeDisplayService);
        this.eventTable = new JTable(eventTableModel);
        this.relatedTable = new JTable(relatedTableModel);
        this.eventSorter = new TableRowSorter<>(eventTableModel);

        initializeWindow();
        initializeBehavior();
        loadSavedViews();
        startAutoRefreshIfConfigured();
        refreshEvents();
        loadSourcesInBackground();
    }

    private void initializeWindow()
    {
        setTitle("MustelaLog Swing Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(settings.getUi().getDefaultWindowWidth(), settings.getUi().getDefaultWindowHeight());
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildMainSplitPane(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        configureEventTable(eventTable);
        configureEventTable(relatedTable);
        eventTable.setRowSorter(eventSorter);
        rememberColumns();
        applyTimeMode(timeMode);
    }

    private void initializeBehavior()
    {
        timeModeCombo.setSelectedItem(timeMode);
        timeModeCombo.addActionListener(event -> applyTimeMode((TimeMode) timeModeCombo.getSelectedItem()));

        eventTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting())
            {
                int selectedRow = eventTable.getSelectedRow();
                selectedEvent = selectedRow >= 0 ? eventTableModel.getRow(eventTable.convertRowIndexToModel(selectedRow)) : null;
                updateDetailFields();
            }
        });

        eventSorter.addRowSorterListener(event -> pageSummaryLabel.setText(buildPageSummary()));
    }

    private Component buildToolbar()
    {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refreshEvents());
        toolBar.add(refreshButton);

        JButton sendTestButton = new JButton("Send test log event");
        sendTestButton.addActionListener(event -> openSendTestDialog());
        toolBar.add(sendTestButton);

        JButton diagnosticsButton = new JButton("Open diagnostics");
        diagnosticsButton.addActionListener(event -> new DiagnosticsDialog(this, memoryLogger).setVisible(true));
        toolBar.add(diagnosticsButton);

        toolBar.addSeparator();

        JButton exportCsvButton = new JButton("Export CSV");
        exportCsvButton.addActionListener(event -> exportVisibleRows(true));
        JButton exportJsonButton = new JButton("Export JSON");
        exportJsonButton.addActionListener(event -> exportVisibleRows(false));
        toolBar.add(exportCsvButton);
        toolBar.add(exportJsonButton);

        toolBar.addSeparator();
        toolBar.add(new JLabel("Time mode: "));
        toolBar.add(timeModeCombo);

        toolBar.addSeparator();
        toolBar.add(new JLabel("Saved view: "));
        toolBar.add(savedViewsCombo);
        toolBar.add(savedViewNameField);

        JButton saveViewButton = new JButton("Save");
        saveViewButton.addActionListener(event -> saveCurrentView());
        JButton loadViewButton = new JButton("Load");
        loadViewButton.addActionListener(event -> loadSelectedView());
        JButton renameViewButton = new JButton("Rename");
        renameViewButton.addActionListener(event -> renameSelectedView());
        JButton deleteViewButton = new JButton("Delete");
        deleteViewButton.addActionListener(event -> deleteSelectedView());

        toolBar.add(saveViewButton);
        toolBar.add(loadViewButton);
        toolBar.add(renameViewButton);
        toolBar.add(deleteViewButton);
        return toolBar;
    }

    private Component buildMainSplitPane()
    {
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplit.setLeftComponent(buildFilterPane());
        horizontalSplit.setRightComponent(buildCenterAndDetailSplit());
        horizontalSplit.setDividerLocation(320);
        return horizontalSplit;
    }

    private Component buildFilterPane()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        panel.add(sectionTitle("Filters"));
        panel.add(labeledComponent("Quick range", quickRangeCombo));
        JButton applyQuickRangeButton = new JButton("Apply quick range");
        applyQuickRangeButton.addActionListener(event -> applyQuickRange());
        panel.add(applyQuickRangeButton);
        panel.add(smallHint("Date and time filters are real date-time values in the Swing client. That avoids the earlier WPF day-only ambiguity."));

        panel.add(labeledComponent("From", fromSpinner));
        panel.add(labeledComponent("To", toSpinner));
        panel.add(labeledComponent("Source", sourceFilterField));
        panel.add(labeledComponent("Hostname", hostnameFilterField));
        panel.add(labeledComponent("Service", serviceFilterField));
        panel.add(labeledComponent("Severity", severityFilterField));
        panel.add(labeledComponent("Category", categoryFilterField));
        panel.add(labeledComponent("Action", actionFilterField));
        panel.add(labeledComponent("Outcome", outcomeFilterField));
        panel.add(labeledComponent("Text search", textSearchFilterField));
        panel.add(labeledComponent("Correlation ID", correlationFilterField));
        panel.add(labeledComponent("Trace ID", traceFilterField));
        panel.add(labeledComponent("Request ID", requestFilterField));
        panel.add(labeledComponent("Component", componentFilterField));
        panel.add(labeledComponent("Actor User ID", actorUserFilterField));
        panel.add(labeledComponent("Actor Principal", actorPrincipalFilterField));
        panel.add(labeledComponent("Session Hash", sessionFilterField));
        panel.add(labeledComponent("Client IP", clientIpFilterField));
        panel.add(labeledComponent("Server IP", serverIpFilterField));
        panel.add(onlyPayloadCheckBox);
        panel.add(onlyCorrelationCheckBox);
        panel.add(onlyActorCheckBox);

        JPanel filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton applyFiltersButton = new JButton("Apply filters");
        applyFiltersButton.addActionListener(event -> {
            currentPage = 1;
            refreshEvents();
        });
        JButton clearFiltersButton = new JButton("Clear");
        clearFiltersButton.addActionListener(event -> clearFilters());
        filterButtonPanel.add(applyFiltersButton);
        filterButtonPanel.add(clearFiltersButton);
        panel.add(filterButtonPanel);

        panel.add(sectionTitle("Active filters"));
        activeFiltersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(activeFiltersPanel);

        panel.add(sectionTitle("Visible columns"));
        panel.add(buildColumnVisibilityPanel());

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private Component buildColumnVisibilityPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (String columnName : new String[] {"Occurred At", "Ingested At", "Severity", "Source / Host", "Service", "Category", "Action", "Outcome", "Message", "Correlation / Trace", "Event ID"})
        {
            JCheckBox checkBox = new JCheckBox(columnName, true);
            checkBox.addActionListener(event -> setColumnVisible(columnName, checkBox.isSelected()));
            columnVisibilityChecks.put(columnName, checkBox);
            panel.add(checkBox);
        }
        return panel;
    }

    private Component buildCenterAndDetailSplit()
    {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(buildCenterArea());
        splitPane.setRightComponent(buildDetailPane());
        splitPane.setDividerLocation(950);
        return splitPane;
    }

    private Component buildCenterArea()
    {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel north = new JPanel(new BorderLayout());
        JLabel title = sectionTitle("Events");
        north.add(title, BorderLayout.NORTH);
        localFilterNoticeLabel.setOpaque(true);
        localFilterNoticeLabel.setBackground(new Color(255, 247, 232));
        localFilterNoticeLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 216, 168)),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        north.add(localFilterNoticeLabel, BorderLayout.SOUTH);
        panel.add(north, BorderLayout.NORTH);

        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);
        panel.add(buildBottomArea(), BorderLayout.SOUTH);
        return panel;
    }

    private Component buildBottomArea()
    {
        JPanel wrapper = new JPanel(new BorderLayout());

        JPanel pageControls = new JPanel(new BorderLayout());
        pageControls.add(pageSummaryLabel, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton previousButton = new JButton("Previous");
        previousButton.addActionListener(event -> { if (currentPage > 1) { currentPage--; refreshEvents(); } });
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(event -> { if (visibleRows.size() >= pageSize) { currentPage++; refreshEvents(); } });
        buttons.add(previousButton);
        buttons.add(nextButton);
        pageControls.add(buttons, BorderLayout.EAST);
        wrapper.add(pageControls, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Related Events", buildRelatedPanel());
        tabs.addTab("Aggregations", buildAggregationPanel());
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
    }

    private Component buildRelatedPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(createRelatedButton("By Correlation ID", "correlation"));
        buttons.add(createRelatedButton("By Trace ID", "trace"));
        buttons.add(createRelatedButton("By Request ID", "request"));
        buttons.add(createRelatedButton("By Source", "source"));
        buttons.add(createRelatedButton("By Session", "session"));
        buttons.add(createRelatedButton("By Actor", "actor"));
        panel.add(buttons, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(relatedTitleLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(relatedTable), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JButton createRelatedButton(String text, String mode)
    {
        JButton button = new JButton(text);
        button.addActionListener(event -> loadRelatedEvents(mode));
        return button;
    }

    private Component buildAggregationPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        addAggregationTable(panel, gbc, 0, 0, "By Severity", severityAggregationModel);
        addAggregationTable(panel, gbc, 1, 0, "By Source / Host", sourceAggregationModel);
        addAggregationTable(panel, gbc, 2, 0, "By Service", serviceAggregationModel);
        addAggregationTable(panel, gbc, 0, 1, "By Category", categoryAggregationModel);
        addAggregationTable(panel, gbc, 1, 1, "By Outcome", outcomeAggregationModel);
        addAggregationTable(panel, gbc, 2, 1, "Over Time (current page)", timeAggregationModel);
        return panel;
    }

    private void addAggregationTable(JPanel panel, GridBagConstraints gbc, int gridx, int gridy, String title, AggregationTableModel model)
    {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(scrollPane, gbc);
    }

    private Component buildDetailPane()
    {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(sectionTitle("Event Details"));

        panel.add(buildFieldGroup("General", Map.of(
            "Event ID", eventIdField,
            "Occurred At", occurredAtField,
            "Observed At", observedAtField,
            "Received At", receivedAtField,
            "Ingested At", ingestedAtField,
            "Severity", severityField,
            "Event Name", eventNameField,
            "Category / Action / Outcome", categoryActionOutcomeField)));

        panel.add(buildFieldGroup("Source", Map.of(
            "Source Name", sourceNameField,
            "Source Key", sourceKeyField,
            "Host", hostField,
            "Service", serviceField,
            "Component", componentField,
            "Version / Environment", versionEnvironmentField)));

        panel.add(buildFieldGroup("Actor / Context", Map.of(
            "Actor User", actorUserField,
            "Actor Principal", actorPrincipalField,
            "Session Hash", sessionField,
            "Client IP", clientIpField,
            "Server IP", serverIpField)));

        panel.add(buildFieldGroup("Correlation", Map.of(
            "Trace ID", traceIdField,
            "Span ID", spanIdField,
            "Correlation ID", correlationIdField,
            "Request ID", requestIdField)));

        panel.add(buildTextAreaGroup("Message", messageArea));
        panel.add(buildTextAreaGroup("Attributes JSON", attributesArea));
        panel.add(buildTextAreaGroup("Raw Payload JSON", payloadArea));

        panel.add(buildFieldGroup("Governance", Map.of(
            "Classification", classificationField,
            "Retention", retentionField,
            "Legal Hold", legalHoldField)));
        panel.add(buildTextAreaGroup("Hash / Chain / Signature", integrityArea));

        JPanel copyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        copyPanel.add(copyButton("Copy Event ID", eventIdField));
        copyPanel.add(copyButton("Copy Message", messageArea));
        copyPanel.add(copyButton("Copy JSON", attributesArea));
        copyPanel.add(copyButton("Copy Correlation", correlationIdField));
        copyPanel.add(copyButton("Copy Trace", traceIdField));
        panel.add(copyPanel);

        return new JScrollPane(panel);
    }

    private Component buildFieldGroup(String title, Map<String, ? extends Component> fields)
    {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        int row = 0;
        for (Map.Entry<String, ? extends Component> entry : fields.entrySet())
        {
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; panel.add(new JLabel(entry.getKey() + ":"), gbc);
            gbc.gridx = 1; gbc.weightx = 1; panel.add(entry.getValue(), gbc);
            row++;
        }
        return panel;
    }

    private Component buildTextAreaGroup(String title, JTextArea textArea)
    {
        JScrollPane pane = new JScrollPane(textArea);
        pane.setBorder(BorderFactory.createTitledBorder(title));
        pane.setPreferredSize(new Dimension(350, 120));
        return pane;
    }

    private Component buildStatusBar()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)));
        panel.add(connectionStatusLabel);
        panel.add(new JLabel("|"));
        panel.add(statusMessageLabel);
        panel.add(new JLabel("|"));
        panel.add(lastRefreshLabel);
        panel.add(new JLabel("|"));
        panel.add(lastResponseLabel);
        panel.add(new JLabel("|"));
        panel.add(timeModeStatusLabel);
        return panel;
    }

    private void configureEventTable(JTable table)
    {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(24);
        table.setDefaultRenderer(Object.class, new SeverityAwareRenderer());
    }

    private void rememberColumns()
    {
        TableColumnModel model = eventTable.getColumnModel();
        for (int index = 0; index < model.getColumnCount(); index++)
        {
            TableColumn column = model.getColumn(index);
            String header = String.valueOf(column.getHeaderValue());
            columnsByHeader.put(header, column);
            columnDefaultWidths.put(header, column.getWidth() <= 0 ? 120 : column.getWidth());
        }
    }

    private void setColumnVisible(String header, boolean visible)
    {
        TableColumn column = columnsByHeader.get(header);
        if (column == null) return;
        if (visible)
        {
            int width = columnDefaultWidths.getOrDefault(header, 120);
            column.setMinWidth(15);
            column.setMaxWidth(1000);
            column.setPreferredWidth(width);
            column.setWidth(width);
        }
        else
        {
            column.setMinWidth(0);
            column.setMaxWidth(0);
            column.setPreferredWidth(0);
            column.setWidth(0);
        }
    }

    private void refreshEvents()
    {
        connectionStatusLabel.setText("Loading...");
        statusMessageLabel.setText("Loading events...");
        EventQueryFilter filter = buildFilterFromControls();
        updateActiveFilterChips(filter);

        SwingWorkerRunner.run(
            () -> apiClient.getEvents(filter, currentPage, pageSize, "occurredAt", false),
            this::handleEventsLoaded,
            this::handleBackgroundError);
    }

    private void handleEventsLoaded(EventListResponse response)
    {
        this.currentPageRows = response.getItems();
        this.visibleRows = applyLocalRefinements(currentPageRows, buildFilterFromControls());
        this.totalCount = response.getTotal();
        eventTableModel.setRows(visibleRows);
        if (!visibleRows.isEmpty())
        {
            eventTable.setRowSelectionInterval(0, 0);
        }
        else
        {
            selectedEvent = null;
            updateDetailFields();
        }
        updateAggregations();
        pageSummaryLabel.setText(buildPageSummary());
        updateLocalFilteringNotice();
        connectionStatusLabel.setText("Connected");
        statusMessageLabel.setText("Loaded " + visibleRows.size() + " row(s)");
        lastRefreshLabel.setText("Last refresh: " + java.time.LocalTime.now().withNano(0));
        lastResponseLabel.setText("Last response: " + lastResponseMillis + " ms");
    }

    private List<LogEventRecord> applyLocalRefinements(List<LogEventRecord> rows, EventQueryFilter filter)
    {
        List<LogEventRecord> result = new ArrayList<>();
        for (LogEventRecord row : rows)
        {
            if (!containsIgnoreCase(row.getHostName(), filter.getHostname())) continue;
            if (!containsIgnoreCase(row.getServiceName(), filter.getService())) continue;
            if (!containsIgnoreCase(row.getEventCategory(), filter.getEventCategory())) continue;
            if (!containsIgnoreCase(row.getEventAction(), filter.getEventAction())) continue;
            if (!containsIgnoreCase(row.getEventOutcome(), filter.getEventOutcome())) continue;
            if (!containsIgnoreCase(row.getComponentName(), filter.getComponent())) continue;
            if (!containsIgnoreCase(row.getActorUserId(), filter.getActorUserId())) continue;
            if (!containsIgnoreCase(row.getActorPrincipal(), filter.getActorPrincipal())) continue;
            if (!containsIgnoreCase(row.getSessionHashSha256(), filter.getSessionHash())) continue;
            if (!containsIgnoreCase(row.getClientIp(), filter.getClientIp())) continue;
            if (!containsIgnoreCase(row.getServerIp(), filter.getServerIp())) continue;
            if (!containsIgnoreCase(row.getRequestCorrelationId(), filter.getRequestId())) continue;
            if (!matchesTextSearch(row, filter.getTextSearch())) continue;
            if (filter.isOnlyWithPayload() && row.getRawPayloadJson() == null) continue;
            if (filter.isOnlyWithCorrelation() && isBlank(row.getCorrelationId()) && isBlank(row.getTraceId())) continue;
            if (filter.isOnlyWithActor() && isBlank(row.getActorUserId()) && isBlank(row.getActorPrincipal())) continue;
            result.add(row);
        }
        return result;
    }

    private void updateLocalFilteringNotice()
    {
        EventQueryFilter filter = buildFilterFromControls();
        boolean localOnly = hasText(filter.getHostname()) || hasText(filter.getService()) || hasText(filter.getEventCategory())
            || hasText(filter.getEventAction()) || hasText(filter.getEventOutcome()) || hasText(filter.getTextSearch())
            || hasText(filter.getRequestId()) || hasText(filter.getComponent()) || hasText(filter.getActorUserId())
            || hasText(filter.getActorPrincipal()) || hasText(filter.getSessionHash()) || hasText(filter.getClientIp())
            || hasText(filter.getServerIp()) || filter.isOnlyWithPayload() || filter.isOnlyWithCorrelation() || filter.isOnlyWithActor();
        localFilterNoticeLabel.setText(localOnly
            ? "Some filters are currently applied only to the loaded page because the V1 API does not yet provide all filter endpoints server-side."
            : " ");
    }

    private void updateActiveFilterChips(EventQueryFilter filter)
    {
        activeFiltersPanel.removeAll();
        for (Map.Entry<String, String> entry : filter.toChipMap().entrySet())
        {
            JLabel label = new JLabel(entry.getKey() + ": " + entry.getValue());
            label.setOpaque(true);
            label.setBackground(new Color(238, 245, 255));
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 232, 255)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            activeFiltersPanel.add(label);
        }
        activeFiltersPanel.revalidate();
        activeFiltersPanel.repaint();
    }

    private String buildPageSummary()
    {
        return "Page " + currentPage + " | visible rows: " + visibleRows.size() + " | total rows reported by API: " + totalCount;
    }

    private void updateDetailFields()
    {
        LogEventRecord item = selectedEvent;
        eventIdField.setText(text(item == null ? null : item.getLogEventId()));
        occurredAtField.setText(item == null ? "-" : timeDisplayService.formatApiTime(item.getOccurredAt(), timeMode));
        observedAtField.setText(item == null ? "-" : timeDisplayService.formatApiTime(item.getObservedAt(), timeMode));
        receivedAtField.setText(item == null ? "-" : timeDisplayService.formatApiTime(item.getReceivedAt(), timeMode));
        ingestedAtField.setText(item == null ? "-" : timeDisplayService.formatApiTime(item.getIngestedAt(), timeMode));
        severityField.setText(text(item == null ? null : item.getSeverityText()));
        eventNameField.setText(text(item == null ? null : item.getEventName()));
        categoryActionOutcomeField.setText(item == null ? "-" : joinParts(item.getEventCategory(), item.getEventAction(), item.getEventOutcome()));
        sourceNameField.setText(text(item == null ? null : item.getSourceName()));
        sourceKeyField.setText(text(item == null ? null : item.getSourceKey()));
        hostField.setText(text(item == null ? null : item.getHostName()));
        serviceField.setText(text(item == null ? null : item.getServiceName()));
        componentField.setText(text(item == null ? null : item.getComponentName()));
        versionEnvironmentField.setText(item == null ? "-" : text(findVersionAndEnvironment(item)));
        actorUserField.setText(text(item == null ? null : item.getActorUserId()));
        actorPrincipalField.setText(text(item == null ? null : item.getActorPrincipal()));
        sessionField.setText(text(item == null ? null : item.getSessionHashSha256()));
        clientIpField.setText(text(item == null ? null : item.getClientIp()));
        serverIpField.setText(text(item == null ? null : item.getServerIp()));
        traceIdField.setText(text(item == null ? null : item.getTraceId()));
        spanIdField.setText(text(item == null ? null : item.getSpanId()));
        correlationIdField.setText(text(item == null ? null : item.getCorrelationId()));
        requestIdField.setText(text(item == null ? null : item.getRequestCorrelationId()));
        messageArea.setText(text(item == null ? null : item.getMessageText()));
        attributesArea.setText(item == null ? "" : prettyJson(item.getAttributesJson()));
        payloadArea.setText(item == null ? "" : prettyJson(item.getRawPayloadJson()));
        classificationField.setText(text(item == null ? null : item.getClassificationCode()));
        retentionField.setText(text(item == null ? null : item.getRetentionPolicyCode()));
        legalHoldField.setText(item == null || item.getLegalHoldFlag() == null ? "-" : item.getLegalHoldFlag().toString());
        integrityArea.setText(item == null ? "" : buildIntegrityText(item));
    }

    private void updateAggregations()
    {
        severityAggregationModel.setRows(aggregationService.aggregateBySeverity(visibleRows));
        sourceAggregationModel.setRows(aggregationService.aggregateBySource(visibleRows));
        serviceAggregationModel.setRows(aggregationService.aggregateByService(visibleRows));
        categoryAggregationModel.setRows(aggregationService.aggregateByCategory(visibleRows));
        outcomeAggregationModel.setRows(aggregationService.aggregateByOutcome(visibleRows));
        List<AggregationBucket> timeBuckets = new ArrayList<>();
        for (TimeBucket bucket : aggregationService.aggregateOverTime(visibleRows))
        {
            timeBuckets.add(new AggregationBucket(bucket.getLabel(), bucket.getCount()));
        }
        timeAggregationModel.setRows(timeBuckets);
    }

    private void applyTimeMode(TimeMode mode)
    {
        this.timeMode = mode == null ? TimeMode.LOCAL : mode;
        eventTableModel.setTimeMode(this.timeMode);
        relatedTableModel.setTimeMode(this.timeMode);
        updateDetailFields();
        timeModeStatusLabel.setText("Time mode: " + this.timeMode);
    }

    private void applyQuickRange()
    {
        String selected = (String) quickRangeCombo.getSelectedItem();
        LocalDateTime now = LocalDateTime.now();
        if (selected == null) return;
        switch (selected)
        {
            case "Last 5 minutes" -> { fromSpinner.setValue(asDate(now.minusMinutes(5))); toSpinner.setValue(asDate(now)); }
            case "Last 15 minutes" -> { fromSpinner.setValue(asDate(now.minusMinutes(15))); toSpinner.setValue(asDate(now)); }
            case "Last hour" -> { fromSpinner.setValue(asDate(now.minusHours(1))); toSpinner.setValue(asDate(now)); }
            case "Today" -> { fromSpinner.setValue(asDate(now.withHour(0).withMinute(0).withSecond(0))); toSpinner.setValue(asDate(now)); }
            case "Last 24 hours" -> { fromSpinner.setValue(asDate(now.minusHours(24))); toSpinner.setValue(asDate(now)); }
            case "Last 7 days" -> { fromSpinner.setValue(asDate(now.minusDays(7))); toSpinner.setValue(asDate(now)); }
            default -> { }
        }
    }

    private void clearFilters()
    {
        fromSpinner.setValue(new Date());
        toSpinner.setValue(new Date());
        sourceFilterField.setText(""); hostnameFilterField.setText(""); serviceFilterField.setText(""); severityFilterField.setText("");
        categoryFilterField.setText(""); actionFilterField.setText(""); outcomeFilterField.setText(""); textSearchFilterField.setText("");
        correlationFilterField.setText(""); traceFilterField.setText(""); requestFilterField.setText(""); componentFilterField.setText("");
        actorUserFilterField.setText(""); actorPrincipalFilterField.setText(""); sessionFilterField.setText(""); clientIpFilterField.setText(""); serverIpFilterField.setText("");
        onlyPayloadCheckBox.setSelected(false); onlyCorrelationCheckBox.setSelected(false); onlyActorCheckBox.setSelected(false);
        currentPage = 1;
        refreshEvents();
    }

    private EventQueryFilter buildFilterFromControls()
    {
        EventQueryFilter filter = new EventQueryFilter();
        filter.setFromLocal(asLocalDateTime((Date) fromSpinner.getValue()));
        filter.setToLocal(asLocalDateTime((Date) toSpinner.getValue()));
        filter.setSourceKey(sourceFilterField.getText().trim());
        filter.setHostname(hostnameFilterField.getText().trim());
        filter.setService(serviceFilterField.getText().trim());
        filter.setSeverity(severityFilterField.getText().trim());
        filter.setEventCategory(categoryFilterField.getText().trim());
        filter.setEventAction(actionFilterField.getText().trim());
        filter.setEventOutcome(outcomeFilterField.getText().trim());
        filter.setTextSearch(textSearchFilterField.getText().trim());
        filter.setCorrelationId(correlationFilterField.getText().trim());
        filter.setTraceId(traceFilterField.getText().trim());
        filter.setRequestId(requestFilterField.getText().trim());
        filter.setComponent(componentFilterField.getText().trim());
        filter.setActorUserId(actorUserFilterField.getText().trim());
        filter.setActorPrincipal(actorPrincipalFilterField.getText().trim());
        filter.setSessionHash(sessionFilterField.getText().trim());
        filter.setClientIp(clientIpFilterField.getText().trim());
        filter.setServerIp(serverIpFilterField.getText().trim());
        filter.setOnlyWithPayload(onlyPayloadCheckBox.isSelected());
        filter.setOnlyWithCorrelation(onlyCorrelationCheckBox.isSelected());
        filter.setOnlyWithActor(onlyActorCheckBox.isSelected());
        return filter;
    }

    private void applySavedView(SavedViewDefinition definition)
    {
        EventQueryFilter filter = definition.getFilter();
        if (filter.getFromLocal() != null) fromSpinner.setValue(asDate(filter.getFromLocal()));
        if (filter.getToLocal() != null) toSpinner.setValue(asDate(filter.getToLocal()));
        sourceFilterField.setText(text(filter.getSourceKey())); hostnameFilterField.setText(text(filter.getHostname())); serviceFilterField.setText(text(filter.getService()));
        severityFilterField.setText(text(filter.getSeverity())); categoryFilterField.setText(text(filter.getEventCategory())); actionFilterField.setText(text(filter.getEventAction()));
        outcomeFilterField.setText(text(filter.getEventOutcome())); textSearchFilterField.setText(text(filter.getTextSearch())); correlationFilterField.setText(text(filter.getCorrelationId()));
        traceFilterField.setText(text(filter.getTraceId())); requestFilterField.setText(text(filter.getRequestId())); componentFilterField.setText(text(filter.getComponent()));
        actorUserFilterField.setText(text(filter.getActorUserId())); actorPrincipalFilterField.setText(text(filter.getActorPrincipal())); sessionFilterField.setText(text(filter.getSessionHash()));
        clientIpFilterField.setText(text(filter.getClientIp())); serverIpFilterField.setText(text(filter.getServerIp()));
        onlyPayloadCheckBox.setSelected(filter.isOnlyWithPayload()); onlyCorrelationCheckBox.setSelected(filter.isOnlyWithCorrelation()); onlyActorCheckBox.setSelected(filter.isOnlyWithActor());
        definition.getVisibleColumns().forEach((header, visible) -> {
            JCheckBox checkBox = columnVisibilityChecks.get(header);
            if (checkBox != null)
            {
                checkBox.setSelected(visible);
                setColumnVisible(header, visible);
            }
        });
        currentPage = 1;
        refreshEvents();
    }

    private void saveCurrentView()
    {
        String name = savedViewNameField.getText().trim();
        if (name.isBlank())
        {
            showWarning("Please enter a name for the saved view.");
            return;
        }

        SavedViewDefinition definition = new SavedViewDefinition();
        definition.setName(name);
        definition.setFilter(buildFilterFromControls());
        definition.setVisibleColumns(currentColumnVisibility());
        definition.setSortColumn("occurredAt");
        definition.setSortAscending(false);

        try
        {
            savedViewService.save(definition);
            loadSavedViews();
            statusMessageLabel.setText("Saved view stored.");
        }
        catch (Exception exception)
        {
            handleBackgroundError(exception);
        }
    }

    private void loadSelectedView()
    {
        SavedViewDefinition selected = (SavedViewDefinition) savedViewsCombo.getSelectedItem();
        if (selected != null)
        {
            applySavedView(selected);
        }
    }

    private void renameSelectedView()
    {
        SavedViewDefinition selected = (SavedViewDefinition) savedViewsCombo.getSelectedItem();
        String newName = savedViewNameField.getText().trim();
        if (selected == null || newName.isBlank())
        {
            showWarning("Please select a saved view and enter a new name.");
            return;
        }
        try
        {
            savedViewService.rename(selected.getName(), newName);
            loadSavedViews();
        }
        catch (Exception exception)
        {
            handleBackgroundError(exception);
        }
    }

    private void deleteSelectedView()
    {
        SavedViewDefinition selected = (SavedViewDefinition) savedViewsCombo.getSelectedItem();
        if (selected == null) return;
        try
        {
            savedViewService.delete(selected.getName());
            loadSavedViews();
        }
        catch (Exception exception)
        {
            handleBackgroundError(exception);
        }
    }

    private void loadSavedViews()
    {
        DefaultComboBoxModel<SavedViewDefinition> model = new DefaultComboBoxModel<>();
        for (SavedViewDefinition item : savedViewService.loadAll()) model.addElement(item);
        savedViewsCombo.setModel(model);
    }

    private void loadSourcesInBackground()
    {
        SwingWorkerRunner.run(
            apiClient::getSources,
            items -> {
                sources = items;
                statusMessageLabel.setText("Loaded " + items.size() + " sources.");
            },
            exception -> logger.warning("Sources could not be loaded", Map.of("message", exception.getMessage())));
    }

    private void loadRelatedEvents(String mode)
    {
        if (selectedEvent == null)
        {
            showWarning("Please select an event first.");
            return;
        }

        List<LogEventRecord> rows = switch (mode)
        {
            case "correlation" -> relatedEventsService.byCorrelation(currentPageRows, selectedEvent);
            case "trace" -> relatedEventsService.byTrace(currentPageRows, selectedEvent);
            case "request" -> relatedEventsService.byRequest(currentPageRows, selectedEvent);
            case "source" -> relatedEventsService.bySource(currentPageRows, selectedEvent);
            case "session" -> relatedEventsService.bySession(currentPageRows, selectedEvent);
            case "actor" -> relatedEventsService.byActor(currentPageRows, selectedEvent);
            default -> List.of();
        };
        relatedTableModel.setRows(rows);
        relatedTitleLabel.setText("Related events by " + mode + " (current page scope)");
    }

    private void openSendTestDialog()
    {
        SendTestLogDialog dialog = new SendTestLogDialog(this, sources, apiClient, logger);
        dialog.setVisible(true);
        if (dialog.isRefreshRequested())
        {
            refreshEvents();
        }
    }

    private void exportVisibleRows(boolean csv)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(Path.of(defaultExportFileName(csv)).toFile());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try
        {
            if (csv) exportService.exportCsv(visibleRows, chooser.getSelectedFile().toPath());
            else exportService.exportJson(visibleRows, chooser.getSelectedFile().toPath());
            statusMessageLabel.setText("Export finished.");
        }
        catch (Exception exception)
        {
            handleBackgroundError(exception);
        }
    }

    private String defaultExportFileName(boolean csv)
    {
        String suffix = csv ? ".csv" : ".json";
        return "mustelalog-export-" + java.time.LocalDateTime.now().toString().replace(':', '-').replace('T', '_') + suffix;
    }

    private void startAutoRefreshIfConfigured()
    {
        if (!settings.getUi().isAutoRefreshEnabled()) return;
        autoRefreshTimer = new Timer(Math.max(5, settings.getUi().getAutoRefreshSeconds()) * 1000, event -> refreshEvents());
        autoRefreshTimer.start();
    }

    private void handleBackgroundError(Throwable throwable)
    {
        logger.error("Desktop client operation failed", Map.of("message", throwable.getMessage()), throwable instanceof Exception ? (Exception) throwable : new RuntimeException(throwable));
        connectionStatusLabel.setText("Error");
        statusMessageLabel.setText(throwable.getMessage());
        JOptionPane.showMessageDialog(this,
            "Die Operation konnte nicht erfolgreich abgeschlossen werden.\n\n" + throwable.getMessage(),
            "MustelaLog Client",
            JOptionPane.ERROR_MESSAGE);
    }

    private JButton copyButton(String text, JTextField field)
    {
        JButton button = new JButton(text);
        button.addActionListener(event -> copyToClipboard(field.getText()));
        return button;
    }

    private JButton copyButton(String text, JTextArea area)
    {
        JButton button = new JButton(text);
        button.addActionListener(event -> copyToClipboard(area.getText()));
        return button;
    }

    private void copyToClipboard(String text)
    {
        try
        {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text == null ? "" : text), null);
            statusMessageLabel.setText("Value copied to clipboard.");
        }
        catch (Exception exception)
        {
            handleBackgroundError(exception);
        }
    }

    private JTextField readonlyField()
    {
        JTextField field = new JTextField();
        field.setEditable(false);
        return field;
    }

    private JTextArea readonlyArea(int rows)
    {
        JTextArea area = new JTextArea(rows, 30);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JLabel sectionTitle(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | java.awt.Font.BOLD, 16f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private Component labeledComponent(String labelText, Component component)
    {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private Component smallHint(String text)
    {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(area.getFont().deriveFont(11f));
        area.setForeground(new Color(100, 100, 100));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return area;
    }

    private Map<String, Boolean> currentColumnVisibility()
    {
        Map<String, Boolean> visibility = new LinkedHashMap<>();
        columnVisibilityChecks.forEach((header, checkBox) -> visibility.put(header, checkBox.isSelected()));
        return visibility;
    }

    private boolean matchesTextSearch(LogEventRecord row, String search)
    {
        if (!hasText(search)) return true;
        String haystack = String.join(" ", text(row.getMessageText()), text(row.getEventName()), text(row.getEventCategory()), text(row.getEventAction()), text(row.getActorPrincipal()));
        return haystack.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    private boolean containsIgnoreCase(String haystack, String needle)
    {
        if (!hasText(needle)) return true;
        return text(haystack).toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private boolean hasText(String value)
    {
        return value != null && !value.isBlank();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }

    private String text(String value)
    {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String buildIntegrityText(LogEventRecord item)
    {
        return "Canonical Hash: " + text(item.getCanonicalHashSha256()) + "\n"
            + "Previous Hash: " + text(item.getPreviousHashSha256()) + "\n"
            + "Source Signature: " + text(item.getSourceSignature()) + "\n"
            + "Signature Algorithm: " + text(item.getSignatureAlgorithm());
    }

    private String prettyJson(Object value)
    {
        if (value == null) return "";
        try { return SimpleJson.stringifyPretty(value); }
        catch (Exception ignored) { return String.valueOf(value); }
    }

    private String findVersionAndEnvironment(LogEventRecord item)
    {
        String version = "-";
        String environment = item.getEnvironmentCode();
        for (SourceSummary source : sources)
        {
            if (source.getSourceKey() != null && source.getSourceKey().equals(item.getSourceKey()))
            {
                version = source.getVersionText();
                if (!hasText(environment)) environment = source.getEnvironment();
                break;
            }
        }
        return text(version) + " / " + text(environment);
    }

    private String joinParts(String... parts)
    {
        List<String> nonEmpty = new ArrayList<>();
        for (String part : parts) if (hasText(part)) nonEmpty.add(part);
        return nonEmpty.isEmpty() ? "-" : String.join(" / ", nonEmpty);
    }

    private Date asDate(LocalDateTime localDateTime)
    {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime asLocalDateTime(Date date)
    {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private JSpinner createDateTimeSpinner()
    {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd HH:mm:ss"));
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return spinner;
    }

    private void showWarning(String text)
    {
        JOptionPane.showMessageDialog(this, text, "MustelaLog Client", JOptionPane.WARNING_MESSAGE);
    }

    private final class SeverityAwareRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected)
            {
                int modelRow = table.convertRowIndexToModel(row);
                String severity = eventTableModel.getRows().size() > modelRow ? eventTableModel.getRow(modelRow).getSeverityText() : "";
                component.setForeground(colorForSeverity(severity));
            }
            return component;
        }

        private Color colorForSeverity(String severity)
        {
            String normalized = severity == null ? "" : severity.toUpperCase(Locale.ROOT);
            if (normalized.contains("ERROR") || normalized.contains("CRITICAL") || normalized.contains("FATAL")) return new Color(160, 0, 0);
            if (normalized.contains("WARN")) return new Color(170, 90, 0);
            if (normalized.contains("DEBUG") || normalized.contains("TRACE")) return new Color(80, 80, 80);
            return new Color(20, 20, 20);
        }
    }
}
