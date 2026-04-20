using System.Text.Json.Serialization;

namespace MustelaLog.Client.Core.Models;

/// <summary>Generisches Hüllformat der API für Endpunkte mit <c>data</c>.</summary>
public sealed class ApiEnvelope<T>
{
    [JsonPropertyName("ok")]
    public bool Ok { get; set; }

    [JsonPropertyName("requestId")]
    public string? RequestId { get; set; }

    [JsonPropertyName("data")]
    public T? Data { get; set; }

    [JsonPropertyName("error")]
    public ApiError? Error { get; set; }
}
