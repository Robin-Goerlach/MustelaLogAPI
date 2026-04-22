package de.sasd.mustelalog.client.config;

/**
 * Root configuration model for the desktop client.
 */
public final class ClientSettings
{
    private ApiSettings api = new ApiSettings();
    private UiSettings ui = new UiSettings();
    private DiagnosticsSettings diagnostics = new DiagnosticsSettings();

    public ApiSettings getApi() { return api; }
    public void setApi(ApiSettings api) { this.api = api; }
    public UiSettings getUi() { return ui; }
    public void setUi(UiSettings ui) { this.ui = ui; }
    public DiagnosticsSettings getDiagnostics() { return diagnostics; }
    public void setDiagnostics(DiagnosticsSettings diagnostics) { this.diagnostics = diagnostics; }
}
