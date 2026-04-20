using System.Text.Json;
using System.Text.Json.Serialization;

namespace MustelaLog.Client.Core.Models;

/// <summary>
/// Repräsentiert ein Logevent aus der API.
/// Zeitfelder bleiben bewusst als Rohstrings erhalten.
/// </summary>
public sealed class LogEventRecord
{
    [JsonPropertyName("log_event_id")] public string? LogEventId { get; set; }
    [JsonPropertyName("tenant_id")] public string? TenantId { get; set; }
    [JsonPropertyName("tenant_key")] public string? TenantKey { get; set; }
    [JsonPropertyName("source_id")] public string? SourceId { get; set; }
    [JsonPropertyName("source_key")] public string? SourceKey { get; set; }
    [JsonPropertyName("source_name")] public string? SourceName { get; set; }
    [JsonPropertyName("environment_code")] public string? EnvironmentCode { get; set; }
    [JsonPropertyName("ingest_request_id")] public string? IngestRequestId { get; set; }
    [JsonPropertyName("source_event_id")] public string? SourceEventId { get; set; }
    [JsonPropertyName("occurred_at")] public string? OccurredAt { get; set; }
    [JsonPropertyName("observed_at")] public string? ObservedAt { get; set; }
    [JsonPropertyName("received_at")] public string? ReceivedAt { get; set; }
    [JsonPropertyName("ingested_at")] public string? IngestedAt { get; set; }
    [JsonPropertyName("severity_number")] public int SeverityNumber { get; set; }
    [JsonPropertyName("severity_text")] public string? SeverityText { get; set; }
    [JsonPropertyName("event_name")] public string? EventName { get; set; }
    [JsonPropertyName("event_category")] public string? EventCategory { get; set; }
    [JsonPropertyName("event_action")] public string? EventAction { get; set; }
    [JsonPropertyName("event_outcome")] public string? EventOutcome { get; set; }
    [JsonPropertyName("message_text")] public string? MessageText { get; set; }
    [JsonPropertyName("raw_payload_json")] public string? RawPayloadJson { get; set; }
    [JsonPropertyName("host_name")] public string? HostName { get; set; }
    [JsonPropertyName("service_name")] public string? ServiceName { get; set; }
    [JsonPropertyName("component_name")] public string? ComponentName { get; set; }
    [JsonPropertyName("module_name")] public string? ModuleName { get; set; }
    [JsonPropertyName("process_name")] public string? ProcessName { get; set; }
    [JsonPropertyName("process_pid")] public int? ProcessPid { get; set; }
    [JsonPropertyName("thread_id")] public string? ThreadId { get; set; }
    [JsonPropertyName("actor_user_id")] public string? ActorUserId { get; set; }
    [JsonPropertyName("actor_principal")] public string? ActorPrincipal { get; set; }
    [JsonPropertyName("session_hash_sha256")] public string? SessionHashSha256 { get; set; }
    [JsonPropertyName("client_ip")] public string? ClientIp { get; set; }
    [JsonPropertyName("server_ip")] public string? ServerIp { get; set; }
    [JsonPropertyName("trace_id")] public string? TraceId { get; set; }
    [JsonPropertyName("span_id")] public string? SpanId { get; set; }
    [JsonPropertyName("correlation_id")] public string? CorrelationId { get; set; }
    [JsonPropertyName("request_correlation_id")] public string? RequestCorrelationId { get; set; }
    [JsonPropertyName("attributes_json")] public string? AttributesJson { get; set; }
    [JsonPropertyName("classification_code")] public string? ClassificationCode { get; set; }
    [JsonPropertyName("retention_policy_code")] public string? RetentionPolicyCode { get; set; }
    [JsonPropertyName("retention_delete_after")] public string? RetentionDeleteAfter { get; set; }
    [JsonPropertyName("legal_hold_flag")] public bool LegalHoldFlag { get; set; }
    [JsonPropertyName("canonical_hash_sha256")] public string? CanonicalHashSha256 { get; set; }
    [JsonPropertyName("previous_hash_sha256")] public string? PreviousHashSha256 { get; set; }
    [JsonPropertyName("source_signature")] public string? SourceSignature { get; set; }
    [JsonPropertyName("signature_algorithm")] public string? SignatureAlgorithm { get; set; }
    [JsonPropertyName("tenant_scope_key")] public string? TenantScopeKey { get; set; }

    public string GetPrettyAttributesJson()
    {
        if (string.IsNullOrWhiteSpace(AttributesJson))
        {
            return string.Empty;
        }

        try
        {
            using var document = JsonDocument.Parse(AttributesJson);
            return JsonSerializer.Serialize(document.RootElement, new JsonSerializerOptions { WriteIndented = true });
        }
        catch
        {
            return AttributesJson ?? string.Empty;
        }
    }

    public string GetPrettyRawPayloadJson()
    {
        if (string.IsNullOrWhiteSpace(RawPayloadJson))
        {
            return string.Empty;
        }

        try
        {
            using var document = JsonDocument.Parse(RawPayloadJson);
            return JsonSerializer.Serialize(document.RootElement, new JsonSerializerOptions { WriteIndented = true });
        }
        catch
        {
            return RawPayloadJson ?? string.Empty;
        }
    }
}
