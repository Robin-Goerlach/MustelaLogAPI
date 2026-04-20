using System.Text.Json.Serialization;

namespace MustelaLog.Client.Core.Models;

/// <summary>Standardisierte Fehlerstruktur der MustelaLogAPI.</summary>
public sealed class ApiError
{
    [JsonPropertyName("code")]
    public string? Code { get; set; }

    [JsonPropertyName("message")]
    public string? Message { get; set; }

    [JsonPropertyName("requestId")]
    public string? RequestId { get; set; }
}
