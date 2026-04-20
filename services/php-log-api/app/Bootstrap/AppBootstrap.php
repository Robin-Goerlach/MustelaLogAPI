<?php

declare(strict_types=1);

namespace App\Bootstrap;

use App\Audit\AuditLogger;
use App\Config\Config;
use App\Error\ErrorHandler;
use App\Http\JsonResponse;
use App\Http\Request;
use App\Kernel;
use App\Repository\ApiClientCredentialRepository;
use App\Repository\DbConnectionFactory;
use App\Repository\EventReadRepository;
use App\Repository\EventWriteRepository;
use App\Repository\SourceCredentialRepository;
use App\Repository\SourceReadRepository;
use App\Routing\Router;
use App\Security\ApiAuthenticator;
use App\Security\AuthorizationService;
use App\Security\FileRateLimiter;
use App\Service\EventQueryService;
use App\Service\IngestService;
use App\Service\SourceService;
use App\Support\FileLogger;
use App\Validation\IngestEventValidator;

/**
 * Zentraler Bootstrap der Anwendung.
 *
 * Hier werden Konfiguration, Fehlerbehandlung, Routing und Kernservices
 * zusammengeführt. Die Klasse kapselt bewusst die Verdrahtung, damit `index.php`
 * schlank bleibt und die Anwendung klar strukturiert ist.
 */
final class AppBootstrap
{
    /**
     * Baut die Anwendung vollständig auf.
     *
     * @param string $projectRoot Absoluter Projektpfad.
     */
    public static function boot(string $projectRoot): Kernel
    {
        $config = Config::fromProjectRoot($projectRoot);
        date_default_timezone_set($config->getString('APP_TIMEZONE', 'UTC'));

        $fileLogger = new FileLogger($projectRoot . '/' . $config->getString('APP_LOCAL_LOG_FILE', 'var/log/app.log'));
        $errorHandler = new ErrorHandler($config, $fileLogger);
        $errorHandler->register();

        $connectionFactory = new DbConnectionFactory($config);
        $readPdo = $connectionFactory->createReadConnection();
        $writePdo = $connectionFactory->createWriteConnection();

        $sourceCredentialRepository = new SourceCredentialRepository($readPdo, $config);
        $apiClientCredentialRepository = new ApiClientCredentialRepository($readPdo, $config);

        $authenticator = new ApiAuthenticator(
            $sourceCredentialRepository,
            $apiClientCredentialRepository,
            $config
        );

        $authorizationService = new AuthorizationService();

        $eventReadRepository = new EventReadRepository($readPdo);
        $sourceReadRepository = new SourceReadRepository($readPdo);
        $eventWriteRepository = new EventWriteRepository($writePdo);

        $auditLogger = new AuditLogger($writePdo, $fileLogger);
        $rateLimiter = new FileRateLimiter($config, $projectRoot . '/var/cache/rate-limit');
        $eventQueryService = new EventQueryService($eventReadRepository);
        $sourceService = new SourceService($sourceReadRepository);
        $ingestService = new IngestService(
            $eventWriteRepository,
            $auditLogger,
            new IngestEventValidator(),
            $config
        );

        $router = RouterFactory::create(
            $config,
            $authenticator,
            $authorizationService,
            $eventQueryService,
            $sourceService,
            $ingestService,
            $auditLogger,
            $rateLimiter
        );

        return new Kernel(
            $config,
            $router,
            $auditLogger,
            $fileLogger
        );
    }
}
