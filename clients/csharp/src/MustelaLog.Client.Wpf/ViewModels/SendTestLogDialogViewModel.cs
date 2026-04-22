using System.Collections.ObjectModel;
using System.Text.Json;
using MustelaLog.Client.Core.Abstractions;
using MustelaLog.Client.Core.Models;

namespace MustelaLog.Client.Wpf.ViewModels;

/// <summary>
/// ViewModel für den Dialog zum Versand eines Test-Events.
/// 
/// V1 verwendet technisch hinterlegte Credentials. Deshalb wird die Quelle nicht
/// frei im Dialog umgeschaltet, sondern aus dem Token-Kontext der API bestimmt.
/// Die Source-Liste wird nur informativ mitgegeben, um spätere Erweiterungen
/// nicht durch einen harten Architekturwechsel zu erschweren.
/// </summary>
public sealed class SendTestLogDialogViewModel : ObservableObject
{
    private readonly ILogApiClient _apiClient;
    private readonly IAppLogger _logger;
    private string _severityText = "Information";
    private int _severityNumber = 9;
    private string _message = "Test event from desktop client";
    private string _eventName = "desktop.test";
    private string _eventCategory = "diagnostic";
    private string _eventAction = "send_test_log";
    private string _eventOutcome = "success";
    private string _attributesJson = "{  \"origin\": \"desktop-client\" }";
    private string? _serviceName = "desktop-client";
    private string? _correlationId;
    private string? _traceId;
    private string? _requestId;
    private string _statusMessage = "Ready.";

    /// <summary>Erzeugt das Dialog-ViewModel für API-Zugriff und Diagnose-Logging.</summary>
    public SendTestLogDialogViewModel(ILogApiClient apiClient, IAppLogger logger, IReadOnlyList<SourceRecord> sources)
    {
        _apiClient = apiClient;
        _logger = logger;
        AvailableSources = new ObservableCollection<SourceRecord>(sources.OrderBy(s => s.SourceName ?? s.SourceKey));
    }

    
    
    /// <summary>
/// Gets the list of selectable sources that can be used for sending a test log event.
/// In V1 this list mainly supports future extensibility and optional source selection scenarios.
/// </summary>
    public ObservableCollection<SourceRecord> AvailableSources { get; }

    public string SeverityText { get => _severityText; set => SetProperty(ref _severityText, value); }
    public int SeverityNumber { get => _severityNumber; set => SetProperty(ref _severityNumber, value); }
    public string Message { get => _message; set => SetProperty(ref _message, value); }
    public string EventName { get => _eventName; set => SetProperty(ref _eventName, value); }
    public string EventCategory { get => _eventCategory; set => SetProperty(ref _eventCategory, value); }
    public string EventAction { get => _eventAction; set => SetProperty(ref _eventAction, value); }
    public string EventOutcome { get => _eventOutcome; set => SetProperty(ref _eventOutcome, value); }
    public string AttributesJson { get => _attributesJson; set => SetProperty(ref _attributesJson, value); }
    public string? ServiceName { get => _serviceName; set => SetProperty(ref _serviceName, value); }
    public string? CorrelationId { get => _correlationId; set => SetProperty(ref _correlationId, value); }
    public string? TraceId { get => _traceId; set => SetProperty(ref _traceId, value); }
    public string? RequestId { get => _requestId; set => SetProperty(ref _requestId, value); }
    public string StatusMessage { get => _statusMessage; set => SetProperty(ref _statusMessage, value); }

    public IngestResponseData? LastResult { get; private set; }

    /// <summary>Versendet ein Testevent an die API.</summary>
    public async Task SendAsync()
    {
        ValidateInput();

        try
        {
            var request = new TestLogEventRequest
            {
                SeverityText = SeverityText,
                SeverityNumber = SeverityNumber,
                Message = Message,
                EventName = EventName,
                EventCategory = EventCategory,
                EventAction = EventAction,
                EventOutcome = EventOutcome,
                ServiceName = ServiceName,
                CorrelationId = CorrelationId,
                TraceId = TraceId,
                RequestId = RequestId,
                AttributesJson = AttributesJson
            };

            LastResult = await _apiClient.SendTestEventAsync(request);
            StatusMessage = $"Accepted: {LastResult.Accepted}, ingestRequestId={LastResult.IngestRequestId}";
            _logger.Information("Test event sent", new Dictionary<string, object?>
            {
                ["accepted"] = LastResult.Accepted,
                ["ingestRequestId"] = LastResult.IngestRequestId
            });
        }
        catch (Exception exception)
        {
            StatusMessage = "Sending test event failed.";
            _logger.Error("Sending test event failed", exception);
            throw;
        }
    }

    private void ValidateInput()
    {
        if (string.IsNullOrWhiteSpace(Message))
        {
            throw new InvalidOperationException("Please enter a message.");
        }

        if (string.IsNullOrWhiteSpace(EventName))
        {
            throw new InvalidOperationException("Please enter an event name.");
        }

        if (SeverityNumber < 1 || SeverityNumber > 24)
        {
            throw new InvalidOperationException("Severity number must be between 1 and 24.");
        }

        if (!string.IsNullOrWhiteSpace(AttributesJson))
        {
            try
            {
                using var document = JsonDocument.Parse(AttributesJson);
                if (document.RootElement.ValueKind != JsonValueKind.Object)
                {
                    throw new InvalidOperationException("Attributes JSON must be a JSON object.");
                }
            }
            catch (JsonException exception)
            {
                throw new InvalidOperationException("Attributes JSON is not valid JSON.", exception);
            }
        }
    }
}
