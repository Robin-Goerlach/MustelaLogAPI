package de.sasd.mustelalog.client.tests;

import de.sasd.mustelalog.client.config.AppSettings;
import de.sasd.mustelalog.client.config.ClientSettingsLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SettingsLoaderSelfTest {
    private SettingsLoaderSelfTest() {
    }

    public static void run() throws Exception {
        Path file = Files.createTempFile("mustela-client-settings", ".json");
        String json = """
                {
                  "api": {
                    "baseUrl": "https://example.com/index.php",
                    "routeParameterName": "route",
                    "apiVersionPath": "/api/v1",
                    "readBearerToken": "reader-token",
                    "ingestBearerToken": "source-token",
                    "timeoutSeconds": 15,
                    "defaultPageSize": 50,
                    "healthOnStartup": true
                  },
                  "diagnostics": {
                    "logFilePath": "logs/test.log",
                    "maxEntriesInMemory": 1000
                  },
                  "export": {
                    "defaultDirectory": "exports"
                  },
                  "ui": {
                    "lookAndFeelSystem": true,
                    "windowWidth": 1280,
                    "windowHeight": 800,
                    "autoLoadSourcesOnStartup": false,
                    "autoLoadEventsOnStartup": false
                  }
                }
                """;
        Files.writeString(file, json, StandardCharsets.UTF_8);
        AppSettings settings = ClientSettingsLoader.load(file);
        if (!"https://example.com/index.php".equals(settings.getApi().getBaseUrl())) {
            throw new IllegalStateException("baseUrl not loaded correctly.");
        }
        if (!"reader-token".equals(settings.getApi().getReadBearerToken())) {
            throw new IllegalStateException("read token missing.");
        }
        if (!"source-token".equals(settings.getApi().getIngestBearerToken())) {
            throw new IllegalStateException("ingest token missing.");
        }
        if (settings.getUi().isAutoLoadEventsOnStartup()) {
            throw new IllegalStateException("UI flag should have been loaded as false.");
        }
    }
}
