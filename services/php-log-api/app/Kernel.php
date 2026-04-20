<?php

declare(strict_types=1);

namespace App;

use App\Audit\AuditLogger;
use App\Config\Config;
use App\Error\ApiException;
use App\Http\JsonResponse;
use App\Http\Request;
use App\Routing\Router;
use App\Support\FileLogger;
use Throwable;

/**
 * HTTP-Kern der Anwendung.
 *
 * Der Kernel nimmt die aktuelle HTTP-Anfrage entgegen, lässt sie vom Router
 * auswerten und sorgt für eine kontrollierte Fehlerbehandlung.
 */
final class Kernel
{
    public function __construct(
        private readonly Config $config,
        private readonly Router $router,
        private readonly AuditLogger $auditLogger,
        private readonly FileLogger $fileLogger
    ) {
    }

    /**
     * Verarbeitet die aktuelle PHP-HTTP-Anfrage.
     */
    public function handleCurrentRequest(): void
    {
        $request = Request::fromGlobals(
            (int) $this->config->getString('APP_MAX_JSON_BYTES', '262144'),
            $this->config->getString('APP_ROUTE_PARAM', 'route')
        );

        try {
            $response = $this->router->dispatch($request);
        } catch (ApiException $exception) {
            $this->auditLogger->logFailure(
                $request,
                'api.request',
                $exception->getMessage(),
                [
                    'http_status' => $exception->getStatusCode(),
                    'error_code' => $exception->getErrorCode(),
                ]
            );

            $response = JsonResponse::error(
                $exception->getStatusCode(),
                $exception->getErrorCode(),
                $exception->getPublicMessage(),
                $request->getRequestId()
            );
        } catch (Throwable $throwable) {
            $this->fileLogger->error('Unhandled exception in Kernel', [
                'request_id' => $request->getRequestId(),
                'exception' => get_class($throwable),
                'message' => $throwable->getMessage(),
            ]);

            $this->auditLogger->logFailure(
                $request,
                'api.request',
                'internal_error',
                ['exception' => get_class($throwable)]
            );

            $response = JsonResponse::error(
                500,
                'internal_error',
                'An internal error occurred.',
                $request->getRequestId()
            );
        }

        $response->send();
    }
}
