using Xunit;
using System.Text;
using MustelaLog.Client.Core.Models;
using MustelaLog.Client.Core.Services;

namespace MustelaLog.Client.Core.Tests;

/// <summary>
/// Prüft CSV- und JSON-Export der aktuell sichtbaren Treffermenge.
/// Damit bleibt die Exportfunktion testbar, obwohl der eigentliche Dateidialog
/// bewusst im UI-Code bleibt.
/// </summary>
public sealed class ExportServiceTests : IDisposable
{
    private readonly string _tempDirectory;
    private readonly ExportService _service = new();

    public ExportServiceTests()
    {
        _tempDirectory = Path.Combine(Path.GetTempPath(), "MustelaLogClientTests", Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(_tempDirectory);
    }

    [Fact]
    public async Task ExportCsvAsync_WritesUtf8BomAndEscapesText()
    {
        var path = Path.Combine(_tempDirectory, "events.csv");
        var events = new[]
        {
            new LogEventRecord
            {
                LogEventId = "evt-1",
                MessageText = "Hello, \"World\"",
                SeverityText = "INFO",
                SourceName = "api",
                OccurredAt = "2026-04-20 10:00:00",
                IngestedAt = "2026-04-20 10:00:01"
            }
        };

        await _service.ExportCsvAsync(path, events);

        var bytes = await File.ReadAllBytesAsync(path);
        Assert.True(bytes.Length >= 3);
        Assert.Equal(new byte[] { 0xEF, 0xBB, 0xBF }, bytes.Take(3).ToArray());

        var text = Encoding.UTF8.GetString(bytes);
        Assert.Contains("\"Hello, \"\"World\"\"\"", text);
    }

    [Fact]
    public async Task ExportJsonAsync_WritesReadableJson()
    {
        var path = Path.Combine(_tempDirectory, "events.json");
        var events = new[] { new LogEventRecord { LogEventId = "evt-1", SeverityText = "ERROR" } };

        await _service.ExportJsonAsync(path, events);

        var text = await File.ReadAllTextAsync(path);
        Assert.Contains("evt-1", text);
        Assert.Contains("ERROR", text);
    }

    public void Dispose()
    {
        if (Directory.Exists(_tempDirectory))
        {
            Directory.Delete(_tempDirectory, recursive: true);
        }
    }
}
