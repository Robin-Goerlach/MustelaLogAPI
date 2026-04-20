<?php

declare(strict_types=1);

namespace App\Validation;

use App\DTO\IngestEvent;
use App\Error\ApiException;
use DateTimeImmutable;
use DateTimeZone;

/**
 * Validiert und normalisiert eingehende Events.
 *
 * Die Klasse trennt Transportdaten bewusst von Fachdaten. So landet nicht
 * beliebiges JSON direkt in der Datenbank.
 */
final class IngestEventValidator
{
    /**
     * @param array<string, mixed> $payload
     *
     * @throws ApiException
     */
    public function validate(array $payload): IngestEvent
    {
        $severityNumber = $this->requireInt($payload, 'severityNumber');
        if ($severityNumber < 1 || $severityNumber > 24) {
            throw new ApiException(422, 'invalid_severity_number', 'severityNumber must be between 1 and 24.');
        }

        $severityText = $this->requireTrimmedString($payload, 'severityText', 32);
        $messageText = $this->sanitizeLogText($this->requireTrimmedString($payload, 'message', 4000));

        $rawPayloadJson = json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        if ($rawPayloadJson === false) {
            throw new ApiException(422, 'payload_encoding_failed', 'Event payload could not be normalized.');
        }

        $attributes = $payload['attributes'] ?? [];
        if (!is_array($attributes)) {
            throw new ApiException(422, 'invalid_attributes', 'attributes must be an object.');
        }

        $attributesJson = json_encode($attributes, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        if ($attributesJson === false) {
            throw new ApiException(422, 'invalid_attributes', 'attributes could not be encoded.');
        }

        return new IngestEvent(
            $this->optionalString($payload, 'sourceEventId', 128),
            $this->normalizeDate($payload['occurredAt'] ?? null, 'occurredAt'),
            $this->normalizeDate($payload['observedAt'] ?? null, 'observedAt'),
            $severityNumber,
            $severityText,
            $this->optionalString($payload, 'eventName', 128),
            $this->optionalString($payload, 'eventCategory', 64),
            $this->optionalString($payload, 'eventAction', 64),
            $this->optionalString($payload, 'eventOutcome', 32),
            $messageText,
            $rawPayloadJson,
            $this->optionalString($payload, 'hostName', 255),
            $this->optionalString($payload, 'serviceName', 128),
            $this->optionalString($payload, 'componentName', 128),
            $this->optionalString($payload, 'moduleName', 128),
            $this->optionalString($payload, 'processName', 128),
            $this->optionalInt($payload, 'processPid'),
            $this->optionalString($payload, 'threadId', 128),
            $this->optionalString($payload, 'actorUserId', 128),
            $this->optionalString($payload, 'actorPrincipal', 255),
            $this->optionalSha256($payload, 'sessionHashSha256'),
            $this->optionalString($payload, 'clientIp', 64),
            $this->optionalString($payload, 'serverIp', 64),
            $this->optionalString($payload, 'traceId', 128),
            $this->optionalString($payload, 'correlationId', 128),
            $this->optionalString($payload, 'requestId', 128),
            $attributesJson,
            $this->optionalString($payload, 'classification', 64) ?? 'internal',
            $this->optionalString($payload, 'retentionPolicy', 64) ?? 'standard',
            $this->optionalString($payload, 'sourceSignature', 2048),
            $this->optionalString($payload, 'signatureAlgorithm', 64),
            $this->optionalString($payload, 'tenantScopeKey', 128)
        );
    }

    /**
     * Entfernt problematische Zeilenumbrüche für textbasierte Logs.
     *
     * Das DB-Event behält den Inhalt, neutralisiert aber klassische CR/LF-
     * Manipulationsmuster. Strukturierte Daten bleiben zusätzlich separat
     * im JSON erhalten.
     */
    private function sanitizeLogText(string $value): string
    {
        return str_replace(["\r", "\n"], ['\r', '\n'], $value);
    }

    /**
     * @param array<string, mixed> $payload
     */
    private function requireInt(array $payload, string $field): int
    {
        if (!array_key_exists($field, $payload) || !is_int($payload[$field])) {
            throw new ApiException(422, 'invalid_field', sprintf('%s must be an integer.', $field));
        }

        return $payload[$field];
    }

    /**
     * @param array<string, mixed> $payload
     */
    private function optionalInt(array $payload, string $field): ?int
    {
        if (!array_key_exists($field, $payload) || $payload[$field] === null) {
            return null;
        }

        if (!is_int($payload[$field])) {
            throw new ApiException(422, 'invalid_field', sprintf('%s must be an integer.', $field));
        }

        return $payload[$field];
    }

    /**
     * @param array<string, mixed> $payload
     */
    private function requireTrimmedString(array $payload, string $field, int $maxLength): string
    {
        $value = $this->optionalString($payload, $field, $maxLength);

        if ($value === null || $value === '') {
            throw new ApiException(422, 'invalid_field', sprintf('%s must be a non-empty string.', $field));
        }

        return $value;
    }

    /**
     * @param array<string, mixed> $payload
     */
    private function optionalString(array $payload, string $field, int $maxLength): ?string
    {
        if (!array_key_exists($field, $payload) || $payload[$field] === null) {
            return null;
        }

        if (!is_string($payload[$field])) {
            throw new ApiException(422, 'invalid_field', sprintf('%s must be a string.', $field));
        }

        $value = trim($payload[$field]);

        if (mb_strlen($value) > $maxLength) {
            throw new ApiException(422, 'field_too_long', sprintf('%s exceeds max length.', $field));
        }

        return $value;
    }

    /**
     * @param array<string, mixed> $payload
     */
    private function optionalSha256(array $payload, string $field): ?string
    {
        $value = $this->optionalString($payload, $field, 64);

        if ($value === null) {
            return null;
        }

        if (!preg_match('/^[a-f0-9]{64}$/i', $value)) {
            throw new ApiException(422, 'invalid_field', sprintf('%s must be a SHA-256 hex string.', $field));
        }

        return strtolower($value);
    }

    /**
     * Normalisiert Datumsangaben auf UTC-ISO-Format mit Mikrosekunden.
     *
     * @throws ApiException
     */
    private function normalizeDate(mixed $value, string $field): string
    {
        if (!is_string($value) || trim($value) === '') {
            throw new ApiException(422, 'invalid_field', sprintf('%s must be an ISO-8601 datetime string.', $field));
        }

        try {
            $dt = new DateTimeImmutable($value);
        } catch (\Exception) {
            throw new ApiException(422, 'invalid_field', sprintf('%s is not a valid datetime.', $field));
        }

        return $dt->setTimezone(new DateTimeZone('UTC'))->format('Y-m-d H:i:s.u');
    }
}
