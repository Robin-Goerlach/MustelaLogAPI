<?php

declare(strict_types=1);

namespace App\Error;

use RuntimeException;

/**
 * Kontrollierte API-Ausnahme.
 *
 * Diese Ausnahme trennt bewusst zwischen interner Diagnose (`message`) und
 * der nach außen sichtbaren Fehlermeldung (`publicMessage`).
 */
final class ApiException extends RuntimeException
{
    public function __construct(
        private readonly int $statusCode,
        private readonly string $errorCode,
        private readonly string $publicMessage,
        string $internalMessage = ''
    ) {
        parent::__construct($internalMessage !== '' ? $internalMessage : $publicMessage);
    }

    public function getStatusCode(): int
    {
        return $this->statusCode;
    }

    public function getErrorCode(): string
    {
        return $this->errorCode;
    }

    public function getPublicMessage(): string
    {
        return $this->publicMessage;
    }
}
