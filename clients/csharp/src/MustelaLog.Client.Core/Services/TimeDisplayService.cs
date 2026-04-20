using System.Globalization;
using MustelaLog.Client.Core.Enums;

namespace MustelaLog.Client.Core.Services;

/// <summary>Kapselt Umrechnung und Darstellung von API-Zeitstempeln.</summary>
public sealed class TimeDisplayService
{
    private static readonly string[] SupportedFormats =
    [
        "yyyy-MM-dd HH:mm:ss.ffffff",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-ddTHH:mm:ss.ffffffK",
        "yyyy-MM-ddTHH:mm:ssK",
        "O"
    ];

    public DateTimeOffset? ParseApiTime(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        if (DateTimeOffset.TryParseExact(value, SupportedFormats, CultureInfo.InvariantCulture, DateTimeStyles.AssumeUniversal | DateTimeStyles.AdjustToUniversal, out var dto))
        {
            return dto.ToUniversalTime();
        }

        if (DateTime.TryParse(value, CultureInfo.InvariantCulture, DateTimeStyles.AssumeUniversal | DateTimeStyles.AdjustToUniversal, out var dt))
        {
            return new DateTimeOffset(dt, TimeSpan.Zero);
        }

        return null;
    }

    public string FormatApiTime(string? value, TimeDisplayMode mode)
    {
        var parsed = ParseApiTime(value);
        if (parsed is null)
        {
            return value ?? string.Empty;
        }

        return Format(parsed.Value, mode);
    }

    public string Format(DateTimeOffset value, TimeDisplayMode mode)
    {
        return mode == TimeDisplayMode.Utc
            ? $"{value.UtcDateTime:yyyy-MM-dd HH:mm:ss} UTC"
            : $"{value.ToLocalTime():yyyy-MM-dd HH:mm:ss} local";
    }

    public DateTimeOffset? ToUtc(DateTime? localDateTime)
    {
        if (localDateTime is null)
        {
            return null;
        }

        var local = DateTime.SpecifyKind(localDateTime.Value, DateTimeKind.Local);
        return new DateTimeOffset(local).ToUniversalTime();
    }

    public string ToApiQueryDate(DateTimeOffset value) => value.UtcDateTime.ToString("yyyy-MM-dd HH:mm:ss", CultureInfo.InvariantCulture);
}
