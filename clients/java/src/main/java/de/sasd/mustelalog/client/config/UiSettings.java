package de.sasd.mustelalog.client.config;

public final class UiSettings {
    private boolean lookAndFeelSystem = true;
    private int windowWidth = 1440;
    private int windowHeight = 900;
    private boolean autoLoadSourcesOnStartup = true;
    private boolean autoLoadEventsOnStartup = true;

    public boolean isLookAndFeelSystem() { return lookAndFeelSystem; }
    public void setLookAndFeelSystem(boolean lookAndFeelSystem) { this.lookAndFeelSystem = lookAndFeelSystem; }
    public int getWindowWidth() { return windowWidth; }
    public void setWindowWidth(int windowWidth) { this.windowWidth = windowWidth; }
    public int getWindowHeight() { return windowHeight; }
    public void setWindowHeight(int windowHeight) { this.windowHeight = windowHeight; }
    public boolean isAutoLoadSourcesOnStartup() { return autoLoadSourcesOnStartup; }
    public void setAutoLoadSourcesOnStartup(boolean autoLoadSourcesOnStartup) { this.autoLoadSourcesOnStartup = autoLoadSourcesOnStartup; }
    public boolean isAutoLoadEventsOnStartup() { return autoLoadEventsOnStartup; }
    public void setAutoLoadEventsOnStartup(boolean autoLoadEventsOnStartup) { this.autoLoadEventsOnStartup = autoLoadEventsOnStartup; }
}
