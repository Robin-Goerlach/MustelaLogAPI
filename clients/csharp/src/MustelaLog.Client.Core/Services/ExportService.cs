using System.Text;
using System.Text.Json;
using MustelaLog.Client.Core.Models;

namespace MustelaLog.Client.Core.Services;

/// <summary>Exportiert die aktuell sichtbaren Datensätze lokal nach CSV oder JSON.</summary>
public sealed class ExportService
{
    private static readonly JsonSerializerOptions JsonOptions = new() { WriteIndented = true };

    public async Task ExportCsvAsync(string filePath, IEnumerable<LogEventRecord> events, CancellationToken cancellationToken = default)
    {
        var rows = events.ToList();
        var builder = new StringBuilder();
        builder.AppendLine("log_event_id,occurred_at,ingested_at,severity_text,source_name,host_name,service_name,event_category,event_action,event_outcome,message_text,correlation_id,trace_id");
        foreach (var item in rows)
        {
            builder.AppendLine(string.Join(",",
                Escape(item.LogEventId),
                Escape(item.OccurredAt),
                Escape(item.IngestedAt),
                Escape(item.SeverityText),
                Escape(item.SourceName),
                Escape(item.HostName),
                Escape(item.ServiceName),
                Escape(item.EventCategory),
                Escape(item.EventAction),
                Escape(item.EventOutcome),
                Escape(item.MessageText),
                Escape(item.CorrelationId),
                Escape(item.TraceId)));
        }

        await File.WriteAllTextAsync(filePath, builder.ToString(), new UTF8Encoding(true), cancellationToken);
    }

    public async Task ExportJsonAsync(string filePath, IEnumerable<LogEventRecord> events, CancellationToken cancellationToken = default)
    {
        await using var stream = File.Create(filePath);
        await JsonSerializer.SerializeAsync(stream, events, JsonOptions, cancellationToken);
    }

    private static string Escape(string? value)
    {
        var text = value ?? string.Empty;
        if (!text.Contains(',') && !text.Contains('"') && !text.Contains('\n'))
        {
            return text;
        }

        return "\"" + text.Replace("\"", "\"\"") + "\"";
    }
}
