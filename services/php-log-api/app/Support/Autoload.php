<?php

declare(strict_types=1);

/**
 * Kleiner PSR-4-Fallback-Autoloader für das Namespace-Präfix `App\`.
 *
 * Die Anwendung ist so sowohl mit Composer-Autoloading als auch ohne Composer
 * lauffähig. Das ist auf Shared Hosting praktisch, weil der Upload eines
 * bereits vorbereiteten ZIP-Pakets dadurch einfacher wird.
 */
spl_autoload_register(
    static function (string $class): void {
        $prefix = 'App\\';
        $baseDir = __DIR__ . '/../';

        if (!str_starts_with($class, $prefix)) {
            return;
        }

        $relativeClass = substr($class, strlen($prefix));
        $file = $baseDir . str_replace('\\', DIRECTORY_SEPARATOR, $relativeClass) . '.php';

        if (is_file($file)) {
            require $file;
        }
    }
);
