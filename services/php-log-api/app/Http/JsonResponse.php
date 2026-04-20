<?php

declare(strict_types=1);

namespace App\Http;

use JsonException;

/**
 * Komfortklasse für JSON-Antworten.
 */
final class JsonResponse extends Response
{
    /**
     * @param array<string, mixed> $payload
     */
    public static function success(array $payload, int $statusCode = 200): self
    {
        return new self(
            $statusCode,
            self::encodeJson($payload),
            [
                'Content-Type' => 'application/json; charset=utf-8',
                'Cache-Control' => 'no-store',
            ]
        );
    }

    public static function empty(int $statusCode = 204): self
    {
        return new self(
            $statusCode,
            '',
            ['Cache-Control' => 'no-store']
        );
    }

    public static function error(
        int $statusCode,
        string $errorCode,
        string $message,
        string $requestId
    ): self {
        return self::success(
            [
                'ok' => false,
                'error' => [
                    'code' => $errorCode,
                    'message' => $message,
                    'requestId' => $requestId,
                ],
            ],
            $statusCode
        );
    }

    /**
     * Kodiert ein Array sicher als JSON.
     *
     * @param array<string, mixed> $payload
     */
    private static function encodeJson(array $payload): string
    {
        try {
            return json_encode(
                $payload,
                JSON_THROW_ON_ERROR | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES
            );
        } catch (JsonException) {
            return '{"ok":false,"error":{"code":"json_encoding_failed","message":"Response encoding failed."}}';
        }
    }
}
