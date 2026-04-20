using System.Text.Json.Serialization;

namespace MustelaLog.Client.Core.Models;

/// <summary>Datenbereich der Ingest-Antwort.</summary>
public sealed class IngestResponseData
{
    [JsonPropertyName("ingestRequestId")]
    public string? IngestRequestId { get; set; }

    [JsonPropertyName("eventIds")]
    public List<string> EventIds { get; set; } = [];

    [JsonPropertyName("accepted")]
    public int Accepted { get; set; }
}
