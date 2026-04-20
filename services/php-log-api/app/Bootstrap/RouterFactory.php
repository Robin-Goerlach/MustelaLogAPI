<?php

declare(strict_types=1);

namespace App\Bootstrap;

use App\Audit\AuditLogger;
use App\Config\Config;
use App\Controller\Api\V1\EventController;
use App\Controller\Api\V1\HealthController;
use App\Controller\Api\V1\IngestController;
use App\Controller\Api\V1\SourceController;
use App\Routing\Route;
use App\Routing\Router;
use App\Security\ApiAuthenticator;
use App\Security\AuthorizationService;
use App\Security\FileRateLimiter;
use App\Service\EventQueryService;
use App\Service\IngestService;
use App\Service\SourceService;

/**
 * Baut die Routenliste der Anwendung auf.
 *
 * Die API ist bewusst pragmatisch: fachlich REST-orientiert, aber nicht von
 * URL-Rewrite abhängig. Der Front Controller erhält die Zielroute über einen
 * Query-Parameter.
 */
final class RouterFactory
{
    /**
     * Erstellt alle bekannten Routen.
     */
    public static function create(
        Config $config,
        ApiAuthenticator $authenticator,
        AuthorizationService $authorizationService,
        EventQueryService $eventQueryService,
        SourceService $sourceService,
        IngestService $ingestService,
        AuditLogger $auditLogger,
        FileRateLimiter $rateLimiter
    ): Router {
        $router = new Router($config->getString('APP_ROUTE_PARAM', 'route'));

        $healthController = new HealthController($config);
        $ingestController = new IngestController($ingestService, $auditLogger);
        $eventController = new EventController($eventQueryService, $auditLogger);
        $sourceController = new SourceController($sourceService, $auditLogger);

        $router->addRoute(
            new Route('GET', '/api/v1/health', [$healthController, 'show'])
        );

        $router->addRoute(
            new Route(
                'POST',
                '/api/v1/ingest/events',
                [$ingestController, 'store'],
                'source',
                ['events.ingest']
            )
        );

        $router->addRoute(
            new Route(
                'GET',
                '/api/v1/events',
                [$eventController, 'index'],
                'client',
                ['events.read']
            )
        );

        $router->addRoute(
            new Route(
                'GET',
                '/api/v1/events/{eventId}',
                [$eventController, 'show'],
                'client',
                ['events.read']
            )
        );

        $router->addRoute(
            new Route(
                'GET',
                '/api/v1/sources',
                [$sourceController, 'index'],
                'client',
                ['sources.read']
            )
        );

        $router->setAuthenticator($authenticator);
        $router->setAuthorizationService($authorizationService);
        $router->setRateLimiter($rateLimiter);

        return $router;
    }
}
