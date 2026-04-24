package de.sasd.mustelalog.client.model;

import java.util.List;

public record EventListPage(List<LogEventRecord> items, int total, int page, int pageSize) {
}
