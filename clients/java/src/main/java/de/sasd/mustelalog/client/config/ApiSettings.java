package de.sasd.mustelalog.client.config;

/**
 * API configuration of the Swing client.
 */
public final class ApiSettings {
    private String baseUrl = "";
    private String routeParameterName = "route";
    private String apiVersionPath = "/api/v1";
    private String readBearerToken = "";
    private String ingestBearerToken = "";
    private int timeoutSeconds = 20;
    private int defaultPageSize = 50;
    private boolean healthOnStartup = true;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getRouteParameterName() { return routeParameterName; }
    public void setRouteParameterName(String routeParameterName) { this.routeParameterName = routeParameterName; }
    public String getApiVersionPath() { return apiVersionPath; }
    public void setApiVersionPath(String apiVersionPath) { this.apiVersionPath = apiVersionPath; }
    public String getReadBearerToken() { return readBearerToken; }
    public void setReadBearerToken(String readBearerToken) { this.readBearerToken = readBearerToken; }
    public String getIngestBearerToken() { return ingestBearerToken; }
    public void setIngestBearerToken(String ingestBearerToken) { this.ingestBearerToken = ingestBearerToken; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public int getDefaultPageSize() { return defaultPageSize; }
    public void setDefaultPageSize(int defaultPageSize) { this.defaultPageSize = defaultPageSize; }
    public boolean isHealthOnStartup() { return healthOnStartup; }
    public void setHealthOnStartup(boolean healthOnStartup) { this.healthOnStartup = healthOnStartup; }
}
