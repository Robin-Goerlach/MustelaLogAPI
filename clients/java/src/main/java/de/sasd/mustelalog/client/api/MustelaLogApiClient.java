package de.sasd.mustelalog.client.api;

import de.sasd.mustelalog.client.auth.CredentialProvider;
import de.sasd.mustelalog.client.config.ApiSettings;
import de.sasd.mustelalog.client.json.SimpleJson;
import de.sasd.mustelalog.client.logging.ClientLogger;
import de.sasd.mustelalog.client.model.EventListResponse;
import de.sasd.mustelalog.client.model.EventQueryFilter;
import de.sasd.mustelalog.client.model.LogEventRecord;
import de.sasd.mustelalog.client.model.SourceSummary;
import de.sasd.mustelalog.client.model.TestLogEventRequest;
import de.sasd.mustelalog.client.service.TimeDisplayService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin HTTP client for MustelaLogAPI.
 *
 * <p>The class intentionally keeps the transport logic separate from Swing code so that UI classes
 * stay readable. It also makes later authentication changes easier because the credential provider
 * is already abstracted.</p>
 */
public final class MustelaLogApiClient
{
    private final HttpClient httpClient;
    private final ApiSettings settings;
    private final CredentialProvider credentialProvider;
    private final ClientLogger logger;
    private final TimeDisplayService timeDisplayService;

    public MustelaLogApiClient(HttpClient httpClient, ApiSettings settings, CredentialProvider credentialProvider,
                               ClientLogger logger, TimeDisplayService timeDisplayService)
    {
        this.httpClient = httpClient;
        this.settings = settings;
        this.credentialProvider = credentialProvider;
        this.logger = logger;
        this.timeDisplayService = timeDisplayService;
    }

    @SuppressWarnings("unchecked")
    public String getHealth() throws ApiClientException
    {
        Map<String, Object> response = getObject("/health", Map.of());
        return stringValue(response.getOrDefault("message", response.getOrDefault("status", "ok")));
    }

    @SuppressWarnings("unchecked")
    public EventListResponse getEvents(EventQueryFilter filter, int page, int pageSize, String sortColumn, boolean sortAscending) throws ApiClientException
    {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("page", String.valueOf(page));
        query.put("pageSize", String.valueOf(pageSize));
        add(query, "sourceKey", filter.getSourceKey());
        add(query, "severityText", filter.getSeverity());
        add(query, "traceId", filter.getTraceId());
        add(query, "correlationId", filter.getCorrelationId());
        add(query, "from", timeDisplayService.toApiQueryDateTime(filter.getFromLocal()));
        add(query, "to", timeDisplayService.toApiQueryEndOfDay(filter.getToLocal()));
        add(query, "sort", sortColumn);
        add(query, "direction", sortAscending ? "asc" : "desc");

        Map<String, Object> response = getObject("/events", query);
        List<LogEventRecord> items = new ArrayList<>();
        Object itemsValue = response.get("items");
        if (itemsValue instanceof List<?> list)
        {
            for (Object item : list)
            {
                if (item instanceof Map<?, ?> map)
                {
                    items.add(mapEvent((Map<String, Object>) map));
                }
            }
        }
        return new EventListResponse(items, numberValue(response.get("total"), items.size()), numberValue(response.get("page"), page), numberValue(response.get("pageSize"), pageSize));
    }

    @SuppressWarnings("unchecked")
    public LogEventRecord getEventById(String eventId) throws ApiClientException
    {
        Map<String, Object> response = getObject("/events/" + encodePathSegment(eventId), Map.of());
        Object item = response.get("item");
        return item instanceof Map<?, ?> map ? mapEvent((Map<String, Object>) map) : mapEvent(response);
    }

    @SuppressWarnings("unchecked")
    public List<SourceSummary> getSources() throws ApiClientException
    {
        Map<String, Object> response = getObject("/sources", Map.of());
        List<SourceSummary> result = new ArrayList<>();
        Object itemsValue = response.get("items");
        if (itemsValue instanceof List<?> list)
        {
            for (Object item : list)
            {
                if (item instanceof Map<?, ?> map)
                {
                    result.add(mapSource((Map<String, Object>) map));
                }
            }
        }
        return result;
    }

