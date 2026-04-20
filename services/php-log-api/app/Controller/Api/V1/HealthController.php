<?php

declare(strict_types=1);

namespace App\Controller\Api\V1;

use App\Config\Config;
use App\Http\JsonResponse;
use App\Http\Request;

/**
 * Einfacher Health-Endpunkt.
 */
final class HealthController
{
    public function __construct(private readonly Config $config)
    {
    }

    public function show(Request $request): JsonResponse
    {
        return JsonResponse::success([
            'ok' => true,
            'service' => $this->config->getString('APP_NAME', 'logging-middleware'),
            'version' => 'v1',
            'requestId' => $request->getRequestId(),
            'timestamp' => gmdate('c'),
        ]);
    }
}
