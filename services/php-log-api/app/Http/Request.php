<?php

declare(strict_types=1);

namespace App\Http;

use App\Error\ApiException;

/**
 * Kapselt die HTTP-Anfrage.
 *
 * Die Klasse vereinheitlicht den Zugriff auf Header, Query, JSON-Body und die
 * Zielroute. Das macht die Controller schlanker und vermeidet mehrfachen Zugriff
 * auf globale Arrays.
 */
final class Request
{
    /**
     * @param array<string, string> $headers
     * @param array<string, mixed> $query
     * @param array<string, mixed>|null $jsonBody
     * @param array<string, string> $pathParams
     */
    public function __construct(
        private readonly string $method,
        private readonly string $uri,
        private readonly string $routePath,
        private readonly array $headers,
        private readonly array $query,
        private readonly ?array $jsonBody,
        private readonly string $rawBody,
        private readonly string $remoteIp,
        private readonly string $requestId,
        private array $pathParams = [],
        private mixed $authenticatedPrincipal = null
    ) {
    }

    /**
     * Erstellt ein Request-Objekt aus den PHP-Globals.
     *
     * @throws ApiException
     */
    public static function fromGlobals(int $maxJsonBytes, string $routeParam): self
    {
        $method = strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET');
        $uri = (string) ($_SERVER['REQUEST_URI'] ?? '/');
        $query = $_GET;
        $headers = self::readHeaders();
        $rawBody = file_get_contents('php://input') ?: '';

        if (strlen($rawBody) > $maxJsonBytes) {
            throw new ApiException(413, 'payload_too_large', 'Payload too large.');
        }

        $contentType = strtolower(trim(explode(';', $headers['content-type'] ?? 'application/json')[0]));
        $jsonBody = null;

        if ($rawBody !== '') {
            if ($contentType !== 'application/json') {
                throw new ApiException(415, 'unsupported_media_type', 'Content-Type must be application/json.');
            }

            try {
                $decoded = json_decode($rawBody, true, 512, JSON_THROW_ON_ERROR);
            } catch (\JsonException) {
                throw new ApiException(400, 'invalid_json', 'Malformed JSON payload.');
            }

            if (!is_array($decoded)) {
                throw new ApiException(400, 'invalid_json_document', 'JSON payload must be an object.');
            }

            $jsonBody = $decoded;
        }

        $routePath = self::resolveRoutePath($routeParam);

        return new self(
            $method,
            $uri,
            $routePath,
            $headers,
            $query,
            $jsonBody,
            $rawBody,
            (string) ($_SERVER['REMOTE_ADDR'] ?? ''),
            self::generateRequestId()
        );
    }

    /**
     * @return array<string, string>
     */
    private static function readHeaders(): array
    {
        $headers = [];

        foreach ($_SERVER as $key => $value) {
            if (!is_string($value)) {
                continue;
            }

            if (str_starts_with($key, 'HTTP_')) {
                $name = strtolower(str_replace('_', '-', substr($key, 5)));
                $headers[$name] = $value;
            }
        }

        if (isset($_SERVER['CONTENT_TYPE']) && is_string($_SERVER['CONTENT_TYPE'])) {
            $headers['content-type'] = $_SERVER['CONTENT_TYPE'];
        }

        if (isset($_SERVER['CONTENT_LENGTH']) && is_string($_SERVER['CONTENT_LENGTH'])) {
            $headers['content-length'] = $_SERVER['CONTENT_LENGTH'];
        }

        return $headers;
    }

    /**
     * Ermittelt die API-Zielroute.
     *
     * Hauptweg: Query-Parameter, z. B. `index.php?route=/api/v1/events`.
     * Optional wird PATH_INFO mitgelesen, falls der Hoster das von selbst liefert.
     */
    private static function resolveRoutePath(string $routeParam): string
    {
        $route = $_GET[$routeParam] ?? null;

        if (is_string($route) && $route !== '') {
            return '/' . ltrim($route, '/');
        }

        $pathInfo = $_SERVER['PATH_INFO'] ?? null;
        if (is_string($pathInfo) && $pathInfo !== '') {
            return '/' . ltrim($pathInfo, '/');
        }

        return '/';
    }

    /**
     * Erzeugt eine einfache Request-ID.
     */
    private static function generateRequestId(): string
    {
        return bin2hex(random_bytes(8));
    }

    public function getMethod(): string
    {
        return $this->method;
    }

    public function getRoutePath(): string
    {
        return $this->routePath;
    }

    public function getUri(): string
    {
        return $this->uri;
    }

    /**
     * @return array<string, mixed>|null
     */
    public function getJsonBody(): ?array
    {
        return $this->jsonBody;
    }

    public function getRawBody(): string
    {
        return $this->rawBody;
    }

    public function getRemoteIp(): string
    {
        return $this->remoteIp;
    }

    public function getHeader(string $name): ?string
    {
        $normalized = strtolower($name);

        return $this->headers[$normalized] ?? null;
    }

    /**
     * @return array<string, mixed>
     */
    public function getQuery(): array
    {
        return $this->query;
    }

    public function getQueryParam(string $name): ?string
    {
        $value = $this->query[$name] ?? null;

        return is_scalar($value) ? (string) $value : null;
    }

    public function getRequestId(): string
    {
        return $this->requestId;
    }

    /**
     * @param array<string, string> $params
     */
    public function withPathParams(array $params): self
    {
        $clone = clone $this;
        $clone->pathParams = $params;

        return $clone;
    }

    public function getPathParam(string $name): ?string
    {
        return $this->pathParams[$name] ?? null;
    }

    public function withAuthenticatedPrincipal(mixed $principal): self
    {
        $clone = clone $this;
        $clone->authenticatedPrincipal = $principal;

        return $clone;
    }

    public function getAuthenticatedPrincipal(): mixed
    {
        return $this->authenticatedPrincipal;
    }
}
