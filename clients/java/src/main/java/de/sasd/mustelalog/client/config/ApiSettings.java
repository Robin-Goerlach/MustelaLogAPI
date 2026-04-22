package de.sasd.mustelalog.client.config;

/**
 * API-specific settings.
 */
public final class ApiSettings
{
    private String baseUrl = "";
    private String routeParameterName = "route";
    private String apiVersionPath = "/api/v1";
    private String technicalAccessToken = "";
    private int timeoutSeconds = 20;
    private int defaultPageSize = 50;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getRouteParameterName() { return routeParameterName; }
    public void setRouteParameterName(String routeParameterName) { this.routeParameterName = routeParameterName; }
    public String getApiVersionPath() { return apiVersionPath; }
    public void setApiVersionPath(String apiVersionPath) { this.apiVersionPath = apiVersionPath; }
    public String getTechnicalAccessToken() { return technicalAccessToken; }
    public void setTechnicalAccessToken(String technicalAccessToken) { this.technicalAccessToken = technicalAccessToken; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public int getDefaultPageSize() { return defaultPageSize; }
    public void setDefaultPageSize(int defaultPageSize) { this.defaultPageSize = defaultPageSize; }
}
