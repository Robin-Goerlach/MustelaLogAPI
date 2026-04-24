package de.sasd.mustelalog.client.config;

public final class AppSettings {
    private final ApiSettings api = new ApiSettings();
    private final DiagnosticsSettings diagnostics = new DiagnosticsSettings();
    private final ExportSettings export = new ExportSettings();
    private final UiSettings ui = new UiSettings();

    public ApiSettings getApi() {
        return api;
    }

    public DiagnosticsSettings getDiagnostics() {
        return diagnostics;
    }

    public ExportSettings getExport() {
        return export;
    }

    public UiSettings getUi() {
        return ui;
    }
}
