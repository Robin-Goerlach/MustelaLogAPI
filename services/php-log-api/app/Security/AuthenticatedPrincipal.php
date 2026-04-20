<?php

declare(strict_types=1);

namespace App\Security;

/**
 * Repräsentiert den authentisierten Aufrufer.
 *
 * Ein Prinzipal kann entweder eine Logquelle (`source`) oder ein lesender
 * API-Client (`client`) sein.
 */
final class AuthenticatedPrincipal
{
    /**
     * @param list<string> $scopes
     * @param array<int, string> $allowedNetworks
     */
    public function __construct(
        public readonly string $principalType,
        public readonly string $principalId,
        public readonly string $displayName,
        public readonly string $tenantId,
        public readonly array $scopes,
        public readonly array $allowedNetworks = [],
        public readonly ?string $sourceId = null,
        public readonly ?string $clientId = null
    ) {
    }
}
