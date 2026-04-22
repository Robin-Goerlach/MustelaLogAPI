package de.sasd.mustelalog.client.ui.dialog;

import de.sasd.mustelalog.client.api.MustelaLogApiClient;
import de.sasd.mustelalog.client.logging.ClientLogger;
import de.sasd.mustelalog.client.model.SourceSummary;
import de.sasd.mustelalog.client.model.TestLogEventRequest;
import de.sasd.mustelalog.client.util.SwingWorkerRunner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;

/**
 * Dialog used to send a test log event to the API.
 */
public final class SendTestLogDialog extends JDialog
{
    private final MustelaLogApiClient apiClient;
    private final ClientLogger logger;
    private boolean refreshRequested;

    private final JComboBox<String> severityCombo = new JComboBox<>(new String[] {"TRACE", "DEBUG", "INFORMATION", "WARNING", "ERROR", "CRITICAL"});
    private final JTextField severityNumberField = new JTextField("9");
    private final JTextField messageField = new JTextField("Desktop client test event");
    private final JTextField eventNameField = new JTextField("desktop.client.test");
    private final JTextField categoryField = new JTextField("diagnostics");
    private final JTextField actionField = new JTextField("manual-test");
    private final JComboBox<SourceSummary> sourceCombo = new JComboBox<>();
    private final JTextField correlationField = new JTextField();
    private final JTextArea attributesArea = new JTextArea("{\n  \"client\": \"MustelaLog Swing Client\"\n}");

    public SendTestLogDialog(Frame owner, List<SourceSummary> sources, MustelaLogApiClient apiClient, ClientLogger logger)
    {
        super(owner, "Send test log event", true);
        this.apiClient = apiClient;
        this.logger = logger;

        setSize(650, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        for (SourceSummary source : sources) sourceCombo.addItem(source);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        int row = 0;
        addField(form, gbc, row++, "Severity", severityCombo);
        addField(form, gbc, row++, "Severity Number", severityNumberField);
        addField(form, gbc, row++, "Message", messageField);
        addField(form, gbc, row++, "Event Name", eventNameField);
        addField(form, gbc, row++, "Category", categoryField);
        addField(form, gbc, row++, "Action", actionField);
        addField(form, gbc, row++, "Source", sourceCombo);
        addField(form, gbc, row++, "Correlation ID", correlationField);

        attributesArea.setLineWrap(true);
        attributesArea.setWrapStyleWord(true);
        JScrollPane attributesPane = new JScrollPane(attributesArea);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; form.add(new JLabel("Attributes JSON"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1; form.add(attributesPane, gbc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(event -> sendRequest());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dispose());
        buttons.add(sendButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);
    }

    public boolean isRefreshRequested()
    {
        return refreshRequested;
    }

    private void sendRequest()
    {
        TestLogEventRequest request = new TestLogEventRequest();
        request.setSeverityText(String.valueOf(severityCombo.getSelectedItem()));
        request.setSeverityNumber(Integer.parseInt(severityNumberField.getText().trim()));
        request.setMessage(messageField.getText().trim());
        request.setEventName(eventNameField.getText().trim());
        request.setEventCategory(categoryField.getText().trim());
        request.setEventAction(actionField.getText().trim());
        SourceSummary selectedSource = (SourceSummary) sourceCombo.getSelectedItem();
        request.setSourceKey(selectedSource == null ? "" : selectedSource.getSourceKey());
        request.setCorrelationId(correlationField.getText().trim());
        request.setAttributesJsonText(attributesArea.getText());

        SwingWorkerRunner.run(
            () -> {
                apiClient.sendTestLogEvent(request);
                return Boolean.TRUE;
            },
            ignored -> {
                logger.information("Test log event sent", Map.of("eventName", request.getEventName(), "severity", request.getSeverityText()));
                refreshRequested = true;
                JOptionPane.showMessageDialog(this, "Test event sent successfully.", "MustelaLog Client", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            },
            error -> JOptionPane.showMessageDialog(this, error.getMessage(), "MustelaLog Client", JOptionPane.ERROR_MESSAGE));
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component component)
    {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(component, gbc);
    }
}
