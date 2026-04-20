<?php

declare(strict_types=1);

namespace App\Http;

/**
 * HTTP-Antwort-Basisklasse.
 */
class Response
{
    /**
     * @param array<string, string> $headers
     */
    public function __construct(
        protected readonly int $statusCode,
        protected readonly string $body,
        protected readonly array $headers = []
    ) {
    }

    /**
     * Sendet Status, Header und Body an den Client.
     */
    public function send(): void
    {
        http_response_code($this->statusCode);

        foreach ($this->headers as $name => $value) {
            header($name . ': ' . $value);
        }

        echo $this->body;
    }
}
