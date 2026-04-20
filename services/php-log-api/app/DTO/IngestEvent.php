<?php

declare(strict_types=1);

namespace App\DTO;

/**
 * Normalisierte Event-Datenstruktur.
 *
 * Diese DTO-Klasse markiert bewusst den Punkt, an dem eingehendes JSON bereits
 * validiert und in ein internes Fachmodell überführt wurde.
 */
final class IngestEvent
{
    public function __construct(
        public readonly ?string $sourceEventId,
        public readonly string $occurredAt,
        public readonly string $observedAt,
        public readonly int $severityNumber,
        public readonly string $severityText,
        public readonly ?string $eventName,
        public readonly ?string $eventCategory,
        public readonly ?string $eventAction,
        public readonly ?string $eventOutcome,
        public readonly string $messageText,
        public readonly string $rawPayloadJson,
        public readonly ?string $hostName,
        public readonly ?string $serviceName,
        public readonly ?string $componentName,
        public readonly ?string $moduleName,
        public readonly ?string $processName,
        public readonly ?int $processPid,
        public readonly ?string $threadId,
        public readonly ?string $actorUserId,
        public readonly ?string $actorPrincipal,
        public readonly ?string $sessionHashSha256,
        public readonly ?string $clientIp,
        public readonly ?string $serverIp,
        public readonly ?string $traceId,
        public readonly ?string $correlationId,
        public readonly ?string $requestCorrelationId,
        public readonly string $attributesJson,
        public readonly string $classificationCode,
        public readonly string $retentionPolicyCode,
        public readonly ?string $sourceSignature,
        public readonly ?string $signatureAlgorithm,
        public readonly ?string $tenantScopeKey
    ) {
    }

    /**
     * Liefert eine kanonische Darstellung für die Hash-Bildung.
     */
    public function canonicalizeForHashing(): string
    {
        return implode('|', [
            $this->sourceEventId ?? '',
            $this->occurredAt,
            $this->observedAt,
            (string) $this->severityNumber,
            $this->severityText,
            $this->eventName ?? '',
            $this->eventCategory ?? '',
            $this->eventAction ?? '',
            $this->eventOutcome ?? '',
            $this->messageText,
            $this->rawPayloadJson,
            $this->hostName ?? '',
            $this->serviceName ?? '',
            $this->componentName ?? '',
            $this->moduleName ?? '',
            $this->processName ?? '',
            (string) ($this->processPid ?? 0),
            $this->threadId ?? '',
            $this->actorUserId ?? '',
            $this->actorPrincipal ?? '',
            $this->sessionHashSha256 ?? '',
            $this->clientIp ?? '',
            $this->serverIp ?? '',
            $this->traceId ?? '',
            $this->correlationId ?? '',
            $this->requestCorrelationId ?? '',
            $this->attributesJson,
            $this->classificationCode,
            $this->retentionPolicyCode,
            $this->tenantScopeKey ?? '',
        ]);
    }
}
