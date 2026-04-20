<?php

declare(strict_types=1);

namespace App\Security;

use App\Config\Config;
use App\Error\ApiException;

/**
 * Sehr einfacher dateibasierter Fixed-Window-Rate-Limiter.
 *
 * Für Shared Hosting ist ein leichtgewichtiges Dateimodell oft praktikabler
 * als Redis oder ein eigener Hintergrunddienst. Die Implementierung ist
 * bewusst konservativ und soll Missbrauch begrenzen, nicht Hochlast-APIs
 * auf CDN-/Gateway-Niveau ersetzen.
 */
final class FileRateLimiter
{
    public function __construct(
        private readonly Config $config,
        private readonly string $storageDirectory
    ) {
    }

    /**
     * Prüft das Limit für den aktuellen Prinzipal.
     *
     * @throws ApiException
     */
    public function assertAllowed(AuthenticatedPrincipal $principal, string $routePath): void
    {
        if (!$this->config->getBool('RATE_LIMIT_ENABLED', true)) {
            return;
        }

        $limit = $principal->principalType === 'source'
            ? (int) $this->config->getString('RATE_LIMIT_INGEST_PER_MINUTE', '120')
            : (int) $this->config->getString('RATE_LIMIT_READ_PER_MINUTE', '300');

        $bucket = gmdate('YmdHi');
        $key = hash('sha256', $principal->principalType . '|' . $principal->principalId . '|' . $routePath . '|' . $bucket);

        if (!is_dir($this->storageDirectory)) {
            @mkdir($this->storageDirectory, 0775, true);
        }

        $file = $this->storageDirectory . '/' . $key . '.json';
        $handle = fopen($file, 'c+');

        if ($handle === false) {
            // Fail-open wäre hier riskanter. Wenn kein Counter geschrieben werden
            // kann, lieber eine kontrollierte 503-Antwort liefern.
            throw new ApiException(503, 'rate_limit_storage_unavailable', 'Service temporarily unavailable.');
        }

        try {
            if (!flock($handle, LOCK_EX)) {
                throw new ApiException(503, 'rate_limit_lock_failed', 'Service temporarily unavailable.');
            }

            $contents = stream_get_contents($handle);
            $data = is_string($contents) && $contents !== '' ? json_decode($contents, true) : null;

            $count = is_array($data) && isset($data['count']) ? (int) $data['count'] : 0;
            $count++;

            if ($count > $limit) {
                throw new ApiException(429, 'rate_limit_exceeded', 'Rate limit exceeded. Please retry later.');
            }

            rewind($handle);
            ftruncate($handle, 0);
            fwrite($handle, json_encode([
                'count' => $count,
                'bucket' => $bucket,
                'limit' => $limit,
            ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES));
        } finally {
            flock($handle, LOCK_UN);
            fclose($handle);
        }
    }
}
