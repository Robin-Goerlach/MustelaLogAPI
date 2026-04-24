package de.sasd.mustelalog.client.ui;

import de.sasd.mustelalog.client.json.SimpleJson;
import de.sasd.mustelalog.client.logging.ClientLogger;
import de.sasd.mustelalog.client.logging.DiagnosticsListener;
import de.sasd.mustelalog.client.logging.LogEntry;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Secondary window showing the in-memory diagnostics log.
 */
public final class DiagnosticsWindow extends JDialog implements DiagnosticsListener {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final JTextArea textArea = new JTextArea();

    public DiagnosticsWindow(MainFrame owner, ClientLogger logger) {
        super(owner, "Diagnostics", false);
        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(900, 500));

        textArea.setEditable(false);
        textArea.setLineWrap(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> reload(logger));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(event -> setVisible(false));

        JPanel footer = new JPanel();
        footer.add(refreshButton);
        footer.add(closeButton);
        add(footer, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        logger.addListener(this);
        reload(logger);
    }

    public void reload(ClientLogger logger) {
        StringBuilder builder = new StringBuilder();
        for (LogEntry entry : logger.snapshot()) {
            builder.append(FORMATTER.format(entry.timestamp()))
                    .append(" [")
                    .append(entry.level())
                    .append("] ")
                    .append(entry.message());
            if (!entry.context().isEmpty()) {
                builder.append(System.lineSeparator())
                        .append("  ")
                        .append(SimpleJson.pretty(entry.context()).replace(System.lineSeparator(), System.lineSeparator() + "  "));
            }
            builder.append(System.lineSeparator()).append(System.lineSeparator());
        }
        textArea.setText(builder.toString());
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    @Override
    public void onLogEntry(LogEntry entry) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(FORMATTER.format(entry.timestamp()) + " [" + entry.level() + "] " + entry.message() + System.lineSeparator());
            if (!entry.context().isEmpty()) {
                textArea.append("  " + SimpleJson.pretty(entry.context()).replace(System.lineSeparator(), System.lineSeparator() + "  ") + System.lineSeparator());
            }
            textArea.append(System.lineSeparator());
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
