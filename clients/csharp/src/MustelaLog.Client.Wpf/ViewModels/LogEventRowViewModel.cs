using MustelaLog.Client.Core.Enums;
using MustelaLog.Client.Core.Models;
using MustelaLog.Client.Core.Services;

namespace MustelaLog.Client.Wpf.ViewModels;

/// <summary>UI-freundliche Projektion eines Logevents.</summary>
public sealed class LogEventRowViewModel : ObservableObject
{
    private readonly TimeDisplayService _timeDisplayService;
    private TimeDisplayMode _timeMode;

    public LogEventRowViewModel(LogEventRecord record, TimeDisplayService timeDisplayService, TimeDisplayMode timeMode)
    {
        Record = record;
        _timeDisplayService = timeDisplayService;
        _timeMode = timeMode;
    }

    public LogEventRecord Record { get; }
    public string EventId => Record.LogEventId ?? string.Empty;
    public string OccurredAtDisplay => _timeDisplayService.FormatApiTime(Record.OccurredAt, _timeMode);
    public string IngestedAtDisplay => _timeDisplayService.FormatApiTime(Record.IngestedAt, _timeMode);
    public string Severity => Record.SeverityText ?? string.Empty;
    public string SourceOrHost => string.IsNullOrWhiteSpace(Record.HostName) ? (Record.SourceName ?? Record.SourceKey ?? string.Empty) : $"{Record.SourceName ?? Record.SourceKey} / {Record.HostName}";
    public string Service => Record.ServiceName ?? string.Empty;
    public string EventCategory => Record.EventCategory ?? string.Empty;
    public string EventAction => Record.EventAction ?? string.Empty;
    public string EventOutcome => Record.EventOutcome ?? string.Empty;
    public string MessagePreview => Ellipsize(Record.MessageText, 100);
    public string CorrelationOrTracePreview => ShortIdentifier(Record.CorrelationId ?? Record.TraceId);
    public string ObservedAtDisplay => _timeDisplayService.FormatApiTime(Record.ObservedAt, _timeMode);
    public string ReceivedAtDisplay => _timeDisplayService.FormatApiTime(Record.ReceivedAt, _timeMode);

    public void SetTimeMode(TimeDisplayMode timeMode)
    {
        _timeMode = timeMode;
        OnPropertyChanged(nameof(OccurredAtDisplay));
        OnPropertyChanged(nameof(IngestedAtDisplay));
        OnPropertyChanged(nameof(ObservedAtDisplay));
        OnPropertyChanged(nameof(ReceivedAtDisplay));
    }

    private static string Ellipsize(string? value, int maxLength)
        => string.IsNullOrWhiteSpace(value) ? string.Empty : (value.Length <= maxLength ? value : value[..maxLength] + "…");

    private static string ShortIdentifier(string? value)
        => string.IsNullOrWhiteSpace(value) ? string.Empty : (value.Length <= 18 ? value : $"{value[..8]}…{value[^6..]}");
}
