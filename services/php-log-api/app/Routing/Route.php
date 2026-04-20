<?php

declare(strict_types=1);

namespace App\Routing;

/**
 * Beschreibt eine einzelne Route.
 */
final class Route
{
    /**
     * @param callable $handler
     * @param list<string> $requiredScopes
     */
    public function __construct(
        private readonly string $method,
        private readonly string $pathPattern,
        private readonly mixed $handler,
        private readonly ?string $authType = null,
        private readonly array $requiredScopes = []
    ) {
    }

    public function getMethod(): string
    {
        return $this->method;
    }

    public function getPathPattern(): string
    {
        return $this->pathPattern;
    }

    public function getHandler(): mixed
    {
        return $this->handler;
    }

    public function getAuthType(): ?string
    {
        return $this->authType;
    }

    /**
     * @return list<string>
     */
    public function getRequiredScopes(): array
    {
        return $this->requiredScopes;
    }
}
