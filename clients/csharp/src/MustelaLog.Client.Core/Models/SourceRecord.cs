using System.Text.Json.Serialization;

namespace MustelaLog.Client.Core.Models;

/// <summary>Repräsentiert eine Quelle aus der API.</summary>
public sealed class SourceRecord
{
    [JsonPropertyName("source_id")]
    public string? SourceId { get; set; }

    [JsonPropertyName("tenant_id")]
    public string? TenantId { get; set; }

    [JsonPropertyName("tenant_key")]
    public string? TenantKey { get; set; }

    [JsonPropertyName("source_key")]
    public string? SourceKey { get; set; }

    [JsonPropertyName("source_name")]
    public string? SourceName { get; set; }

    [JsonPropertyName("source_type_code")]
    public string? SourceTypeCode { get; set; }

    [JsonPropertyName("source_type_name")]
    public string? SourceTypeName { get; set; }

    [JsonPropertyName("hostname")]
    public string? Hostname { get; set; }

    [JsonPropertyName("environment_code")]
    public string? EnvironmentCode { get; set; }

    [JsonPropertyName("service_name")]
    public string? ServiceName { get; set; }

    [JsonPropertyName("version_text")]
    public string? VersionText { get; set; }

    [JsonPropertyName("active")]
    public bool Active { get; set; }
}
