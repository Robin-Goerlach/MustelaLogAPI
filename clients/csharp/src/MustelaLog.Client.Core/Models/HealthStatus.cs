using System.Text.Json.Serialization;

namespace MustelaLog.Client.Core.Models;

/// <summary>Antwortmodell des Health-Endpunkts.</summary>
public sealed class HealthStatus
{
    [JsonPropertyName("ok")]
    public bool Ok { get; set; }

    [JsonPropertyName("service")]
    public string? Service { get; set; }

    [JsonPropertyName("version")]
    public string? Version { get; set; }

    [JsonPropertyName("requestId")]
    public string? RequestId { get; set; }

    [JsonPropertyName("timestamp")]
    public string? Timestamp { get; set; }
}
