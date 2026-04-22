package de.sasd.mustelalog.client.auth;

import java.util.Optional;

/**
 * Supplies credentials for outbound API calls.
 *
 * <p>V1 uses a simple technical token. The interface keeps the code open for later user login,
 * session handling, or token refresh workflows.</p>
 */
public interface CredentialProvider
{
    /**
     * Returns the currently available bearer token.
     *
     * @return bearer token if available
     */
    Optional<String> getBearerToken();
}
