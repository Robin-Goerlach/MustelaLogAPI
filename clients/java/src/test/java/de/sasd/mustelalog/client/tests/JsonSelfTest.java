package de.sasd.mustelalog.client.tests;

import de.sasd.mustelalog.client.json.SimpleJson;
import java.util.List;
import java.util.Map;

public final class JsonSelfTest {
    private JsonSelfTest() {
    }

    public static void run() {
        Object parsed = SimpleJson.parse("{\"ok\":true,\"data\":{\"items\":[1,2,3],\"text\":\"hello\"}}");
        if (!(parsed instanceof Map<?, ?> map)) {
            throw new IllegalStateException("Expected parsed root object.");
        }
        if (!Boolean.TRUE.equals(map.get("ok"))) {
            throw new IllegalStateException("Boolean field not parsed correctly.");
        }
        Object dataObject = map.get("data");
        if (!(dataObject instanceof Map<?, ?> data)) {
            throw new IllegalStateException("Expected nested data object.");
        }
        Object itemsObject = data.get("items");
        if (!(itemsObject instanceof List<?> items) || items.size() != 3) {
            throw new IllegalStateException("Expected items array with three elements.");
        }
        String pretty = SimpleJson.pretty(parsed);
        if (!pretty.contains("\"items\"")) {
            throw new IllegalStateException("Pretty JSON output missing expected field.");
        }
    }
}
