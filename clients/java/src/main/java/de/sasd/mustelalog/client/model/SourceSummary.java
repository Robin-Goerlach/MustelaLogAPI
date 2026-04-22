package de.sasd.mustelalog.client.model;

/**
 * Lightweight source representation used by filters and the test-log dialog.
 */
public final class SourceSummary
{
    private final String sourceId;
    private final String sourceKey;
    private final String sourceName;
    private final String sourceType;
    private final String environment;
    private final String hostname;
    private final String serviceName;
    private final String versionText;
    private final boolean active;

    public SourceSummary(String sourceId, String sourceKey, String sourceName, String sourceType, String environment,
                         String hostname, String serviceName, String versionText, boolean active)
    {
        this.sourceId = sourceId;
        this.sourceKey = sourceKey;
        this.sourceName = sourceName;
        this.sourceType = sourceType;
        this.environment = environment;
        this.hostname = hostname;
        this.serviceName = serviceName;
        this.versionText = versionText;
        this.active = active;
    }

    public String getSourceId() { return sourceId; }
    public String getSourceKey() { return sourceKey; }
    public String getSourceName() { return sourceName; }
    public String getSourceType() { return sourceType; }
    public String getEnvironment() { return environment; }
    public String getHostname() { return hostname; }
    public String getServiceName() { return serviceName; }
    public String getVersionText() { return versionText; }
    public boolean isActive() { return active; }

    @Override
    public String toString()
    {
        return sourceName != null && !sourceName.isBlank() ? sourceName + " (" + sourceKey + ")" : sourceKey;
    }
}
