<?php

declare(strict_types=1);

namespace App\Repository;

use App\Security\AuthenticatedPrincipal;
use PDO;

/**
 * Liest aktive API-Client-Credentials.
 */
final class ApiClientCredentialRepository
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
    c.client_id,
    c.tenant_id,
    c.client_name,
    c.active,
    c.revoked_at,
    cc.credential_id,
    cc.token_scopes_json
FROM api_clients c
INNER JOIN api_client_credentials cc ON cc.client_id = c.client_id
WHERE cc.credential_type = 'bearer_token'
  AND cc.token_hash_sha256 = :token_hash
  AND cc.active = 1
  AND (cc.revoked_at IS NULL)
  AND c.active = 1
  AND (c.revoked_at IS NULL)
LIMIT 1
SQL;

        $stmt = $this->pdo->prepare($sql);
        $stmt->execute(['token_hash' => $tokenHash]);
        $row = $stmt->fetch();

        if (!is_array($row)) {
            return null;
        }

        $scopes = json_decode((string) $row['token_scopes_json'], true);
        if (!is_array($scopes)) {
            $scopes = [];
        }

        return new AuthenticatedPrincipal(
            'client',
            (string) $row['credential_id'],
            (string) $row['client_name'],
            (string) $row['tenant_id'],
            array_values(array_filter(array_map('strval', $scopes))),
            [],
            null,
            (string) $row['client_id']
        );
    }
}
