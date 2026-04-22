using System.Text.Json;
using System.Text.RegularExpressions;

namespace MustelaLog.Client.Core.Diagnostics;

/// <summary>
/// Maskiert offensichtliche Geheimnisse im Diagnose-Logging.
/// 
/// Die Hilfsklasse deckt absichtlich eher zu viel als zu wenig ab. Für einen
/// Desktop-Client ist es sicherer, verdächtige Tokens und Secret-ähnliche Werte
/// defensiv zu maskieren, statt im Zweifel doch versehentlich Klartext zu loggen.
/// </summary>
public static class LogSanitizer
{
    private static readonly string[] SensitiveKeys = ["authorization", "token", "apiKey", "api-key", "accessToken", "access_token", "secret", "password"];

    /// <summary>Maskiert typische Token-, Passwort- und Secret-Muster in Texten.</summary>
    public static string MaskSecrets(string value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return value;
        }

        var sanitized = value;
        sanitized = Regex.Replace(
            sanitized,
            "(?i)(authorization\\s*[:=]\\s*)(bearer\\s+)?([^\\s,;]+)",
            "$1***");

        sanitized = Regex.Replace(
            sanitized,
            "(?i)(token|api[_-]?key|access[_-]?token|secret|password)\\s*[:=]\\s*([^\\s,;]+)",
            "$1=***");

        sanitized = Regex.Replace(
            sanitized,
            "(?i)(\\\"(?:token|api[_-]?key|access[_-]?token|secret|password)\\\"\\s*:\\s*\\\")(.*?)(\\\")",
            "$1***$3");

        return sanitized;
    }

    /// <summary>Serialisiert Diagnosekontext und maskiert sensible Schlüssel.</summary>
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
