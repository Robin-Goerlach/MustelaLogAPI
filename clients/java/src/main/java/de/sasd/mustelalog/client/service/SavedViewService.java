package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.json.SimpleJson;
import de.sasd.mustelalog.client.model.EventQueryFilter;
import de.sasd.mustelalog.client.model.SavedViewDefinition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores saved views locally in a small JSON file.
 */
public final class SavedViewService
{
    private final Path storagePath;

    public SavedViewService(Path storagePath) { this.storagePath = storagePath; }

    @SuppressWarnings("unchecked")
    public List<SavedViewDefinition> loadAll()
    {
        try
        {
            if (!Files.exists(storagePath)) return new ArrayList<>();
            Object parsed = SimpleJson.parse(Files.readString(storagePath, StandardCharsets.UTF_8));
            if (!(parsed instanceof List<?> list)) return new ArrayList<>();
            List<SavedViewDefinition> result = new ArrayList<>();
            for (Object item : list)
            {
                if (item instanceof Map<?, ?> map) result.add(fromMap((Map<String, Object>) map));
            }
            result.sort(Comparator.comparing(SavedViewDefinition::getName, String.CASE_INSENSITIVE_ORDER));
            return result;
        }
        catch (Exception ignored)
        {
            return new ArrayList<>();
        }
    }

    public void save(SavedViewDefinition definition) throws IOException
    {
        List<SavedViewDefinition> items = loadAll();
        items.removeIf(item -> item.getName().equalsIgnoreCase(definition.getName()));
        items.add(definition);
        persist(items);
    }

    public void delete(String name) throws IOException
    {
        List<SavedViewDefinition> items = loadAll();
        items.removeIf(item -> item.getName().equalsIgnoreCase(name));
        persist(items);
    }

    public void rename(String oldName, String newName) throws IOException
    {
        List<SavedViewDefinition> items = loadAll();
        boolean targetExists = items.stream().anyMatch(item -> item.getName().equalsIgnoreCase(newName));
        if (targetExists) throw new IOException("A saved view with this name already exists.");
        for (SavedViewDefinition item : items) { if (item.getName().equalsIgnoreCase(oldName)) item.setName(newName); }
        persist(items);
    }

    private void persist(List<SavedViewDefinition> items) throws IOException
    {
        Files.createDirectories(storagePath.getParent());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SavedViewDefinition item : items) rows.add(toMap(item));
        Path tempPath = storagePath.resolveSibling(storagePath.getFileName() + ".tmp");
        Files.writeString(tempPath, SimpleJson.stringifyPretty(rows), StandardCharsets.UTF_8);
        Files.move(tempPath, storagePath, StandardCopyOption.REPLACE_EXISTING);
    }

    private Map<String, Object> toMap(SavedViewDefinition definition)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", definition.getName());
        row.put("sortColumn", definition.getSortColumn());
        row.put("sortAscending", definition.isSortAscending());
        row.put("visibleColumns", definition.getVisibleColumns());
        row.put("filter", filterToMap(definition.getFilter()));
        return row;
    }

    @SuppressWarnings("unchecked")
    private SavedViewDefinition fromMap(Map<String, Object> row)
    {
        SavedViewDefinition result = new SavedViewDefinition();
        result.setName(String.valueOf(row.getOrDefault("name", "Unnamed")));
        result.setSortColumn(String.valueOf(row.getOrDefault("sortColumn", "occurredAt")));
        result.setSortAscending(Boolean.parseBoolean(String.valueOf(row.getOrDefault("sortAscending", Boolean.FALSE))));
        Object columnsValue = row.get("visibleColumns");
        if (columnsValue instanceof Map<?, ?> map)
        {
            Map<String, Boolean> visibility = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) visibility.put(String.valueOf(entry.getKey()), Boolean.parseBoolean(String.valueOf(entry.getValue())));
            result.setVisibleColumns(visibility);
        }
        Object filterValue = row.get("filter");
        if (filterValue instanceof Map<?, ?> map) result.setFilter(filterFromMap((Map<String, Object>) map));
        return result;
    }

    private Map<String, Object> filterToMap(EventQueryFilter filter)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("fromLocal", filter.getFromLocal() == null ? null : filter.getFromLocal().toString());
        row.put("toLocal", filter.getToLocal() == null ? null : filter.getToLocal().toString());
        row.put("sourceKey", filter.getSourceKey());
        row.put("hostname", filter.getHostname());
        row.put("service", filter.getService());
        row.put("severity", filter.getSeverity());
        row.put("eventCategory", filter.getEventCategory());
        row.put("eventAction", filter.getEventAction());
        row.put("eventOutcome", filter.getEventOutcome());
        row.put("textSearch", filter.getTextSearch());
        row.put("correlationId", filter.getCorrelationId());
        row.put("traceId", filter.getTraceId());
        row.put("requestId", filter.getRequestId());
        row.put("component", filter.getComponent());
        row.put("actorUserId", filter.getActorUserId());
        row.put("actorPrincipal", filter.getActorPrincipal());
        row.put("sessionHash", filter.getSessionHash());
        row.put("clientIp", filter.getClientIp());
        row.put("serverIp", filter.getServerIp());
        row.put("onlyWithPayload", filter.isOnlyWithPayload());
        row.put("onlyWithCorrelation", filter.isOnlyWithCorrelation());
        row.put("onlyWithActor", filter.isOnlyWithActor());
        return row;
    }

    private EventQueryFilter filterFromMap(Map<String, Object> row)
    {
        EventQueryFilter filter = new EventQueryFilter();
        filter.setFromLocal(parseLocalDateTime(row.get("fromLocal")));
        filter.setToLocal(parseLocalDateTime(row.get("toLocal")));
        filter.setSourceKey(asString(row.get("sourceKey")));
        filter.setHostname(asString(row.get("hostname")));
        filter.setService(asString(row.get("service")));
        filter.setSeverity(asString(row.get("severity")));
        filter.setEventCategory(asString(row.get("eventCategory")));
        filter.setEventAction(asString(row.get("eventAction")));
        filter.setEventOutcome(asString(row.get("eventOutcome")));
        filter.setTextSearch(asString(row.get("textSearch")));
        filter.setCorrelationId(asString(row.get("correlationId")));
        filter.setTraceId(asString(row.get("traceId")));
        filter.setRequestId(asString(row.get("requestId")));
        filter.setComponent(asString(row.get("component")));
        filter.setActorUserId(asString(row.get("actorUserId")));
        filter.setActorPrincipal(asString(row.get("actorPrincipal")));
        filter.setSessionHash(asString(row.get("sessionHash")));
        filter.setClientIp(asString(row.get("clientIp")));
        filter.setServerIp(asString(row.get("serverIp")));
        filter.setOnlyWithPayload(Boolean.parseBoolean(asString(row.get("onlyWithPayload"))));
        filter.setOnlyWithCorrelation(Boolean.parseBoolean(asString(row.get("onlyWithCorrelation"))));
        filter.setOnlyWithActor(Boolean.parseBoolean(asString(row.get("onlyWithActor"))));
        return filter;
    }

    private LocalDateTime parseLocalDateTime(Object input) { return input == null || String.valueOf(input).isBlank() ? null : LocalDateTime.parse(String.valueOf(input)); }
    private String asString(Object input) { return input == null ? "" : String.valueOf(input); }
}
