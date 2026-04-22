using System.Globalization;
using MustelaLog.Client.Core.Enums;

namespace MustelaLog.Client.Core.Services;

/// <summary>
/// Kapselt Umrechnung und Darstellung von API-Zeitstempeln.
/// 
/// Die Klasse trennt bewusst zwischen zwei UI-Fällen:
/// - minutengenaue Zeiträume wie „letzte 15 Minuten“
/// - kalenderbasierte Tagesauswahl aus DatePicker-Steuerelementen.
/// 
/// Dadurch vermeiden wir den häufigen Fehler, dass ein im DatePicker gewähltes
/// „Bis“-Datum unabsichtlich bereits um 00:00 Uhr endet und damit fast den
/// gesamten Tag ausschließt.
/// </summary>
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

    /// <summary>Parst ein Zeitformat aus der API nach UTC.</summary>
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

    /// <summary>Formatiert einen Rohwert aus der API für die Anzeige.</summary>
    public string FormatApiTime(string? value, TimeDisplayMode mode)
    {
        var parsed = ParseApiTime(value);
        if (parsed is null)
        {
            return value ?? string.Empty;
        }

        return Format(parsed.Value, mode);
    }

    /// <summary>Formatiert einen Zeitpunkt für UTC- oder Lokalzeit-Anzeige.</summary>
    public string Format(DateTimeOffset value, TimeDisplayMode mode)
    {
        return mode == TimeDisplayMode.Utc
            ? $"{value.UtcDateTime:yyyy-MM-dd HH:mm:ss} UTC"
            : $"{value.ToLocalTime():yyyy-MM-dd HH:mm:ss} local";
    }

    /// <summary>
    /// Wandelt einen lokalen Zeitpunkt nach UTC um.
    /// 
    /// Bereits als UTC markierte Werte bleiben erhalten.
    /// Unmarkierte Werte behandeln wir als lokale Desktop-Zeit.
    /// </summary>
    public DateTimeOffset? ToUtc(DateTime? value)
    {
        if (value is null)
        {
            return null;
        }

        return value.Value.Kind switch
        {
            DateTimeKind.Utc => new DateTimeOffset(value.Value, TimeSpan.Zero),
            DateTimeKind.Local => new DateTimeOffset(value.Value).ToUniversalTime(),
            _ => new DateTimeOffset(DateTime.SpecifyKind(value.Value, DateTimeKind.Local)).ToUniversalTime()
        };
    }

    /// <summary>
    /// Wandelt das Startdatum eines per DatePicker gewählten Tages in UTC um.
    /// </summary>
    public DateTimeOffset? ToUtcStartOfDay(DateTime? localDate)
    {
        if (localDate is null)
        {
            return null;
        }

        var localStart = localDate.Value.Date;
        return ToUtc(localStart);
    }

    /// <summary>
    /// Wandelt das Ende eines per DatePicker gewählten Tages in UTC um.
    /// 
    /// Wir verwenden bewusst den letzten Tick des Tages, damit ein Benutzer,
    /// der z. B. den 21.04. auswählt, auch wirklich den ganzen Tag meint.
    /// </summary>
    public DateTimeOffset? ToUtcEndOfDay(DateTime? localDate)
    {
        if (localDate is null)
        {
            return null;
        }

        var localEnd = localDate.Value.Date.AddDays(1).AddTicks(-1);
        return ToUtc(localEnd);
    }

    /// <summary>Formatiert einen UTC-Zeitpunkt für Query-Parameter der API.</summary>
    public string ToApiQueryDate(DateTimeOffset value) => value.UtcDateTime.ToString("yyyy-MM-dd HH:mm:ss", CultureInfo.InvariantCulture);
}