    public void sendTestLogEvent(TestLogEventRequest request) throws ApiClientException
    {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("severityText", request.getSeverityText());
        event.put("severityNumber", request.getSeverityNumber());
        event.put("message", request.getMessage());
        event.put("eventName", request.getEventName());
        event.put("eventCategory", request.getEventCategory());
        event.put("eventAction", request.getEventAction());
        if (request.getSourceKey() != null && !request.getSourceKey().isBlank()) event.put("sourceKey", request.getSourceKey());
        if (request.getCorrelationId() != null && !request.getCorrelationId().isBlank()) event.put("correlationId", request.getCorrelationId());
        try
        {
            Object attributes = request.getAttributesJsonText() == null || request.getAttributesJsonText().isBlank() ? Map.of() : SimpleJson.parse(request.getAttributesJsonText());
            event.put("attributes", attributes);
        }
        catch (Exception exception)
        {
            throw new ApiClientException("Attributes JSON is invalid.", exception);
        }
        postObject("/ingest/events", Map.of("events", List.of(event)));
    }

    private Map<String, Object> getObject(String routePath, Map<String, String> query) throws ApiClientException
    {
        return sendForObject(buildRequest("GET", routePath, query, null));
    }

    private void postObject(String routePath, Map<String, Object> payload) throws ApiClientException
    {
        sendForObject(buildRequest("POST", routePath, Map.of(), SimpleJson.stringify(payload)));
    }

