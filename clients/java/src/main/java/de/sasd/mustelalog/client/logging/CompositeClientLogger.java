package de.sasd.mustelalog.client.logging;

import java.util.Arrays;
import java.util.List;

/**
 * Fan-out logger that writes each entry to several underlying loggers.
 */
public final class CompositeClientLogger implements ClientLogger
{
    private final List<ClientLogger> delegates;

    public CompositeClientLogger(ClientLogger... delegates)
    {
        this.delegates = Arrays.stream(delegates).filter(item -> item != null).toList();
    }

    @Override
    public void log(LogEntry entry)
    {
        for (ClientLogger delegate : delegates)
        {
            try
            {
                delegate.log(entry);
            }
            catch (Exception ignored)
            {
                // Diagnostics logging must remain side-effect-safe.
            }
        }
    }
}
