<?php

declare(strict_types=1);

namespace App\Service;

use App\Repository\SourceReadRepository;

/**
 * Fachservice für Quellen-Lesezugriffe.
 */
final class SourceService
{
    public function __construct(private readonly SourceReadRepository $sourceReadRepository)
    {
    }

    /**
     * @return list<array<string,mixed>>
     */
    public function listSources(string $tenantId): array
    {
        return $this->sourceReadRepository->findSourcesByTenant($tenantId);
    }
}
