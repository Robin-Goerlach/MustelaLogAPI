using System.Diagnostics;
using System.Net;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using MustelaLog.Client.Core.Abstractions;
using MustelaLog.Client.Core.Configuration;
using MustelaLog.Client.Core.Models;

namespace MustelaLog.Client.Core.Services;

/// <summary>
/// HTTP-Client für die MustelaLogAPI.
///
/// Die Klasse kapselt alle HTTP-Details bewusst außerhalb der UI.
/// </summary>
public sealed class LogApiClient : ILogApiClient
{
    private readonly HttpClient _httpClient;
    private readonly ApiSettings _apiSettings;
    private readonly ICredentialProvider _credentialProvider;
    private readonly IAppLogger _logger;
    private readonly TimeDisplayService _timeDisplayService;

    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNameCaseInsensitive = true
    };

    public LogApiClient(HttpClient httpClient, ApiSettings apiSettings, ICredentialProvider credentialProvider, IAppLogger logger, TimeDisplayService timeDisplayService)
    {
        _httpClient = httpClient;
        _apiSettings = apiSettings;
        _credentialProvider = credentialProvider;
        _logger = logger;
        _timeDisplayService = timeDisplayService;
    }

    public Task<HealthStatus> GetHealthAsync(CancellationToken cancellationToken = default)
        => SendAsync<HealthStatus>(HttpMethod.Get, $"/api/{_apiSettings.ApiVersion}/health", null, cancellationToken);

    public async Task<EventListPage> GetEventsAsync(LogQueryFilter filter, int page, int pageSize, string sortField, bool sortAscending, CancellationToken cancellationToken = default)
    {
        var query = new Dictionary<string, string?>
        {
            ["page"] = page.ToString(),
            ["pageSize"] = pageSize.ToString(),
            ["sort"] = sortField,
            ["direction"] = sortAscending ? "ASC" : "DESC",
            ["sourceKey"] = filter.SourceKey,
            ["severityText"] = filter.SeverityText,
            ["traceId"] = filter.TraceId,
            ["correlationId"] = filter.CorrelationId,
            ["requestId"] = filter.RequestId,
            ["hostname"] = filter.Hostname,
            ["service"] = filter.Service,
            ["eventCategory"] = filter.EventCategory,
            ["eventAction"] = filter.EventAction,
            ["eventOutcome"] = filter.EventOutcome,
            ["text"] = filter.TextSearch,
            ["component"] = filter.Component,
            ["actorUserId"] = filter.ActorUserId,
            ["actorPrincipal"] = filter.ActorPrincipal,
            ["sessionHash"] = filter.SessionHash,
            ["clientIp"] = filter.ClientIp,
            ["serverIp"] = filter.ServerIp,
            ["hasPayload"] = filter.OnlyWithPayload ? "1" : null,
            ["hasCorrelation"] = filter.OnlyWithCorrelation ? "1" : null,
            ["hasActor"] = filter.OnlyWithActor ? "1" : null,
            ["from"] = filter.FromUtc.HasValue ? _timeDisplayService.ToApiQueryDate(filter.FromUtc.Value) : null,
            ["to"] = filter.ToUtc.HasValue ? _timeDisplayService.ToApiQueryDate(filter.ToUtc.Value) : null
        };

        var envelope = await SendAsync<ApiEnvelope<EventListPage>>(HttpMethod.Get, $"/api/{_apiSettings.ApiVersion}/events", query, cancellationToken);
        return envelope.Data ?? new EventListPage();
    }

    public async Task<LogEventRecord?> GetEventByIdAsync(string eventId, CancellationToken cancellationToken = default)
    {
        var envelope = await SendAsync<ApiEnvelope<LogEventRecord>>(HttpMethod.Get, $"/api/{_apiSettings.ApiVersion}/events/{Uri.EscapeDataString(eventId)}", null, cancellationToken);
        return envelope.Data;
    }

    public async Task<IReadOnlyList<SourceRecord>> GetSourcesAsync(CancellationToken cancellationToken = default)
    {
        var envelope = await SendAsync<ApiEnvelope<List<SourceRecord>>>(HttpMethod.Get, $"/api/{_apiSettings.ApiVersion}/sources", null, cancellationToken);
        return envelope.Data ?? [];
    }

    public async Task<IngestResponseData> SendTestEventAsync(TestLogEventRequest request, CancellationToken cancellationToken = default)
    {
        var payload = BuildTestEventPayload(request);
        var envelope = await SendAsync<ApiEnvelope<IngestResponseData>>(HttpMethod.Post, $"/api/{_apiSettings.ApiVersion}/ingest/events", null, cancellationToken, payload);
        return envelope.Data ?? new IngestResponseData();
    }

    private object BuildTestEventPayload(TestLogEventRequest request)
    {
        Dictionary<string, object?> attributes;
        if (string.IsNullOrWhiteSpace(request.AttributesJson))
        {
            attributes = new Dictionary<string, object?>();
        }
        else
        {
            try
            {
                attributes = JsonSerializer.Deserialize<Dictionary<string, object?>>(request.AttributesJson, JsonOptions) ?? new Dictionary<string, object?>();
            }
            catch
            {
                attributes = new Dictionary<string, object?> { ["rawAttributesText"] = request.AttributesJson };
            }
        }

        return new Dictionary<string, object?>
        {
            ["sourceEventId"] = request.SourceEventId,
            ["occurredAt"] = DateTimeOffset.UtcNow.ToString("O"),
            ["observedAt"] = DateTimeOffset.UtcNow.ToString("O"),
            ["severityNumber"] = request.SeverityNumber,
            ["severityText"] = request.SeverityText,
            ["eventName"] = request.EventName,
            ["eventCategory"] = request.EventCategory,
            ["eventAction"] = request.EventAction,
            ["eventOutcome"] = request.EventOutcome,
            ["message"] = request.Message,
            ["hostName"] = request.HostName,
            ["serviceName"] = request.ServiceName,
            ["componentName"] = request.ComponentName,
            ["correlationId"] = request.CorrelationId,
            ["traceId"] = request.TraceId,
            ["requestId"] = request.RequestId,
            ["attributes"] = attributes
        };
    }

    private async Task<T> SendAsync<T>(HttpMethod method, string route, IReadOnlyDictionary<string, string?>? query, CancellationToken cancellationToken, object? body = null)
    {
        var request = new HttpRequestMessage(method, BuildRouteUri(route, query));
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

        var authHeader = await _credentialProvider.GetAuthorizationHeaderAsync(cancellationToken);
        if (authHeader is not null)
        {
            request.Headers.Authorization = authHeader;
        }

        if (body is not null)
        {
            var json = JsonSerializer.Serialize(body, JsonOptions);
            request.Content = new StringContent(json, Encoding.UTF8, "application/json");
        }

        var stopwatch = Stopwatch.StartNew();
        var maskedRoute = request.RequestUri?.ToString() ?? route;
        _logger.Information("Sending API request", new Dictionary<string, object?>
        {
            ["method"] = method.Method,
            ["url"] = maskedRoute
        });

        try
        {
            using var response = await _httpClient.SendAsync(request, cancellationToken);
            var text = await response.Content.ReadAsStringAsync(cancellationToken);
            stopwatch.Stop();

            _logger.Information("Received API response", new Dictionary<string, object?>
            {
                ["statusCode"] = (int)response.StatusCode,
                ["durationMs"] = stopwatch.ElapsedMilliseconds,
                ["url"] = maskedRoute
            });

            if (!response.IsSuccessStatusCode)
            {
                throw CreateApiException(response.StatusCode, text);
            }

            var result = JsonSerializer.Deserialize<T>(text, JsonOptions);
            if (result is null)
            {
                throw new InvalidOperationException("API returned an empty JSON body.");
            }

            return result;
        }
        catch (Exception exception)
        {
            stopwatch.Stop();
            _logger.Error("API request failed", exception, new Dictionary<string, object?>
            {
                ["url"] = maskedRoute,
                ["durationMs"] = stopwatch.ElapsedMilliseconds
            });
            throw;
        }
    }

    private Uri BuildRouteUri(string route, IReadOnlyDictionary<string, string?>? query)
    {
        var builder = new UriBuilder(_apiSettings.BaseUrl);
        var parameters = ParseExistingQuery(builder.Query);
        parameters["route"] = route;

        if (query is not null)
        {
            foreach (var item in query)
            {
                if (!string.IsNullOrWhiteSpace(item.Value))
                {
                    parameters[item.Key] = item.Value!;
                }
            }
        }

        builder.Query = string.Join("&", parameters.Select(pair => $"{Uri.EscapeDataString(pair.Key)}={Uri.EscapeDataString(pair.Value)}"));
        return builder.Uri;
    }

    private static Dictionary<string, string> ParseExistingQuery(string? query)
    {
        var result = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
        if (string.IsNullOrWhiteSpace(query))
        {
            return result;
        }

        var cleaned = query.TrimStart('?');
        foreach (var part in cleaned.Split('&', StringSplitOptions.RemoveEmptyEntries))
        {
            var split = part.Split('=', 2);
            var key = Uri.UnescapeDataString(split[0]);
            var value = split.Length > 1 ? Uri.UnescapeDataString(split[1]) : string.Empty;
            result[key] = value;
        }

        return result;
    }

    private static Exception CreateApiException(HttpStatusCode statusCode, string responseBody)
    {
        try
        {
            var envelope = JsonSerializer.Deserialize<ApiEnvelope<object>>(responseBody, JsonOptions);
            if (envelope?.Error is not null)
            {
                return new InvalidOperationException($"{(int)statusCode} {envelope.Error.Code}: {envelope.Error.Message}");
            }
        }
        catch
        {
        }

        return new InvalidOperationException($"{(int)statusCode} API error.");
    }
}
