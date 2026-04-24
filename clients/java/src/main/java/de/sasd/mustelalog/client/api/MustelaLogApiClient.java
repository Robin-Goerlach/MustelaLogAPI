package de.sasd.mustelalog.client.api;

import de.sasd.mustelalog.client.auth.CredentialProvider;
import de.sasd.mustelalog.client.config.ApiSettings;
import de.sasd.mustelalog.client.json.JsonSupport;
import de.sasd.mustelalog.client.json.SimpleJson;
import de.sasd.mustelalog.client.logging.ClientLogger;
import de.sasd.mustelalog.client.model.EventListPage;
import de.sasd.mustelalog.client.model.EventQueryFilter;
import de.sasd.mustelalog.client.model.HealthStatus;
import de.sasd.mustelalog.client.model.LogEventRecord;
import de.sasd.mustelalog.client.model.SourceSummary;
import de.sasd.mustelalog.client.model.TestLogEventRequest;
import de.sasd.mustelalog.client.service.TimeService;
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
import java.util.Optional;

public final class MustelaLogApiClient {
    private final HttpClient httpClient;
    private final ApiSettings settings;
    private final CredentialProvider credentialProvider;
    private final ClientLogger logger;
    private final TimeService timeService;

    public MustelaLogApiClient(HttpClient httpClient,
                               ApiSettings settings,
                               CredentialProvider credentialProvider,
                               ClientLogger logger,
                               TimeService timeService) {
        this.httpClient = httpClient;
        this.settings = settings;
        this.credentialProvider = credentialProvider;
        this.logger = logger;
        this.timeService = timeService;
    }

    public HealthStatus getHealth() throws ApiClientException {
        Map<String, Object> response = getObject(ApiRequestPurpose.NONE, "/health", Map.of());
        return new HealthStatus(
                booleanValue(response.get("ok"), false),
                stringValue(response.get("service")),
                stringValue(response.get("version")),
                stringValue(response.get("requestId")),
                stringValue(response.get("timestamp")));
    }

    public EventListPage getEvents(EventQueryFilter filter,
                                   int page,
                                   int pageSize,
                                   String sortField,
                                   boolean sortAscending) throws ApiClientException {
        LinkedHashMap<String, String> query = new LinkedHashMap<>();
        query.put("page", String.valueOf(Math.max(1, page)));
        query.put("pageSize", String.valueOf(Math.max(1, Math.min(200, pageSize))));
        add(query, "sourceKey", filter.getSourceKey());
        add(query, "severityText", filter.getSeverityText());
        add(query, "traceId", filter.getTraceId());
        add(query, "correlationId", filter.getCorrelationId());
        add(query, "from", timeService.normalizeQueryFrom(filter.getFrom()));
        add(query, "to", timeService.normalizeQueryTo(filter.getTo()));
        add(query, "sort", normalizeSort(sortField));
        query.put("direction", sortAscending ? "ASC" : "DESC");

        Map<String, Object> response = getObject(ApiRequestPurpose.READ, "/events", query);
        Map<String, Object> data = getDataObject(response, "Event list response must contain a data object.");
        List<Object> rawItems = JsonSupport.asList(data.get("items"));
        ArrayList<LogEventRecord> items = new ArrayList<>();
        for (Object rawItem : rawItems) {
            if (rawItem instanceof Map<?, ?> map) {
                items.add(mapEvent(JsonSupport.asObject(map, "Event item must be an object.")));
            }
        }
        return new EventListPage(
                items,
                numberValue(data.get("total"), items.size()),
                numberValue(data.get("page"), page),
                numberValue(data.get("pageSize"), pageSize));
    }

    public LogEventRecord getEventById(String eventId) throws ApiClientException {
        Map<String, Object> response = getObject(ApiRequestPurpose.READ, "/events/" + encodePathSegment(eventId), Map.of());
        Object data = response.get("data");
        return mapEvent(JsonSupport.asObject(data, "Event detail response must contain an event object in data."));
    }

