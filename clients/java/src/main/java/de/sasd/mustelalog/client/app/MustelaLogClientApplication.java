package de.sasd.mustelalog.client.app;

import de.sasd.mustelalog.client.api.MustelaLogApiClient;
import de.sasd.mustelalog.client.auth.StaticCredentialProvider;
import de.sasd.mustelalog.client.config.AppSettings;
import de.sasd.mustelalog.client.config.ClientSettingsLoader;
import de.sasd.mustelalog.client.config.SettingsValidationException;
import de.sasd.mustelalog.client.logging.ClientLogger;
import de.sasd.mustelalog.client.service.EventAggregationService;
import de.sasd.mustelalog.client.service.EventExportService;
import de.sasd.mustelalog.client.service.TimeService;
import de.sasd.mustelalog.client.ui.MainFrame;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Bootstraps the rebuilt Swing client.
 */
public final class MustelaLogClientApplication {
    private MustelaLogClientApplication() {
    }

    public static void main(String[] args) {
        Path settingsPath = Path.of(args.length > 0 ? args[0] : "client-settings.json");
        try {
            AppSettings settings = ClientSettingsLoader.load(settingsPath);
            if (settings.getUi().isLookAndFeelSystem()) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }

            ClientLogger logger = new ClientLogger(
                    Path.of(settings.getDiagnostics().getLogFilePath()),
                    settings.getDiagnostics().getMaxEntriesInMemory());

            logger.information("Client startup", java.util.Map.of(
                    "settingsFile", settingsPath.toAbsolutePath().toString(),
                    "javaVersion", System.getProperty("java.version", "unknown")));

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(Math.max(5, settings.getApi().getTimeoutSeconds())))
                    .build();

            StaticCredentialProvider credentialProvider = new StaticCredentialProvider(
                    settings.getApi().getReadBearerToken(),
                    settings.getApi().getIngestBearerToken());

            TimeService timeService = new TimeService();
            MustelaLogApiClient apiClient = new MustelaLogApiClient(
                    httpClient,
                    settings.getApi(),
                    credentialProvider,
                    logger,
                    timeService);

            EventAggregationService aggregationService = new EventAggregationService();
            EventExportService exportService = new EventExportService(timeService);

            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame(settings, apiClient, logger, timeService, aggregationService, exportService);
                frame.setVisible(true);
                frame.startup();
            });
        } catch (SettingsValidationException exception) {
            System.err.println("Configuration error: " + exception.getMessage());
            exception.printStackTrace(System.err);
            System.exit(2);
        } catch (Exception exception) {
            System.err.println("Application startup failed: " + exception.getMessage());
            exception.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
