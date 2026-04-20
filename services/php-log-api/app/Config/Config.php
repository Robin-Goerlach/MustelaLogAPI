<?php

declare(strict_types=1);

namespace App\Config;

use RuntimeException;

/**
 * Einfache Konfigurationshülle.
 *
 * Die Klasse liest `.env` und `$_ENV` zusammen. Für Shared Hosting ist diese
 * leichte Eigenlösung oft robuster als eine harte Laufzeitabhängigkeit von
 * zusätzlichen Libraries.
 */
final class Config
{
    /**
     * @param array<string, string> $values
     */
    private function __construct(private readonly array $values)
    {
    }

    /**
     * Lädt die Konfiguration aus dem Projektverzeichnis.
     */
    public static function fromProjectRoot(string $projectRoot): self
    {
        $values = [];

        $envFile = $projectRoot . '/.env';
        if (is_file($envFile)) {
            $lines = file($envFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES) ?: [];

            foreach ($lines as $line) {
                $trimmed = trim($line);

                if ($trimmed === '' || str_starts_with($trimmed, '#')) {
                    continue;
                }

                [$key, $value] = array_pad(explode('=', $trimmed, 2), 2, '');
                $key = trim($key);
                $value = trim($value);

                if ($value !== '' && (
                    ($value[0] === '"' && str_ends_with($value, '"')) ||
                    ($value[0] === "'" && str_ends_with($value, "'"))
                )) {
                    $value = substr($value, 1, -1);
                }

                $values[$key] = $value;
            }
        }

        foreach ($_ENV as $key => $value) {
            if (is_string($value)) {
                $values[$key] = $value;
            }
        }

        return new self($values);
    }

    /**
     * Gibt einen Stringwert zurück.
     */
    public function getString(string $key, ?string $default = null): string
    {
        if (array_key_exists($key, $this->values)) {
            return $this->values[$key];
        }

        if ($default !== null) {
            return $default;
        }

        throw new RuntimeException('Missing configuration key: ' . $key);
    }

    /**
     * Gibt einen booleschen Wert zurück.
     */
    public function getBool(string $key, bool $default = false): bool
    {
        if (!array_key_exists($key, $this->values)) {
            return $default;
        }

        return filter_var($this->values[$key], FILTER_VALIDATE_BOOL) ?? $default;
    }

    /**
     * Gibt den Wert zurück, wenn vorhanden.
     */
    public function getNullableString(string $key): ?string
    {
        return array_key_exists($key, $this->values) ? $this->values[$key] : null;
    }
}
