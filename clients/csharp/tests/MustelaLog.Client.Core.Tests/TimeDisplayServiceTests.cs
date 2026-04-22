using Xunit;
using MustelaLog.Client.Core.Enums;
using MustelaLog.Client.Core.Services;

namespace MustelaLog.Client.Core.Tests;

/// <summary>
/// Prüft Zeitumrechnung und Anzeige in UTC bzw. lokaler Zeit.
/// Diese Logik ist fachlich wichtig, weil der Client explizit beide
/// Darstellungsmodi unterstützen soll.
/// </summary>
public sealed class TimeDisplayServiceTests
{
    private readonly TimeDisplayService _service = new();

    /// <summary>
    /// Stellt sicher, dass API-Zeitwerte mit Offset in einen UTC-basierten
    /// <see cref="DateTimeOffset"/> umgerechnet werden.
    /// </summary>
    [Fact]
    public void ParseApiTime_ReturnsUtcOffset()
    {
        var parsed = _service.ParseApiTime("2026-04-20T12:34:56+02:00");

        Assert.NotNull(parsed);
        Assert.Equal(TimeSpan.Zero, parsed!.Value.Offset);
        Assert.Equal(10, parsed.Value.Hour);
    }

    /// <summary>
    /// Prüft, dass formatierte UTC-Zeitwerte den UTC-Hinweis enthalten,
    /// damit der Benutzer den aktiven Zeitmodus klar erkennt.
    /// </summary>
    [Fact]
    public void FormatApiTime_IndicatesUtcMode()
    {
        var formatted = _service.FormatApiTime("2026-04-20T10:34:56Z", TimeDisplayMode.Utc);

        Assert.Contains("UTC", formatted);
        Assert.Contains("2026-04-20 10:34:56", formatted);
    }

    /// <summary>
    /// Prüft das API-konforme Datumsformat für Query-Parameter.
    /// </summary>
    [Fact]
    public void ToApiQueryDate_ProducesExpectedFormat()
    {
        var text = _service.ToApiQueryDate(new DateTimeOffset(2026, 4, 20, 10, 34, 56, TimeSpan.Zero));

        Assert.Equal("2026-04-20 10:34:56", text);
    }

    /// <summary>
    /// Stellt sicher, dass das Ende eines ausgewählten Tages wirklich bis zur
    /// letzten Ticks-Auflösung des Tages reicht. Das ist wichtig, damit der
    /// Bis-Filter den kompletten Tag einschließt.
    /// </summary>
    [Fact]
    public void ToUtcEndOfDay_IncludesEntireSelectedDay()
    {
        var utc = _service.ToUtcEndOfDay(new DateTime(2026, 4, 20));

        Assert.NotNull(utc);
        Assert.Equal(59, utc!.Value.UtcDateTime.Second);
        Assert.Equal(9999999, utc.Value.UtcDateTime.Ticks % TimeSpan.TicksPerSecond);
    }
}