    private HttpRequest buildRequest(String method, String routePath, Map<String, String> query, String body)
    {
        String url = buildUrl(routePath, query);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(Math.max(5, settings.getTimeoutSeconds())))
            .header("Accept", "application/json");
        credentialProvider.getBearerToken().ifPresent(token -> builder.header("Authorization", "Bearer " + token));
        if ("POST".equalsIgnoreCase(method))
        {
            builder.header("Content-Type", "application/json");
            builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8));
        }
        else builder.GET();
        logger.debug("HTTP request prepared", Map.of("method", method, "url", url));
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sendForObject(HttpRequest request) throws ApiClientException
    {
        long startNanos = System.nanoTime();
        try
        {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            long durationMillis = (System.nanoTime() - startNanos) / 1_000_000L;
            logger.information("HTTP response received", Map.of("statusCode", response.statusCode(), "durationMs", durationMillis, "uri", request.uri().toString()));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ApiClientException("API call failed with status " + response.statusCode() + ": " + response.body());
            }
            Object parsed = SimpleJson.parse(response.body());
            if (parsed instanceof Map<?, ?> map) return (Map<String, Object>) map;
            throw new ApiClientException("API response is not a JSON object.");
        }
        catch (IOException exception)
        {
            throw new ApiClientException("API call failed: " + exception.getMessage(), exception);
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
            throw new ApiClientException("API call interrupted.", exception);
        }
        catch (RuntimeException exception)
        {
            throw new ApiClientException("API response could not be parsed: " + exception.getMessage(), exception);
        }
    }

    private String buildUrl(String routePath, Map<String, String> query)
    {
        String baseUrl = settings.getBaseUrl();
        StringBuilder builder = new StringBuilder(baseUrl);
        char separator = baseUrl.contains("?") ? '&' : '?';
        builder.append(separator).append(urlEncode(settings.getRouteParameterName())).append('=').append(urlEncode(settings.getApiVersionPath() + routePath));
        for (Map.Entry<String, String> entry : query.entrySet())
        {
            if (entry.getValue() != null && !entry.getValue().isBlank())
            {
                builder.append('&').append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
            }
        }
        return builder.toString();
    }

    private LogEventRecord mapEvent(Map<String, Object> row)
    {
        LogEventRecord item = new LogEventRecord();
        item.setLogEventId(stringValue(row.get("logEventId")));
        item.setOccurredAt(stringValue(row.get("occurredAt")));
        item.setObservedAt(stringValue(row.get("observedAt")));
        item.setReceivedAt(stringValue(row.get("receivedAt")));
        item.setIngestedAt(stringValue(row.get("ingestedAt")));
        item.setSeverityNumber(numberValueOrNull(row.get("severityNumber")));
        item.setSeverityText(stringValue(row.get("severityText")));
        item.setEventName(stringValue(row.get("eventName")));
        item.setEventCategory(stringValue(row.get("eventCategory")));
        item.setEventAction(stringValue(row.get("eventAction")));
        item.setEventOutcome(stringValue(row.get("eventOutcome")));
        item.setMessageText(stringValue(row.get("messageText")));
        item.setAttributesJson(row.get("attributes"));
        item.setRawPayloadJson(row.get("rawPayloadJson"));
        item.setSourceId(stringValue(row.get("sourceId")));
        item.setSourceKey(stringValue(row.get("sourceKey")));
        item.setSourceName(stringValue(row.get("sourceName")));
        item.setSourceType(stringValue(row.get("sourceType")));
        item.setEnvironmentCode(stringValue(row.get("environmentCode")));
        item.setHostName(stringValue(row.get("hostName")));
        item.setServiceName(stringValue(row.get("serviceName")));
        item.setComponentName(stringValue(row.get("componentName")));
        item.setModuleName(stringValue(row.get("moduleName")));
        item.setProcessName(stringValue(row.get("processName")));
        item.setProcessPid(numberValueOrNull(row.get("processPid")));
        item.setThreadId(stringValue(row.get("threadId")));
        item.setActorUserId(stringValue(row.get("actorUserId")));
        item.setActorPrincipal(stringValue(row.get("actorPrincipal")));
        item.setSessionHashSha256(stringValue(row.get("sessionHashSha256")));
        item.setClientIp(stringValue(row.get("clientIp")));
        item.setServerIp(stringValue(row.get("serverIp")));
        item.setTraceId(stringValue(row.get("traceId")));
        item.setSpanId(stringValue(row.get("spanId")));
        item.setCorrelationId(stringValue(row.get("correlationId")));
        item.setRequestCorrelationId(stringValue(row.get("requestCorrelationId")));
        item.setClassificationCode(stringValue(row.get("classificationCode")));
        item.setRetentionPolicyCode(stringValue(row.get("retentionPolicyCode")));
        item.setLegalHoldFlag(booleanValueOrNull(row.get("legalHoldFlag")));
        item.setCanonicalHashSha256(stringValue(row.get("canonicalHashSha256")));
        item.setPreviousHashSha256(stringValue(row.get("previousHashSha256")));
        item.setSourceSignature(stringValue(row.get("sourceSignature")));
        item.setSignatureAlgorithm(stringValue(row.get("signatureAlgorithm")));
        return item;
    }

    private SourceSummary mapSource(Map<String, Object> row)
    {
        return new SourceSummary(
            stringValue(row.get("sourceId")),
            stringValue(row.get("sourceKey")),
            stringValue(row.get("sourceName")),
            stringValue(row.get("sourceType")),
            stringValue(row.get("environment")),
            stringValue(row.get("hostname")),
            stringValue(row.get("serviceName")),
            stringValue(row.get("versionText")),
            boolValue(row.get("active"), true));
    }

    private void add(Map<String, String> query, String key, String value)
    {
        if (value != null && !value.isBlank()) query.put(key, value);
    }

    private String encodePathSegment(String input) { return input == null ? "" : input.replace("/", "%2F"); }
    private String urlEncode(String input) { return URLEncoder.encode(input, StandardCharsets.UTF_8); }
    private String stringValue(Object input) { return input == null ? "" : String.valueOf(input); }
    private int numberValue(Object input, int fallback)
    {
        if (input instanceof Number number) return number.intValue();
        if (input instanceof BigDecimal decimal) return decimal.intValue();
        try { return Integer.parseInt(String.valueOf(input)); } catch (Exception ignored) { return fallback; }
    }
    private Integer numberValueOrNull(Object input) { return input == null ? null : numberValue(input, 0); }
    private Boolean booleanValueOrNull(Object input) { return input == null ? null : Boolean.parseBoolean(String.valueOf(input)); }
    private boolean boolValue(Object input, boolean fallback) { return input == null ? fallback : Boolean.parseBoolean(String.valueOf(input)); }
}
