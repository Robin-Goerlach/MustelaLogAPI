<?php

declare(strict_types=1);

namespace App\Repository;

use App\DTO\IngestEvent;
use PDO;
use Throwable;

/**
 * Schreibt Ingest-Requests und Log-Events.
 *
 * Der Repository-Layer kapselt die physische Tabellenstruktur. Controller und
 * Services sprechen bewusst nicht direkt SQL und sind dadurch weniger stark an
 * das Schema gekoppelt.
 */
final class EventWriteRepository
{
    public function __construct(private readonly PDO $pdo)
    {
    }

    /**
     * Persistiert einen gesamten Ingest-Request mitsamt Events transaktional.
     *
     * @param list<IngestEvent> $events
     *
     * @return array{ingestRequestId:string,eventIds:list<string>}
     */
    public function ingestBatch(
        string $tenantId,
        string $sourceId,
        string $remoteIp,
        string $requestId,
        array $events
    ): array {
        $ingestRequestId = self::generateUlid();
        $eventIds = [];

        $this->pdo->beginTransaction();

        try {
            $requestSql = <<<SQL
INSERT INTO ingest_requests (
    ingest_request_id,
    tenant_id,
    source_id,
    request_id,
    remote_ip,
    auth_result,
    event_count,
    accepted_count,
    rejected_count,
    received_at,
    ingested_at
) VALUES (
    :ingest_request_id,
    :tenant_id,
    :source_id,
    :request_id,
    :remote_ip,
    'accepted',
    :event_count,
    :accepted_count,
    0,
    UTC_TIMESTAMP(6),
    UTC_TIMESTAMP(6)
)
SQL;

            $requestStmt = $this->pdo->prepare($requestSql);
            $requestStmt->execute([
                'ingest_request_id' => $ingestRequestId,
                'tenant_id' => $tenantId,
                'source_id' => $sourceId,
                'request_id' => $requestId,
                'remote_ip' => $remoteIp !== '' ? $remoteIp : null,
                'event_count' => count($events),
                'accepted_count' => count($events),
            ]);

            $eventSql = <<<SQL
INSERT INTO log_events (
    log_event_id,
    tenant_id,
    source_id,
    ingest_request_id,
    source_event_id,
    occurred_at,
    observed_at,
    received_at,
    ingested_at,
    severity_number,
    severity_text,
    event_name,
    event_category,
    event_action,
    event_outcome,
    message_text,
    raw_payload_json,
    host_name,
    service_name,
    component_name,
    module_name,
    process_name,
    process_pid,
    thread_id,
    actor_user_id,
    actor_principal,
    session_hash_sha256,
    client_ip,
    server_ip,
    trace_id,
    correlation_id,
    request_correlation_id,
    attributes_json,
    classification_code,
    retention_policy_code,
    canonical_hash_sha256,
    previous_hash_sha256,
    source_signature,
    signature_algorithm,
    tenant_scope_key
) VALUES (
    :log_event_id,
    :tenant_id,
    :source_id,
    :ingest_request_id,
    :source_event_id,
    :occurred_at,
    :observed_at,
    UTC_TIMESTAMP(6),
    UTC_TIMESTAMP(6),
    :severity_number,
    :severity_text,
    :event_name,
    :event_category,
    :event_action,
    :event_outcome,
    :message_text,
    :raw_payload_json,
    :host_name,
    :service_name,
    :component_name,
    :module_name,
    :process_name,
    :process_pid,
    :thread_id,
    :actor_user_id,
    :actor_principal,
    :session_hash_sha256,
    :client_ip,
    :server_ip,
    :trace_id,
    :correlation_id,
    :request_correlation_id,
    :attributes_json,
    :classification_code,
    :retention_policy_code,
    :canonical_hash_sha256,
    :previous_hash_sha256,
    :source_signature,
    :signature_algorithm,
    :tenant_scope_key
)
SQL;

            $eventStmt = $this->pdo->prepare($eventSql);

            $previousHash = null;

            foreach ($events as $event) {
                $eventId = self::generateUlid();
                $canonicalHash = hash('sha256', $event->canonicalizeForHashing());

                $eventStmt->execute([
                    'log_event_id' => $eventId,
                    'tenant_id' => $tenantId,
                    'source_id' => $sourceId,
                    'ingest_request_id' => $ingestRequestId,
                    'source_event_id' => $event->sourceEventId,
                    'occurred_at' => $event->occurredAt,
                    'observed_at' => $event->observedAt,
                    'severity_number' => $event->severityNumber,
                    'severity_text' => $event->severityText,
                    'event_name' => $event->eventName,
                    'event_category' => $event->eventCategory,
                    'event_action' => $event->eventAction,
                    'event_outcome' => $event->eventOutcome,
                    'message_text' => $event->messageText,
                    'raw_payload_json' => $event->rawPayloadJson,
                    'host_name' => $event->hostName,
                    'service_name' => $event->serviceName,
                    'component_name' => $event->componentName,
                    'module_name' => $event->moduleName,
                    'process_name' => $event->processName,
                    'process_pid' => $event->processPid,
                    'thread_id' => $event->threadId,
                    'actor_user_id' => $event->actorUserId,
                    'actor_principal' => $event->actorPrincipal,
                    'session_hash_sha256' => $event->sessionHashSha256,
                    'client_ip' => $event->clientIp,
                    'server_ip' => $event->serverIp,
                    'trace_id' => $event->traceId,
                    'correlation_id' => $event->correlationId,
                    'request_correlation_id' => $event->requestCorrelationId,
                    'attributes_json' => $event->attributesJson,
                    'classification_code' => $event->classificationCode,
                    'retention_policy_code' => $event->retentionPolicyCode,
                    'canonical_hash_sha256' => $canonicalHash,
                    'previous_hash_sha256' => $previousHash,
                    'source_signature' => $event->sourceSignature,
                    'signature_algorithm' => $event->signatureAlgorithm,
                    'tenant_scope_key' => $event->tenantScopeKey,
                ]);

                $previousHash = $canonicalHash;
                $eventIds[] = $eventId;
            }

            $this->pdo->commit();

            return [
                'ingestRequestId' => $ingestRequestId,
                'eventIds' => $eventIds,
            ];
        } catch (Throwable $throwable) {
            if ($this->pdo->inTransaction()) {
                $this->pdo->rollBack();
            }

            throw $throwable;
        }
    }

    /**
     * Erzeugt eine einfache ULID-ähnliche ID.
     *
     * Für eine echte Produktionsimplementierung kann hier später ein dedizierter
     * ULID/UUIDv7-Generator eingesetzt werden. Für das Projektpaket reicht eine
     * monotonic-stabile ASCII-ID aus.
     */
    private static function generateUlid(): string
    {
        $time = (int) floor(microtime(true) * 1000);
        $timeHex = strtoupper(str_pad(dechex($time), 12, '0', STR_PAD_LEFT));
        $randomHex = strtoupper(bin2hex(random_bytes(7)));

        return substr($timeHex . $randomHex, 0, 26);
    }
}
