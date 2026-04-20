<?php

declare(strict_types=1);

namespace App\Repository;

use PDO;

/**
 * Liest freigegebene Quelleninformationen.
 */
final class SourceReadRepository
{
    public function __construct(private readonly PDO $pdo)
    {
    }

    /**
     * @return list<array<string,mixed>>
     */
    public function findSourcesByTenant(string $tenantId): array
    {
        $stmt = $this->pdo->prepare(
            'SELECT * FROM api_v1_log_sources WHERE tenant_id = :tenant_id ORDER BY source_name ASC'
        );
        $stmt->execute(['tenant_id' => $tenantId]);

        $rows = $stmt->fetchAll();

        return is_array($rows) ? $rows : [];
    }
}
