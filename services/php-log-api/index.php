<?php

declare(strict_types=1);

/**
 * Primärer Einstiegspunkt der Anwendung.
 *
 * Diese Datei liegt bewusst im Root-Verzeichnis, weil die Hosting-Vorgabe
 * ausdrücklich keine public-only-Struktur verlangt und die Lösung ohne
 * Rewrite-Regeln funktionieren muss.
 */
require __DIR__ . '/bootstrap.php';

use App\Bootstrap\AppBootstrap;

$kernel = AppBootstrap::boot(__DIR__);
$kernel->handleCurrentRequest();
