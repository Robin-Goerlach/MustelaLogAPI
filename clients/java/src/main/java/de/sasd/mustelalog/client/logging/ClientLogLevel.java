package de.sasd.mustelalog.client.logging;

/**
 * Simple log level model for the client-side diagnostics logging.
 *
 * <p>The order is intentional. A higher ordinal means a more verbose level.</p>
 */
public enum ClientLogLevel
{
    ERROR,
    WARNING,
    INFORMATION,
    DEBUG,
    TRACE;

    /**
     * Returns whether the current level should be written when the configured minimum level is used.
     *
     * @param minimumLevel the configured minimum level
     * @return {@code true} if the entry should be written
     */
    public boolean isEnabledFor(ClientLogLevel minimumLevel)
    {
        return this.ordinal() <= minimumLevel.ordinal();
    }
}
