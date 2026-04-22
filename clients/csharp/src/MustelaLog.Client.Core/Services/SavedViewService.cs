using System.Text.Json;
using MustelaLog.Client.Core.Models;

namespace MustelaLog.Client.Core.Services;

/// <summary>
/// Verwaltet lokal gespeicherte Filteransichten.
/// 
/// Die Implementierung behandelt beschädigte JSON-Dateien defensiv und schreibt
/// Änderungen atomar in eine temporäre Datei, bevor der finale Name ersetzt wird.
/// </summary>
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

        try
        {
            var json = File.ReadAllText(StoragePath);
            return JsonSerializer.Deserialize<List<SavedViewDefinition>>(json, SerializerOptions) ?? [];
        }
        catch
        {
            return Array.Empty<SavedViewDefinition>();
        }
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
        var cleanedNewName = newName.Trim();
        var items = LoadAll().ToList();
        var existing = items.FirstOrDefault(v => string.Equals(v.Name, oldName, StringComparison.OrdinalIgnoreCase));
        if (existing is null)
        {
            return;
        }

        var collision = items.Any(v => !string.Equals(v.Name, oldName, StringComparison.OrdinalIgnoreCase) && string.Equals(v.Name, cleanedNewName, StringComparison.OrdinalIgnoreCase));
        if (collision)
        {
            throw new InvalidOperationException($"A saved view named '{cleanedNewName}' already exists.");
        }

        existing.Name = cleanedNewName;
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
        var tempPath = StoragePath + ".tmp";
        File.WriteAllText(tempPath, json);

        if (File.Exists(StoragePath))
        {
            File.Delete(StoragePath);
        }

        File.Move(tempPath, StoragePath, true);
    }
}
