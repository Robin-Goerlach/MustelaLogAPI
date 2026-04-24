package de.sasd.mustelalog.client.auth;

import de.sasd.mustelalog.client.api.ApiRequestPurpose;
import java.util.Optional;

public interface CredentialProvider {
    Optional<String> getBearerToken(ApiRequestPurpose purpose);
}
