package de.sasd.mustelalog.client.config;

/**
 * Diagnostics logging settings.
 */
public final class DiagnosticsSettings
{
    private boolean enabled = true;
    private String minimumLevel = "INFORMATION";
    private String logFilePath = "${user.home}/.mustelalog-client/logs/client.log";
    private long maxFileSizeBytes = 1024L * 1024L;
    private int maxRetainedFiles = 5;
    private int inMemoryBufferSize = 500;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getMinimumLevel() { return minimumLevel; }
    public void setMinimumLevel(String minimumLevel) { this.minimumLevel = minimumLevel; }
    public String getLogFilePath() { return logFilePath; }
    public void setLogFilePath(String logFilePath) { this.logFilePath = logFilePath; }
    public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
    public void setMaxFileSizeBytes(long maxFileSizeBytes) { this.maxFileSizeBytes = maxFileSizeBytes; }
    public int getMaxRetainedFiles() { return maxRetainedFiles; }
    public void setMaxRetainedFiles(int maxRetainedFiles) { this.maxRetainedFiles = maxRetainedFiles; }
    public int getInMemoryBufferSize() { return inMemoryBufferSize; }
    public void setInMemoryBufferSize(int inMemoryBufferSize) { this.inMemoryBufferSize = inMemoryBufferSize; }
}
