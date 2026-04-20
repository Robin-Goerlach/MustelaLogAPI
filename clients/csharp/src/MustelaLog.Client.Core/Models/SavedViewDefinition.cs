namespace MustelaLog.Client.Core.Models;

/// <summary>Lokal gespeicherte Filteransicht.</summary>
public sealed class SavedViewDefinition
{
    public string Name { get; set; } = "New View";
    public LogQueryFilter Filter { get; set; } = new();
    public string SortField { get; set; } = "occurredAt";
    public bool SortAscending { get; set; }
    public List<string> VisibleColumns { get; set; } = [];
}
