package de.sasd.mustelalog.client.config;

import de.sasd.mustelalog.client.json.SimpleJson;
import de.sasd.mustelalog.client.model.TimeMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Loads the client settings from a local JSON file.
 */
public final class ClientSettingsLoader
{
    public void ensureExampleExists(Path examplePath, String exampleJson) throws IOException
    {
        if (Files.exists(examplePath)) return;
        Files.createDirectories(examplePath.getParent());
        Files.writeString(examplePath, exampleJson, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    public ClientSettings load(Path settingsPath) throws IOException
    {
        if (!Files.exists(settingsPath)) throw new IOException("Settings file not found: " + settingsPath);
        Object parsed = SimpleJson.parse(Files.readString(settingsPath, StandardCharsets.UTF_8));
        if (!(parsed instanceof Map<?, ?> root)) throw new IOException("Root JSON must be an object.");

        ClientSettings settings = new ClientSettings();
        Map<String, Object> rootMap = (Map<String, Object>) root;

        Object apiValue = rootMap.get("api");
        if (apiValue instanceof Map<?, ?> apiMap)
        {
            settings.getApi().setBaseUrl(asString(apiMap.get("baseUrl"), settings.getApi().getBaseUrl()));
            settings.getApi().setRouteParameterName(asString(apiMap.get("routeParameterName"), settings.getApi().getRouteParameterName()));
            settings.getApi().setApiVersionPath(asString(apiMap.get("apiVersionPath"), settings.getApi().getApiVersionPath()));
            settings.getApi().setTechnicalAccessToken(asString(apiMap.get("technicalAccessToken"), settings.getApi().getTechnicalAccessToken()));
            settings.getApi().setTimeoutSeconds(asInt(apiMap.get("timeoutSeconds"), settings.getApi().getTimeoutSeconds()));
            settings.getApi().setDefaultPageSize(asInt(apiMap.get("defaultPageSize"), settings.getApi().getDefaultPageSize()));
        }

        Object uiValue = rootMap.get("ui");
        if (uiValue instanceof Map<?, ?> uiMap)
        {
            settings.getUi().setDefaultTimeMode(parseTimeMode(asString(uiMap.get("defaultTimeMode"), settings.getUi().getDefaultTimeMode().name())));
            settings.getUi().setAutoRefreshEnabled(asBoolean(uiMap.get("autoRefreshEnabled"), settings.getUi().isAutoRefreshEnabled()));
            settings.getUi().setAutoRefreshSeconds(asInt(uiMap.get("autoRefreshSeconds"), settings.getUi().getAutoRefreshSeconds()));
            settings.getUi().setDefaultWindowWidth(asInt(uiMap.get("defaultWindowWidth"), settings.getUi().getDefaultWindowWidth()));
            settings.getUi().setDefaultWindowHeight(asInt(uiMap.get("defaultWindowHeight"), settings.getUi().getDefaultWindowHeight()));
        }

        Object diagnosticsValue = rootMap.get("diagnostics");
        if (diagnosticsValue instanceof Map<?, ?> diagnosticsMap)
        {
            settings.getDiagnostics().setEnabled(asBoolean(diagnosticsMap.get("enabled"), settings.getDiagnostics().isEnabled()));
            settings.getDiagnostics().setMinimumLevel(asString(diagnosticsMap.get("minimumLevel"), settings.getDiagnostics().getMinimumLevel()));
            settings.getDiagnostics().setLogFilePath(resolveSpecialPath(asString(diagnosticsMap.get("logFilePath"), settings.getDiagnostics().getLogFilePath())));
            settings.getDiagnostics().setMaxFileSizeBytes(asLong(diagnosticsMap.get("maxFileSizeBytes"), settings.getDiagnostics().getMaxFileSizeBytes()));
            settings.getDiagnostics().setMaxRetainedFiles(asInt(diagnosticsMap.get("maxRetainedFiles"), settings.getDiagnostics().getMaxRetainedFiles()));
            settings.getDiagnostics().setInMemoryBufferSize(asInt(diagnosticsMap.get("inMemoryBufferSize"), settings.getDiagnostics().getInMemoryBufferSize()));
        }

        return settings;
    }

    private String resolveSpecialPath(String input) { return input.replace("${user.home}", System.getProperty("user.home")); }
    private static String asString(Object value, String fallback) { return value == null ? fallback : String.valueOf(value); }
    private static int asInt(Object value, int fallback) { try { return value instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return fallback; } }
    private static long asLong(Object value, long fallback) { try { return value instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(value)); } catch (Exception e) { return fallback; } }
    private static boolean asBoolean(Object value, boolean fallback) { return value == null ? fallback : (value instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(value))); }
    private static TimeMode parseTimeMode(String input) { try { return TimeMode.valueOf(input.toUpperCase()); } catch (Exception e) { return TimeMode.LOCAL; } }
}
