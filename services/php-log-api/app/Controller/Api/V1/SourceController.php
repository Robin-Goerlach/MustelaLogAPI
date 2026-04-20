<?php

declare(strict_types=1);

namespace App\Controller\Api\V1;

use App\Audit\AuditLogger;
use App\Error\ApiException;
use App\Http\JsonResponse;
use App\Http\Request;
use App\Security\AuthenticatedPrincipal;
use App\Service\SourceService;

/**
 * Controller für Quellen-Lesezugriffe.
 */
final class SourceController
{
    public function __construct(
        private readonly SourceService $sourceService,
        private readonly AuditLogger $auditLogger
    ) {
    }

    /**
     * @throws ApiException
     */
    public function index(Request $request): JsonResponse
    {
        $principal = $request->getAuthenticatedPrincipal();

        if (!$principal instanceof AuthenticatedPrincipal) {
            throw new ApiException(401, 'authentication_required', 'Authentication is required.');
        }

        $sources = $this->sourceService->listSources($principal->tenantId);

        $this->auditLogger->logSuccess(
            $request,
            'sources.read',
            $principal->principalType,
            $principal->principalId,
            ['count' => count($sources)]
        );

        return JsonResponse::success([
            'ok' => true,
            'requestId' => $request->getRequestId(),
            'data' => $sources,
        ]);
    }
}
