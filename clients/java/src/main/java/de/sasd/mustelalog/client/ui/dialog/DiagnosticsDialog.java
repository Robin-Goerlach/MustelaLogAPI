package de.sasd.mustelalog.client.ui.dialog;

import de.sasd.mustelalog.client.logging.LogEntry;
import de.sasd.mustelalog.client.logging.LogListener;
import de.sasd.mustelalog.client.logging.MemoryClientLogger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.FlowLayout;

/**
 * Simple live diagnostics dialog backed by the in-memory logger.
 */
public final class DiagnosticsDialog extends JDialog
{
    private final MemoryClientLogger memoryLogger;
    private final JTextArea textArea = new JTextArea();
    private final LogListener listener;

    public DiagnosticsDialog(Frame owner, MemoryClientLogger memoryLogger)
    {
        super(owner, "Client Diagnostics", false);
        this.memoryLogger = memoryLogger;
        this.listener = this::appendEntry;

        setSize(900, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton copyButton = new JButton("Copy All");
        copyButton.addActionListener(event -> textArea.selectAll());
        JButton clearButton = new JButton("Clear Buffer");
        clearButton.addActionListener(event -> { memoryLogger.clear(); textArea.setText(""); });
        buttons.add(copyButton);
        buttons.add(clearButton);
        add(buttons, BorderLayout.SOUTH);

        memoryLogger.addListener(listener);
        for (LogEntry entry : memoryLogger.snapshot()) appendEntry(entry);
    }

    @Override
    public void dispose()
    {
        memoryLogger.removeListener(listener);
        super.dispose();
    }

    private void appendEntry(LogEntry entry)
    {
        textArea.append(entry.getTimestamp() + " [" + entry.getLevel() + "] " + entry.getMessage() + System.lineSeparator());
    }
}