    public List<SourceSummary> getSources() throws ApiClientException {
        Map<String, Object> response = getObject(ApiRequestPurpose.READ, "/sources", Map.of());
        List<Object> items = JsonSupport.asList(response.get("data"));
        ArrayList<SourceSummary> result = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> map) {
                result.add(mapSource(JsonSupport.asObject(map, "Source item must be an object.")));
            }
        }
        return result;
    }

    public String sendTestLogEvent(TestLogEventRequest request) throws ApiClientException {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ApiClientException("message must not be empty.");
        }
        if (request.getSeverityText() == null || request.getSeverityText().isBlank()) {
            throw new ApiClientException("severityText must not be empty.");
        }
        if (request.getSeverityNumber() < 1 || request.getSeverityNumber() > 24) {
            throw new ApiClientException("severityNumber must be between 1 and 24.");
        }
        if (request.getOccurredAt().isBlank()) request.setOccurredAt(timeService.currentUtcTimestamp());
        if (request.getObservedAt().isBlank()) request.setObservedAt(timeService.currentUtcTimestamp());

        Object attributesObject;
        try {
            attributesObject = SimpleJson.parse(request.getAttributesJsonText());
        } catch (RuntimeException exception) {
            throw new ApiClientException("attributes JSON is invalid.", exception);
        }
        if (!(attributesObject instanceof Map<?, ?>)) {
            throw new ApiClientException("attributes JSON must be a JSON object.");
        }

        Map<String, Object> payload = Map.of("events", List.of(request.toMap(attributesObject)));
        Map<String, Object> response = postObject(ApiRequestPurpose.INGEST, "/ingest/events", payload);
        Map<String, Object> data = getDataObject(response, "Ingest response must contain a data object.");
        return stringValue(data.get("ingestRequestId"));
    }

    public List<LogEventRecord> getRelatedEvents(LogEventRecord selectedRecord, int pageSize) throws ApiClientException {
        EventQueryFilter filter = new EventQueryFilter();
        if (selectedRecord.getCorrelationId() != null && !selectedRecord.getCorrelationId().isBlank()) {
            filter.setCorrelationId(selectedRecord.getCorrelationId());
        } else if (selectedRecord.getTraceId() != null && !selectedRecord.getTraceId().isBlank()) {
            filter.setTraceId(selectedRecord.getTraceId());
        } else {
            return List.of();
        }
        return getEvents(filter, 1, pageSize, "occurredAt", false).items();
    }

    private Map<String, Object> getObject(ApiRequestPurpose purpose, String routePath, Map<String, String> query) throws ApiClientException {
        return sendForObject(buildRequest("GET", purpose, routePath, query, null));
    }

    private Map<String, Object> postObject(ApiRequestPurpose purpose, String routePath, Map<String, Object> payload) throws ApiClientException {
        return sendForObject(buildRequest("POST", purpose, routePath, Map.of(), SimpleJson.stringify(payload)));
    }

    private HttpRequest buildRequest(String method,
                                     ApiRequestPurpose purpose,
                                     String routePath,
                                     Map<String, String> query,
                                     String body) throws ApiClientException {
        String url = buildUrl(routePath, query);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(Math.max(5, settings.getTimeoutSeconds())))
                .header("Accept", "application/json");

        Optional<String> token = credentialProvider.getBearerToken(purpose);
        if (purpose != ApiRequestPurpose.NONE && token.isEmpty()) {
            throw new ApiClientException("No bearer token configured for request purpose " + purpose + ".");
        }
        token.ifPresent(value -> builder.header("Authorization", "Bearer " + value));

        if ("POST".equalsIgnoreCase(method)) {
            builder.header("Content-Type", "application/json");
            builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8));
        } else {
            builder.GET();
        }

        logger.debug("HTTP request prepared", Map.of("method", method, "purpose", purpose.name(), "url", url));
        return builder.build();
    }

    private Map<String, Object> sendForObject(HttpRequest request) throws ApiClientException {
        long startedAt = System.nanoTime();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            long durationMillis = (System.nanoTime() - startedAt) / 1_000_000L;
            logger.information("HTTP response received", Map.of("statusCode", response.statusCode(), "durationMs", durationMillis, "uri", request.uri().toString()));
            Map<String, Object> root = parseResponseObject(response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw buildApiError(response.statusCode(), root, response.body());
            }
            return root;
        } catch (IOException exception) {
            throw new ApiClientException("API call failed: " + exception.getMessage(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiClientException("API call interrupted.", exception);
        }
    }

    private Map<String, Object> parseResponseObject(String body) throws ApiClientException {
        try {
            return JsonSupport.asObject(SimpleJson.parse(body), "API response is not a JSON object.");
        } catch (RuntimeException exception) {
            throw new ApiClientException("API response could not be parsed: " + exception.getMessage(), exception);
        }
    }

    private ApiClientException buildApiError(int statusCode, Map<String, Object> root, String responseBody) {
        String requestId = stringValue(root.get("requestId"));
        String errorCode = "";
        String message = "API call failed with status " + statusCode + ".";
        Object error = root.get("error");
        if (error instanceof Map<?, ?> errorMap) {
            Map<String, Object> object = JsonSupport.asObject(errorMap, "error");
            errorCode = stringValue(object.get("code"));
            String publicMessage = stringValue(object.get("message"));
            if (!publicMessage.isBlank()) message = publicMessage;
            if (requestId.isBlank()) requestId = stringValue(object.get("requestId"));
        }
        return new ApiClientException(message, statusCode, errorCode, requestId, responseBody);
    }

    private Map<String, Object> getDataObject(Map<String, Object> root, String message) throws ApiClientException {
        Object data = root.get("data");
        if (!(data instanceof Map<?, ?> map)) throw new ApiClientException(message);
        return JsonSupport.asObject(map, message);
    }

    private LogEventRecord mapEvent(Map<String, Object> row) {
        LogEventRecord item = new LogEventRecord();
        item.setLogEventId(aliasString(row, "logEventId", "log_event_id", "eventId", "id"));
        item.setOccurredAt(aliasString(row, "occurredAt", "occurred_at"));
        item.setObservedAt(aliasString(row, "observedAt", "observed_at"));
        item.setReceivedAt(aliasString(row, "receivedAt", "received_at"));
        item.setIngestedAt(aliasString(row, "ingestedAt", "ingested_at"));
        item.setSeverityNumber(aliasNumber(row, "severityNumber", "severity_number"));
        item.setSeverityText(aliasString(row, "severityText", "severity_text"));
        item.setEventName(aliasString(row, "eventName", "event_name"));
        item.setEventCategory(aliasString(row, "eventCategory", "event_category"));
        item.setEventAction(aliasString(row, "eventAction", "event_action"));
        item.setEventOutcome(aliasString(row, "eventOutcome", "event_outcome"));
        item.setMessageText(aliasString(row, "messageText", "message_text", "message"));
        item.setAttributesJson(aliasObject(row, "attributes", "attributesJson", "attributes_json"));
        item.setRawPayloadJson(aliasObject(row, "rawPayloadJson", "raw_payload_json"));
        item.setSourceId(aliasString(row, "sourceId", "source_id"));
        item.setSourceKey(aliasString(row, "sourceKey", "source_key"));
        item.setSourceName(aliasString(row, "sourceName", "source_name"));
        item.setSourceType(aliasString(row, "sourceType", "source_type"));
        item.setEnvironmentCode(aliasString(row, "environmentCode", "environment", "environment_code"));
        item.setHostName(aliasString(row, "hostName", "host_name", "hostname"));
        item.setServiceName(aliasString(row, "serviceName", "service_name"));
        item.setComponentName(aliasString(row, "componentName", "component_name"));
        item.setModuleName(aliasString(row, "moduleName", "module_name"));
        item.setProcessName(aliasString(row, "processName", "process_name"));
        item.setProcessPid(aliasNumber(row, "processPid", "process_pid"));
        item.setThreadId(aliasString(row, "threadId", "thread_id"));
        item.setActorUserId(aliasString(row, "actorUserId", "actor_user_id"));
        item.setActorPrincipal(aliasString(row, "actorPrincipal", "actor_principal"));
        item.setSessionHashSha256(aliasString(row, "sessionHashSha256", "session_hash_sha256"));
        item.setClientIp(aliasString(row, "clientIp", "client_ip"));
        item.setServerIp(aliasString(row, "serverIp", "server_ip"));
        item.setTraceId(aliasString(row, "traceId", "trace_id"));
        item.setCorrelationId(aliasString(row, "correlationId", "correlation_id"));
        item.setRequestCorrelationId(aliasString(row, "requestCorrelationId", "request_correlation_id"));
        item.setClassificationCode(aliasString(row, "classificationCode", "classification_code"));
        item.setRetentionPolicyCode(aliasString(row, "retentionPolicyCode", "retention_policy_code"));
        item.setCanonicalHashSha256(aliasString(row, "canonicalHashSha256", "canonical_hash_sha256"));
        item.setPreviousHashSha256(aliasString(row, "previousHashSha256", "previous_hash_sha256"));
        item.setSourceSignature(aliasString(row, "sourceSignature", "source_signature"));
        item.setSignatureAlgorithm(aliasString(row, "signatureAlgorithm", "signature_algorithm"));
        item.setRawFields(new LinkedHashMap<>(row));
        return item;
    }

    private SourceSummary mapSource(Map<String, Object> row) {
        return new SourceSummary(
                aliasString(row, "sourceId", "source_id"),
                aliasString(row, "sourceKey", "source_key"),
                aliasString(row, "sourceName", "source_name"),
                aliasString(row, "sourceType", "source_type"),
                aliasString(row, "environment", "environmentCode", "environment_code"),
                aliasString(row, "hostName", "host_name", "hostname"),
                aliasString(row, "serviceName", "service_name"),
                aliasBoolean(row, true, "active"),
                new LinkedHashMap<>(row));
    }

    private String aliasString(Map<String, Object> map, String... keys) {
        for (String key : keys) if (map.containsKey(key) && map.get(key) != null) return String.valueOf(map.get(key));
        return "";
    }

    private Object aliasObject(Map<String, Object> map, String... keys) {
        for (String key : keys) if (map.containsKey(key)) return map.get(key);
        return null;
    }

    private Integer aliasNumber(Map<String, Object> map, String... keys) {
        for (String key : keys) if (map.containsKey(key)) return numberValueOrNull(map.get(key));
        return null;
    }

    private boolean aliasBoolean(Map<String, Object> map, boolean fallback, String... keys) {
        for (String key : keys) if (map.containsKey(key)) return booleanValue(map.get(key), fallback);
        return fallback;
    }

    private void add(Map<String, String> query, String key, String value) {
        if (value != null && !value.isBlank()) query.put(key, value);
    }

    private String normalizeSort(String sortField) {
        return switch (sortField == null ? "" : sortField) {
            case "severityNumber", "sourceKey" -> sortField;
            default -> "occurredAt";
        };
    }

    private String buildUrl(String routePath, Map<String, String> query) {
        String baseUrl = settings.getBaseUrl();
        StringBuilder builder = new StringBuilder(baseUrl);
        char separator = baseUrl.contains("?") ? '&' : '?';
        builder.append(separator).append(urlEncode(settings.getRouteParameterName())).append('=').append(urlEncode(settings.getApiVersionPath() + routePath));
        for (Map.Entry<String, String> entry : query.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                builder.append('&').append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
            }
        }
        return builder.toString();
    }

    private String encodePathSegment(String input) { return input == null ? "" : input.replace("/", "%2F"); }
    private String urlEncode(String input) { return URLEncoder.encode(input, StandardCharsets.UTF_8); }
    private String stringValue(Object input) { return input == null ? "" : String.valueOf(input); }
    private int numberValue(Object input, int fallback) {
        if (input instanceof Number number) return number.intValue();
        if (input instanceof BigDecimal decimal) return decimal.intValue();
        try { return Integer.parseInt(String.valueOf(input)); } catch (Exception ignored) { return fallback; }
    }
    private Integer numberValueOrNull(Object input) { return input == null ? null : numberValue(input, 0); }
    private boolean booleanValue(Object input, boolean fallback) {
        if (input == null) return fallback;
        if (input instanceof Boolean bool) return bool;
        return Boolean.parseBoolean(String.valueOf(input));
    }
}
