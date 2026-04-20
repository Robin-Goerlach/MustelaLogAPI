using System.Text.Json;
using MustelaLog.Client.Core.Models;

namespace MustelaLog.Client.Core.Services;

/// <summary>Verwaltet lokal gespeicherte Filteransichten.</summary>
public sealed class SavedViewService
{
    private static readonly JsonSerializerOptions SerializerOptions = new()
    {
        WriteIndented = true,
        PropertyNameCaseInsensitive = true
    };

    public SavedViewService(string storagePath)
    {
        StoragePath = Environment.ExpandEnvironmentVariables(storagePath);
    }

    public string StoragePath { get; }

    public IReadOnlyList<SavedViewDefinition> LoadAll()
    {
        if (!File.Exists(StoragePath))
        {
            return Array.Empty<SavedViewDefinition>();
        }

        var json = File.ReadAllText(StoragePath);
        return JsonSerializer.Deserialize<List<SavedViewDefinition>>(json, SerializerOptions) ?? [];
    }

    public void Save(SavedViewDefinition definition)
    {
        var items = LoadAll().ToList();
        var existing = items.FirstOrDefault(v => string.Equals(v.Name, definition.Name, StringComparison.OrdinalIgnoreCase));
        if (existing is null)
        {
            items.Add(definition);
        }
        else
        {
            existing.Filter = definition.Filter;
            existing.SortField = definition.SortField;
            existing.SortAscending = definition.SortAscending;
            existing.VisibleColumns = definition.VisibleColumns;
        }

        Persist(items);
    }

    public void Delete(string name)
    {
        var items = LoadAll().Where(v => !string.Equals(v.Name, name, StringComparison.OrdinalIgnoreCase)).ToList();
        Persist(items);
    }

    public void Rename(string oldName, string newName)
    {
        var items = LoadAll().ToList();
        var existing = items.FirstOrDefault(v => string.Equals(v.Name, oldName, StringComparison.OrdinalIgnoreCase));
        if (existing is null)
        {
            return;
        }
        existing.Name = newName;
        Persist(items);
    }

    private void Persist(List<SavedViewDefinition> items)
    {
        var directory = Path.GetDirectoryName(StoragePath);
        if (!string.IsNullOrWhiteSpace(directory))
        {
            Directory.CreateDirectory(directory);
        }

        var json = JsonSerializer.Serialize(items.OrderBy(v => v.Name).ToList(), SerializerOptions);
        File.WriteAllText(StoragePath, json);
    }
}
