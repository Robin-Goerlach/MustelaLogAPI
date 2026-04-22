package de.sasd.mustelalog.client.app;

import de.sasd.mustelalog.client.api.MustelaLogApiClient;
import de.sasd.mustelalog.client.auth.StaticTokenCredentialProvider;
import de.sasd.mustelalog.client.config.ClientSettings;
import de.sasd.mustelalog.client.config.ClientSettingsLoader;
import de.sasd.mustelalog.client.logging.ClientLogLevel;
import de.sasd.mustelalog.client.logging.ClientLogger;
import de.sasd.mustelalog.client.logging.CompositeClientLogger;
import de.sasd.mustelalog.client.logging.FileClientLogger;
import de.sasd.mustelalog.client.logging.MemoryClientLogger;
import de.sasd.mustelalog.client.service.AggregationService;
import de.sasd.mustelalog.client.service.ExportService;
import de.sasd.mustelalog.client.service.RelatedEventsService;
import de.sasd.mustelalog.client.service.SavedViewService;
import de.sasd.mustelalog.client.service.TimeDisplayService;
import de.sasd.mustelalog.client.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * Application entry point for the Swing client.
 *
 * <p>The startup code is intentionally explicit and lightweight. For V1 the project does not need a
 * dependency injection framework. It is easier to understand when the core services are assembled in
 * one place.</p>
 */
public final class Main
{
    private Main() {}

    /**
     * Starts the Swing client.
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            try
            {
                start();
            }
            catch (Exception exception)
            {
                JOptionPane.showMessageDialog(
                    null,
                    "Der Client konnte nicht gestartet werden.\n\n" + exception.getClass().getSimpleName() + ": " + exception.getMessage(),
                    "MustelaLog Swing Client",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void start() throws Exception
    {
        Path applicationRoot = Path.of(System.getProperty("user.dir"));
        Path settingsPath = applicationRoot.resolve("client-settings.json");
        Path examplePath = applicationRoot.resolve("client-settings.example.json");

        ClientSettingsLoader loader = new ClientSettingsLoader();
        loader.ensureExampleExists(examplePath, Files.readString(applicationRoot.resolve("client-settings.example.json")));
        if (!Files.exists(settingsPath))
        {
            Files.copy(examplePath, settingsPath);
        }

        ClientSettings settings = loader.load(settingsPath);
        ClientLogLevel minimumLevel = parseLogLevel(settings.getDiagnostics().getMinimumLevel());
        MemoryClientLogger memoryLogger = new MemoryClientLogger(settings.getDiagnostics().getInMemoryBufferSize(), minimumLevel);
        ClientLogger logger = settings.getDiagnostics().isEnabled()
            ? new CompositeClientLogger(
                memoryLogger,
                new FileClientLogger(
                    Path.of(settings.getDiagnostics().getLogFilePath()),
                    settings.getDiagnostics().getMaxFileSizeBytes(),
                    settings.getDiagnostics().getMaxRetainedFiles(),
                    minimumLevel))
            : memoryLogger;

        logger.information("Desktop client starting", Map.of(
            "baseUrl", settings.getApi().getBaseUrl(),
            "apiVersionPath", settings.getApi().getApiVersionPath(),
            "timeMode", settings.getUi().getDefaultTimeMode().name(),
            "diagnosticsEnabled", settings.getDiagnostics().isEnabled()));

        TimeDisplayService timeDisplayService = new TimeDisplayService();
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(Math.max(5, settings.getApi().getTimeoutSeconds())))
            .build();

        MustelaLogApiClient apiClient = new MustelaLogApiClient(
            httpClient,
            settings.getApi(),
            new StaticTokenCredentialProvider(settings.getApi().getTechnicalAccessToken()),
            logger,
            timeDisplayService);

        MainFrame mainFrame = new MainFrame(
            settings,
            apiClient,
            new AggregationService(timeDisplayService),
            new ExportService(),
            new SavedViewService(Path.of(System.getProperty("user.home"), ".mustelalog-client", "saved-views.json")),
            new RelatedEventsService(),
            timeDisplayService,
            logger,
            memoryLogger);

        mainFrame.setVisible(true);
    }

    private static ClientLogLevel parseLogLevel(String value)
    {
        try
        {
            return ClientLogLevel.valueOf(value.toUpperCase());
        }
        catch (Exception ignored)
        {
            return ClientLogLevel.INFORMATION;
        }
    }
}
