package de.sasd.mustelalog.client.auth;

import de.sasd.mustelalog.client.api.ApiRequestPurpose;
import java.util.Optional;

public final class StaticCredentialProvider implements CredentialProvider {
    private final String readBearerToken;
    private final String ingestBearerToken;

    public StaticCredentialProvider(String readBearerToken, String ingestBearerToken) {
        this.readBearerToken = readBearerToken == null ? "" : readBearerToken.trim();
        this.ingestBearerToken = ingestBearerToken == null ? "" : ingestBearerToken.trim();
    }

    @Override
    public Optional<String> getBearerToken(ApiRequestPurpose purpose) {
        return switch (purpose) {
            case READ -> optional(readBearerToken);
            case INGEST -> optional(ingestBearerToken);
            case NONE -> Optional.empty();
        };
    }

    private Optional<String> optional(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }
}
