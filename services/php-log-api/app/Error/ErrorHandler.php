<?php

declare(strict_types=1);

namespace App\Error;

use App\Config\Config;
use App\Support\FileLogger;
use Throwable;

/**
 * Registriert zentrale Fehler- und Ausnahmebehandlung.
 */
final class ErrorHandler
{
    public function __construct(
        private readonly Config $config,
        private readonly FileLogger $fileLogger
    ) {
    }

    /**
     * Aktiviert Fehlerbehandlung und sinnvolle PHP-Defaults.
     */
    public function register(): void
    {
        ini_set('display_errors', $this->config->getBool('APP_DEBUG', false) ? '1' : '0');
        error_reporting(E_ALL);

        set_exception_handler(function (Throwable $throwable): void {
            $this->fileLogger->error('Unhandled exception', [
                'exception' => get_class($throwable),
                'message' => $throwable->getMessage(),
            ]);

            if ($this->config->getBool('APP_DEBUG', false)) {
                http_response_code(500);
                header('Content-Type: text/plain; charset=utf-8');
                echo $throwable;
            }
        });

        set_error_handler(static function (
            int $severity,
            string $message,
            string $file,
            int $line
        ): bool {
            throw new \ErrorException($message, 0, $severity, $file, $line);
        });
    }
}
