<?php

declare(strict_types=1);

namespace App\Audit;

use App\Http\Request;
use App\Support\FileLogger;
use PDO;
use Throwable;

/**
 * Audit-Logger für sicherheits- und zugriffsrelevante Vorgänge.
 *
 * Audit-Logs werden getrennt von rein technischen Fehlerlogs behandelt, damit
 * sie später sauber ausgewertet und geschützt werden können.
 */
final class AuditLogger
{
    public function __construct(
        private readonly PDO $pdo,
        private readonly FileLogger $fileLogger
    ) {
    }

    /**
     * @param array<string, mixed> $details
     */
    public function logSuccess(
        Request $request,
        string $operation,
        string $actorType,
        string $actorId,
        array $details = []
    ): void {
        $this->write($request, $operation, $actorType, $actorId, true, null, $details);
    }

    /**
     * @param array<string, mixed> $details
     */
    public function logFailure(
        Request $request,
        string $operation,
        string $failureReason,
        array $details = []
    ): void {
        $this->write($request, $operation, 'anonymous', 'anonymous', false, $failureReason, $details);
    }

    /**
     * @param array<string, mixed> $details
     */
    private function write(
        Request $request,
        string $operation,
        string $actorType,
        string $actorId,
        bool $success,
        ?string $failureReason,
        array $details
    ): void {
        try {
            $principal = $request->getAuthenticatedPrincipal();
            $tenantId = is_object($principal) && property_exists($principal, 'tenantId')
                ? (string) $principal->tenantId
                : null;

            $stmt = $this->pdo->prepare(
                'INSERT INTO access_audit (
                    audit_id,
                    tenant_id,
                    actor_type,
                    actor_id,
                    remote_ip,
                    operation_name,
                    resource_path,
                    request_id,
                    success_flag,
                    failure_reason,
                    details_json,
                    occurred_at
                ) VALUES (
                    :audit_id,
                    :tenant_id,
                    :actor_type,
                    :actor_id,
                    :remote_ip,
                    :operation_name,
                    :resource_path,
                    :request_id,
                    :success_flag,
                    :failure_reason,
                    :details_json,
                    UTC_TIMESTAMP(6)
                )'
            );

            $stmt->execute([
                'audit_id' => strtoupper(substr(bin2hex(random_bytes(16)), 0, 26)),
                'tenant_id' => $tenantId,
                'actor_type' => $actorType,
                'actor_id' => $actorId,
                'remote_ip' => $request->getRemoteIp() !== '' ? $request->getRemoteIp() : null,
                'operation_name' => $operation,
                'resource_path' => $request->getRoutePath(),
                'request_id' => $request->getRequestId(),
                'success_flag' => $success ? 1 : 0,
                'failure_reason' => $failureReason,
                'details_json' => json_encode($details, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
            ]);
        } catch (Throwable $throwable) {
            // Audit-Probleme dürfen die eigentliche API-Antwort nicht zwangsläufig zerstören.
            $this->fileLogger->warning('Audit logging failed', [
                'message' => $throwable->getMessage(),
                'request_id' => $request->getRequestId(),
            ]);
        }
    }
}
