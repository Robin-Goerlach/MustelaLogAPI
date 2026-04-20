<?php

declare(strict_types=1);

namespace App\Security;

use App\Error\ApiException;

/**
 * Prüft Scope-basierte Autorisierung.
 */
final class AuthorizationService
{
    /**
     * @param list<string> $availableScopes
     * @param list<string> $requiredScopes
     *
     * @throws ApiException
     */
    public function assertScopes(array $availableScopes, array $requiredScopes): void
    {
        foreach ($requiredScopes as $requiredScope) {
            if (!in_array($requiredScope, $availableScopes, true)) {
                throw new ApiException(403, 'forbidden', 'The authenticated principal is not allowed to perform this action.');
            }
        }
    }
}
