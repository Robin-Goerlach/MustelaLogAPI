<?php

declare(strict_types=1);

/**
 * Optionale alternative Eintrittsstelle.
 *
 * Diese Datei ist NICHT die Hauptlösung. Sie ist nur dafür gedacht, spätere
 * Hosting-Varianten mit public-Webroot zu unterstützen, ohne dass die
 * eigentliche Anwendung neu geschnitten werden muss.
 */
require dirname(__DIR__) . '/bootstrap.php';

use App\Bootstrap\AppBootstrap;

$kernel = AppBootstrap::boot(dirname(__DIR__));
$kernel->handleCurrentRequest();
