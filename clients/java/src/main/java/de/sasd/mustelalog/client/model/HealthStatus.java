package de.sasd.mustelalog.client.model;

public record HealthStatus(boolean ok, String service, String version, String requestId, String timestamp) {
}
