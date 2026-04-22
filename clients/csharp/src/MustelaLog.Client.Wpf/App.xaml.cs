using System.IO;
using System.Net.Http;
using System.Windows;
using MustelaLog.Client.Core.Abstractions;
using MustelaLog.Client.Core.Configuration;
using MustelaLog.Client.Core.Diagnostics;
using MustelaLog.Client.Core.Enums;
using MustelaLog.Client.Core.Services;
using MustelaLog.Client.Wpf.ViewModels;
using MustelaLog.Client.Wpf.Views;

namespace MustelaLog.Client.Wpf;

/// <summary>
/// WPF-Anwendungseinstieg.
/// 
/// Die Startlogik baut die wenigen V1-Services bewusst direkt zusammen.
/// Ein größerer DI-Container wäre hier unnötige Komplexität.
/// </summary>
public partial class App : Application
{
    private CompositeAppLogger? _logger;

    /// <summary>
    /// Initializes the application, loads the client configuration,
    /// configures diagnostics logging, and creates the main window.
    /// </summary>
    /// <param name="e">
    /// Contains the startup arguments and startup context provided by WPF.
    /// </param>
    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);

        try
        {
            var settingsPath = Path.Combine(AppContext.BaseDirectory, "clientsettings.json");
            var exampleSettingsPath = Path.Combine(AppContext.BaseDirectory, "clientsettings.json.example");

            var settingsService = new ClientSettingsService();
            settingsService.EnsureExampleExists(exampleSettingsPath, new ClientSettings());
            var settings = settingsService.Load(settingsPath);

            var minimumLevel = ParseMinimumLevel(settings.Diagnostics.MinimumLevel);
            var inMemoryLogger = new InMemoryAppLogger(settings.Diagnostics.InMemoryBufferSize, minimumLevel);
            var loggers = new List<IAppLogger> { inMemoryLogger };
            if (settings.Diagnostics.Enabled)
            {
                loggers.Add(new FileAppLogger(
                    settings.Diagnostics.FilePath,
                    settings.Diagnostics.MaxFileSizeBytes,
                    settings.Diagnostics.MaxRetainedFiles,
                    minimumLevel));
            }

            _logger = new CompositeAppLogger(loggers.ToArray());
            _logger.Information("Desktop client starting", new Dictionary<string, object?>
            {
                ["baseUrl"] = settings.Api.BaseUrl,
                ["apiVersion"] = settings.Api.ApiVersion,
                ["defaultPageSize"] = settings.Api.DefaultPageSize,
                ["timeMode"] = settings.Ui.DefaultTimeMode,
                ["diagnosticsEnabled"] = settings.Diagnostics.Enabled,
                ["minimumLevel"] = minimumLevel.ToString()
            });

            var timeDisplayService = new TimeDisplayService();
            var credentialProvider = new StaticTokenCredentialProvider(settings.Api.TechnicalAccessToken);

            var httpClient = new HttpClient
            {
                Timeout = TimeSpan.FromSeconds(Math.Max(5, settings.Api.TimeoutSeconds))
            };

            var apiClient = new LogApiClient(httpClient, settings.Api, credentialProvider, _logger, timeDisplayService);
            var aggregationService = new AggregationService(timeDisplayService);
            var savedViewStorage = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "MustelaLogClient",
                "saved-views.json");
            var savedViewService = new SavedViewService(savedViewStorage);
            var exportService = new ExportService();
            var correlationService = new EventCorrelationService();

            var viewModel = new MainWindowViewModel(
                settings,
                apiClient,
                aggregationService,
                savedViewService,
                exportService,
                correlationService,
                timeDisplayService,
                _logger,
                inMemoryLogger);

            var window = new MainWindow(viewModel);
            window.Show();
        }
        catch (Exception exception)
        {
            MessageBox.Show(
                "Der Client konnte nicht gestartet werden." + exception,
                "MustelaLog Client",
                MessageBoxButton.OK,
                MessageBoxImage.Error);
            Shutdown(-1);
        }
    }

    /// <summary>
    /// Performs controlled application shutdown tasks, writes final diagnostic entries,
    /// and releases resources that were initialized during application startup.
    /// </summary>
    /// <param name="e">
    /// Provides shutdown information from the WPF application runtime,
    /// including the application exit code.
    /// </param>
    protected override void OnExit(ExitEventArgs e)
    {
        _logger?.Information("Desktop client shut down", new Dictionary<string, object?> { ["exitCode"] = e.ApplicationExitCode });
        base.OnExit(e);
    }

    private static ClientLogLevel ParseMinimumLevel(string? configuredValue)
    {
        return Enum.TryParse<ClientLogLevel>(configuredValue, true, out var parsed)
            ? parsed
            : ClientLogLevel.Information;
    }
}
