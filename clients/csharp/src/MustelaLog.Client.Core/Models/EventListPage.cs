using System.Text.Json.Serialization;

namespace MustelaLog.Client.Core.Models;

/// <summary>Paginiertes Resultat für Eventlisten.</summary>
public sealed class EventListPage
{
    [JsonPropertyName("items")]
    public List<LogEventRecord> Items { get; set; } = [];

    [JsonPropertyName("total")]
    public int Total { get; set; }

    [JsonPropertyName("page")]
    public int Page { get; set; }

    [JsonPropertyName("pageSize")]
    public int PageSize { get; set; }
}
