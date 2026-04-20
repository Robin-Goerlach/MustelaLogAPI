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

    [Fact]
    public void ParseApiTime_ReturnsUtcOffset()
    {
        var parsed = _service.ParseApiTime("2026-04-20T12:34:56+02:00");

        Assert.NotNull(parsed);
        Assert.Equal(TimeSpan.Zero, parsed!.Value.Offset);
        Assert.Equal(10, parsed.Value.Hour);
    }

    [Fact]
    public void FormatApiTime_IndicatesUtcMode()
    {
        var formatted = _service.FormatApiTime("2026-04-20T10:34:56Z", TimeDisplayMode.Utc);

        Assert.Contains("UTC", formatted);
        Assert.Contains("2026-04-20 10:34:56", formatted);
    }

    [Fact]
    public void ToApiQueryDate_ProducesExpectedFormat()
    {
        var text = _service.ToApiQueryDate(new DateTimeOffset(2026, 4, 20, 10, 34, 56, TimeSpan.Zero));

        Assert.Equal("2026-04-20 10:34:56", text);
    }
}
