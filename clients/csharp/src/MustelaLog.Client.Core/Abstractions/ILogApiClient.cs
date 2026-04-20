using MustelaLog.Client.Core.Models;

namespace MustelaLog.Client.Core.Abstractions;

/// <summary>Beschreibt die API-Operationen, die der Desktop-Client benötigt.</summary>
public interface ILogApiClient
{
    Task<HealthStatus> GetHealthAsync(CancellationToken cancellationToken = default);
    Task<EventListPage> GetEventsAsync(LogQueryFilter filter, int page, int pageSize, string sortField, bool sortAscending, CancellationToken cancellationToken = default);
    Task<LogEventRecord?> GetEventByIdAsync(string eventId, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<SourceRecord>> GetSourcesAsync(CancellationToken cancellationToken = default);
    Task<IngestResponseData> SendTestEventAsync(TestLogEventRequest request, CancellationToken cancellationToken = default);
}
