package de.sasd.mustelalog.client.config;

public final class DiagnosticsSettings {
    private String logFilePath = "logs/mustralla-java-client.log";
    private int maxEntriesInMemory = 1000;

    public String getLogFilePath() { return logFilePath; }
    public void setLogFilePath(String logFilePath) { this.logFilePath = logFilePath; }
    public int getMaxEntriesInMemory() { return maxEntriesInMemory; }
    public void setMaxEntriesInMemory(int maxEntriesInMemory) { this.maxEntriesInMemory = maxEntriesInMemory; }
}
