package de.sasd.mustelalog.client.config;

import de.sasd.mustelalog.client.model.TimeMode;

/**
 * UI-related settings.
 */
public final class UiSettings
{
    private TimeMode defaultTimeMode = TimeMode.LOCAL;
    private boolean autoRefreshEnabled;
    private int autoRefreshSeconds = 30;
    private int defaultWindowWidth = 1600;
    private int defaultWindowHeight = 950;

    public TimeMode getDefaultTimeMode() { return defaultTimeMode; }
    public void setDefaultTimeMode(TimeMode defaultTimeMode) { this.defaultTimeMode = defaultTimeMode; }
    public boolean isAutoRefreshEnabled() { return autoRefreshEnabled; }
    public void setAutoRefreshEnabled(boolean autoRefreshEnabled) { this.autoRefreshEnabled = autoRefreshEnabled; }
    public int getAutoRefreshSeconds() { return autoRefreshSeconds; }
    public void setAutoRefreshSeconds(int autoRefreshSeconds) { this.autoRefreshSeconds = autoRefreshSeconds; }
    public int getDefaultWindowWidth() { return defaultWindowWidth; }
    public void setDefaultWindowWidth(int defaultWindowWidth) { this.defaultWindowWidth = defaultWindowWidth; }
    public int getDefaultWindowHeight() { return defaultWindowHeight; }
    public void setDefaultWindowHeight(int defaultWindowHeight) { this.defaultWindowHeight = defaultWindowHeight; }
}
