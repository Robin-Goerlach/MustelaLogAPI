<?php

declare(strict_types=1);

namespace App\Controller\Api\V1;

use App\Audit\AuditLogger;
use App\Error\ApiException;
use App\Http\JsonResponse;
use App\Http\Request;
use App\Security\AuthenticatedPrincipal;
use App\Service\IngestService;

/**
 * Controller für den Event-Ingest.
 */
final class IngestController
{
    public function __construct(
        private readonly IngestService $ingestService,
        private readonly AuditLogger $auditLogger
    ) {
    }

    /**
     * Nimmt ein oder mehrere Events entgegen.
     *
     * @throws ApiException
     */
    public function store(Request $request): JsonResponse
    {
        $principal = $request->getAuthenticatedPrincipal();

        if (!$principal instanceof AuthenticatedPrincipal) {
            throw new ApiException(401, 'authentication_required', 'Authentication is required.');
        }

        $result = $this->ingestService->ingest($request, $principal);

        return JsonResponse::success([
            'ok' => true,
            'requestId' => $request->getRequestId(),
            'data' => $result,
        ], 202);
    }
}
