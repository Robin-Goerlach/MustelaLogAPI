<?php

declare(strict_types=1);

namespace App\Support;

/**
 * Sehr einfacher JSON-Line-Dateilogger.
 *
 * Dieser Logger ist für lokale Technikdiagnosen der Middleware gedacht. Er ist
 * bewusst getrennt vom eigentlichen Audit-Logging der Fachanwendung.
 */
final class FileLogger
{
    public function __construct(private readonly string $filePath)
    {
    }

    /**
     * @param array<string, mixed> $context
     */
    public function info(string $message, array $context = []): void
    {
        $this->write('INFO', $message, $context);
    }

    /**
     * @param array<string, mixed> $context
     */
    public function warning(string $message, array $context = []): void
    {
        $this->write('WARNING', $message, $context);
    }

    /**
     * @param array<string, mixed> $context
     */
    public function error(string $message, array $context = []): void
    {
        $this->write('ERROR', $message, $context);
    }

    /**
     * @param array<string, mixed> $context
     */
    private function write(string $level, string $message, array $context): void
    {
        $dir = dirname($this->filePath);
        if (!is_dir($dir)) {
            @mkdir($dir, 0775, true);
        }

        $record = [
            'ts' => gmdate('c'),
            'level' => $level,
            'message' => $message,
            'context' => $context,
        ];

        $json = json_encode($record, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        if ($json === false) {
            $json = '{"level":"ERROR","message":"log_encoding_failed"}';
        }

        @file_put_contents($this->filePath, $json . PHP_EOL, FILE_APPEND | LOCK_EX);
    }
}
