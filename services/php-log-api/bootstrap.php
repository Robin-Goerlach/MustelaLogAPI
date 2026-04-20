<?php

declare(strict_types=1);

/**
 * Frühes Bootstrap.
 *
 * Diese Datei lädt zuerst einen minimalen Fallback-Autoloader, damit das
 * Projekt selbst dann lauffähig bleibt, wenn auf dem Shared Hosting kein
 * `composer dump-autoload` ausgeführt wurde.
 */
$composerAutoload = __DIR__ . '/vendor/autoload.php';

if (is_file($composerAutoload)) {
    require $composerAutoload;
} else {
    require __DIR__ . '/app/Support/Autoload.php';
}
