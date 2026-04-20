<?php

declare(strict_types=1);

namespace App\Repository;

use App\Security\AuthenticatedPrincipal;
use PDO;

/**
 * Liest aktive Quellen-Credentials.
 */
final class SourceCredentialRepository
{
    public function __construct(
        private readonly PDO $pdo,
        private readonly \App\Config\Config $config
    ) {
    }

    public function findActivePrincipalByTokenHash(string $tokenHash): ?AuthenticatedPrincipal
    {
        $sql = <<<SQL
SELECT
    s.source_id,
    s.tenant_id,
    s.source_name,
    s.active,
    s.revoked_at,
    c.credential_id,
    c.token_scopes_json
FROM log_source_credentials c
INNER JOIN log_sources s ON s.source_id = c.source_id
WHERE c.credential_type = 'bearer_token'
  AND c.token_hash_sha256 = :token_hash
  AND c.active = 1
  AND (c.revoked_at IS NULL)
  AND s.active = 1
  AND (s.revoked_at IS NULL)
LIMIT 1
SQL;

        $stmt = $this->pdo->prepare($sql);
        $stmt->execute(['token_hash' => $tokenHash]);
        $row = $stmt->fetch();

        if (!is_array($row)) {
            return null;
        }

        $networksStmt = $this->pdo->prepare(
            'SELECT allowed_cidr FROM log_source_networks WHERE source_id = :source_id AND active = 1'
        );
        $networksStmt->execute(['source_id' => $row['source_id']]);
        $allowedNetworks = array_map(
            static fn (array $networkRow): string => (string) $networkRow['allowed_cidr'],
            $networksStmt->fetchAll()
        );

        $scopes = json_decode((string) $row['token_scopes_json'], true);
        if (!is_array($scopes)) {
            $scopes = [];
        }

        return new AuthenticatedPrincipal(
            'source',
            (string) $row['credential_id'],
            (string) $row['source_name'],
            (string) $row['tenant_id'],
            array_values(array_filter(array_map('strval', $scopes))),
            $allowedNetworks,
            (string) $row['source_id'],
            null
        );
    }
}
