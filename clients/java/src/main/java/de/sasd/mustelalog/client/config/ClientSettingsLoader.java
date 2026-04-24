package de.sasd.mustelalog.client.config;

import de.sasd.mustelalog.client.json.JsonSupport;
import de.sasd.mustelalog.client.json.SimpleJson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class ClientSettingsLoader {
    private ClientSettingsLoader() {
    }

    public static AppSettings load(Path path) throws SettingsValidationException {
        try {
            if (!Files.exists(path)) {
                throw new SettingsValidationException("Settings file not found: " + path.toAbsolutePath());
            }
            Object parsed = SimpleJson.parse(Files.readString(path, StandardCharsets.UTF_8));
            Map<String, Object> root = JsonSupport.asObject(parsed, "Root JSON document must be an object.");
            AppSettings settings = new AppSettings();

            Map<String, Object> api = JsonSupport.getObject(root, "api");
            if (api != null) {
                settings.getApi().setBaseUrl(JsonSupport.getString(api, "baseUrl", settings.getApi().getBaseUrl()));
                settings.getApi().setRouteParameterName(JsonSupport.getString(api, "routeParameterName", settings.getApi().getRouteParameterName()));
                settings.getApi().setApiVersionPath(JsonSupport.getString(api, "apiVersionPath", settings.getApi().getApiVersionPath()));
                settings.getApi().setReadBearerToken(JsonSupport.getString(api, "readBearerToken", settings.getApi().getReadBearerToken()));
                settings.getApi().setIngestBearerToken(JsonSupport.getString(api, "ingestBearerToken", settings.getApi().getIngestBearerToken()));
                settings.getApi().setTimeoutSeconds(JsonSupport.getInt(api, "timeoutSeconds", settings.getApi().getTimeoutSeconds()));
                settings.getApi().setDefaultPageSize(JsonSupport.getInt(api, "defaultPageSize", settings.getApi().getDefaultPageSize()));
                settings.getApi().setHealthOnStartup(JsonSupport.getBoolean(api, "healthOnStartup", settings.getApi().isHealthOnStartup()));
            }

            Map<String, Object> diagnostics = JsonSupport.getObject(root, "diagnostics");
            if (diagnostics != null) {
                settings.getDiagnostics().setLogFilePath(JsonSupport.getString(diagnostics, "logFilePath", settings.getDiagnostics().getLogFilePath()));
                settings.getDiagnostics().setMaxEntriesInMemory(JsonSupport.getInt(diagnostics, "maxEntriesInMemory", settings.getDiagnostics().getMaxEntriesInMemory()));
            }

            Map<String, Object> export = JsonSupport.getObject(root, "export");
            if (export != null) {
                settings.getExport().setDefaultDirectory(JsonSupport.getString(export, "defaultDirectory", settings.getExport().getDefaultDirectory()));
            }

            Map<String, Object> ui = JsonSupport.getObject(root, "ui");
            if (ui != null) {
                settings.getUi().setLookAndFeelSystem(JsonSupport.getBoolean(ui, "lookAndFeelSystem", settings.getUi().isLookAndFeelSystem()));
                settings.getUi().setWindowWidth(JsonSupport.getInt(ui, "windowWidth", settings.getUi().getWindowWidth()));
                settings.getUi().setWindowHeight(JsonSupport.getInt(ui, "windowHeight", settings.getUi().getWindowHeight()));
                settings.getUi().setAutoLoadSourcesOnStartup(JsonSupport.getBoolean(ui, "autoLoadSourcesOnStartup", settings.getUi().isAutoLoadSourcesOnStartup()));
                settings.getUi().setAutoLoadEventsOnStartup(JsonSupport.getBoolean(ui, "autoLoadEventsOnStartup", settings.getUi().isAutoLoadEventsOnStartup()));
            }

            validate(settings);
            return settings;
        } catch (IOException exception) {
            throw new SettingsValidationException("Settings file could not be read.", exception);
        } catch (RuntimeException exception) {
            throw new SettingsValidationException("Settings file could not be parsed: " + exception.getMessage(), exception);
        }
    }

    private static void validate(AppSettings settings) throws SettingsValidationException {
        if (settings.getApi().getBaseUrl() == null || settings.getApi().getBaseUrl().isBlank()) {
            throw new SettingsValidationException("api.baseUrl must not be empty.");
        }
        if (settings.getApi().getRouteParameterName() == null || settings.getApi().getRouteParameterName().isBlank()) {
            throw new SettingsValidationException("api.routeParameterName must not be empty.");
        }
        if (settings.getApi().getApiVersionPath() == null || settings.getApi().getApiVersionPath().isBlank()) {
            throw new SettingsValidationException("api.apiVersionPath must not be empty.");
        }
        if (!settings.getApi().getApiVersionPath().startsWith("/")) {
            throw new SettingsValidationException("api.apiVersionPath must start with '/'.");
        }
        if (settings.getApi().getDefaultPageSize() < 1 || settings.getApi().getDefaultPageSize() > 200) {
            throw new SettingsValidationException("api.defaultPageSize must be between 1 and 200.");
        }
        if (settings.getApi().getTimeoutSeconds() < 5) {
            throw new SettingsValidationException("api.timeoutSeconds must be at least 5.");
        }
        if (settings.getDiagnostics().getMaxEntriesInMemory() < 100) {
            throw new SettingsValidationException("diagnostics.maxEntriesInMemory must be at least 100.");
        }
    }
}
