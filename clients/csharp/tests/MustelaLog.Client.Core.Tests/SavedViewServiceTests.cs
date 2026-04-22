using Xunit;
using MustelaLog.Client.Core.Models;
using MustelaLog.Client.Core.Services;

namespace MustelaLog.Client.Core.Tests;

/// <summary>
/// Sichert das lokale Speichern, Umbenennen und Löschen von Ansichten ab.
/// Gerade weil V1 Saved Views lokal hält, ist diese Dateilogik ein guter
/// Kandidat für kleine, deterministische Tests.
/// </summary>
public sealed class SavedViewServiceTests : IDisposable
{
    private readonly string _tempDirectory;
    private readonly SavedViewService _service;

    public SavedViewServiceTests()
    {
        _tempDirectory = Path.Combine(Path.GetTempPath(), "MustelaLogClientTests", Guid.NewGuid().ToString("N"));
        _service = new SavedViewService(Path.Combine(_tempDirectory, "savedviews.json"));
    }

    [Fact]
    public void Save_ThenLoad_RoundTripsDefinition()
    {
        var definition = new SavedViewDefinition
        {
            Name = "Errors today",
            SortField = "occurredAt",
            SortAscending = false,
            VisibleColumns = ["severity", "message"],
            Filter = new LogQueryFilter
            {
                SeverityText = "ERROR",
                EventCategory = "security"
            }
        };

        _service.Save(definition);

        var loaded = _service.LoadAll().Single();
        Assert.Equal("Errors today", loaded.Name);
        Assert.Equal("ERROR", loaded.Filter.SeverityText);
        Assert.Equal("security", loaded.Filter.EventCategory);
        Assert.Equal(2, loaded.VisibleColumns.Count);
    }

    [Fact]
    public void Rename_ChangesStoredName()
    {
        _service.Save(new SavedViewDefinition { Name = "Old" });

        _service.Rename("Old", "New");

        var loaded = _service.LoadAll().Single();
        Assert.Equal("New", loaded.Name);
    }

    [Fact]
    public void Delete_RemovesStoredDefinition()
    {
        _service.Save(new SavedViewDefinition { Name = "Keep" });
        _service.Save(new SavedViewDefinition { Name = "Delete" });

        _service.Delete("Delete");

        var loaded = _service.LoadAll();
        Assert.Single(loaded);
        Assert.Equal("Keep", loaded[0].Name);
    }

    [Fact]
    public void LoadAll_ReturnsEmptyList_ForMalformedJson()
    {
        Directory.CreateDirectory(_tempDirectory);
        File.WriteAllText(Path.Combine(_tempDirectory, "savedviews.json"), "{ invalid json");

        var loaded = _service.LoadAll();

        Assert.Empty(loaded);
    }

    [Fact]
    public void Rename_Throws_ForDuplicateTargetName()
    {
        _service.Save(new SavedViewDefinition { Name = "One" });
        _service.Save(new SavedViewDefinition { Name = "Two" });

        Assert.Throws<InvalidOperationException>(() => _service.Rename("One", "Two"));
    }

    public void Dispose()
    {
        if (Directory.Exists(_tempDirectory))
        {
            Directory.Delete(_tempDirectory, recursive: true);
        }
    }
}
