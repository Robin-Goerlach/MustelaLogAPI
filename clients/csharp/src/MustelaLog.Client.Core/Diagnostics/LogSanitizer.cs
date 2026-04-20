using System.Text.Json;

namespace MustelaLog.Client.Core.Diagnostics;

/// <summary>Maskiert offensichtliche Geheimnisse im Diagnose-Logging.</summary>
public static class LogSanitizer
{
    private static readonly string[] SensitiveKeys = ["authorization", "token", "apiKey", "accessToken", "secret", "password"];

    /// <summary>
    /// Provides helper methods for sanitizing log output and masking sensitive values.
    /// </summary>
    public static string MaskSecrets(string value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return value;
        }

        var sanitized = value;
        sanitized = System.Text.RegularExpressions.Regex.Replace(
            sanitized,
            @"(?i)(authorization\s*[:=]\s*)(bearer\s+)?([A-Za-z0-9._\-+/=]+)",
            "$1***");

        sanitized = System.Text.RegularExpressions.Regex.Replace(
            sanitized,
            @"(?i)(token|apikey|access_token|secret|password)\s*[:=]\s*([A-Za-z0-9._\-+/=]+)",
            "$1=***");

        return sanitized;
    }

    /// <summary>
    /// Serializes a context dictionary to JSON while masking values of sensitive keys.
    /// </summary>
    /// <param name="context">
    /// The context data to serialize. Keys that look sensitive, such as authorization,
    /// token, apiKey, accessToken, secret, or password, are masked before serialization.
    /// </param>
    /// <returns>
    /// A JSON string containing the sanitized context data. Returns an empty string if the
    /// input is null or contains no entries.
    /// </returns>
    public static string SerializeContext(IReadOnlyDictionary<string, object?>? context)
    {
        if (context is null || context.Count == 0)
        {
            return string.Empty;
        }

        var normalized = new Dictionary<string, object?>();
        foreach (var item in context)
        {
            var isSensitive = SensitiveKeys.Any(key => item.Key.Contains(key, StringComparison.OrdinalIgnoreCase));
            normalized[item.Key] = isSensitive ? "***" : item.Value;
        }

        return JsonSerializer.Serialize(normalized);
    }
}
