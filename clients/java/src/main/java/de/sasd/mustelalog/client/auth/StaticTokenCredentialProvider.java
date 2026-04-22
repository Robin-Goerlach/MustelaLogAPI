package de.sasd.mustelalog.client.auth;

import java.util.Optional;

/**
 * Simple V1 credential provider that always returns one technical token from configuration.
 */
public final class StaticTokenCredentialProvider implements CredentialProvider
{
    private final String technicalToken;

    /**
     * Creates the provider.
     *
     * @param technicalToken configured token
     */
    public StaticTokenCredentialProvider(String technicalToken)
    {
        this.technicalToken = technicalToken == null ? "" : technicalToken.trim();
    }

    @Override
    public Optional<String> getBearerToken()
    {
        return technicalToken.isBlank() ? Optional.empty() : Optional.of(technicalToken);
    }
}
