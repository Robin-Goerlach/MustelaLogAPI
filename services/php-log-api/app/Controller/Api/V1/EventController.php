<?php

declare(strict_types=1);

namespace App\Controller\Api\V1;

use App\Audit\AuditLogger;
use App\Error\ApiException;
use App\Http\JsonResponse;
use App\Http\Request;
use App\Security\AuthenticatedPrincipal;
use App\Service\EventQueryService;

/**
 * Controller für Event-Lesezugriffe.
 */
final class EventController
{
    public function __construct(
        private readonly EventQueryService $eventQueryService,
        private readonly AuditLogger $auditLogger
    ) {
    }

    /**
     * Listet Log-Events.
     *
     * @throws ApiException
     */
    public function index(Request $request): JsonResponse
    {
        $principal = $request->getAuthenticatedPrincipal();

        if (!$principal instanceof AuthenticatedPrincipal) {
            throw new ApiException(401, 'authentication_required', 'Authentication is required.');
        }

        $page = max(1, (int) ($request->getQueryParam('page') ?? '1'));
        $pageSize = min(200, max(1, (int) ($request->getQueryParam('pageSize') ?? '50')));

        $filters = [
            'sourceKey' => $request->getQueryParam('sourceKey') ?? '',
            'severityText' => $request->getQueryParam('severityText') ?? '',
            'traceId' => $request->getQueryParam('traceId') ?? '',
            'correlationId' => $request->getQueryParam('correlationId') ?? '',
            'from' => $request->getQueryParam('from') ?? '',
            'to' => $request->getQueryParam('to') ?? '',
            'sort' => $request->getQueryParam('sort') ?? 'occurredAt',
            'direction' => $request->getQueryParam('direction') ?? 'DESC',
        ];

        $result = $this->eventQueryService->search($principal->tenantId, $filters, $page, $pageSize);

        $this->auditLogger->logSuccess(
            $request,
            'events.read',
            $principal->principalType,
            $principal->principalId,
            [
                'page' => $page,
                'page_size' => $pageSize,
                'filter_keys' => array_keys(array_filter($filters, static fn (string $value): bool => $value !== '')),
            ]
        );

        return JsonResponse::success([
            'ok' => true,
            'requestId' => $request->getRequestId(),
            'data' => $result,
        ]);
    }

    /**
     * Liefert ein einzelnes Event.
     *
     * @throws ApiException
     */
    public function show(Request $request): JsonResponse
    {
        $principal = $request->getAuthenticatedPrincipal();

        if (!$principal instanceof AuthenticatedPrincipal) {
            throw new ApiException(401, 'authentication_required', 'Authentication is required.');
        }

        $eventId = $request->getPathParam('eventId');
        if ($eventId === null || $eventId === '') {
            throw new ApiException(400, 'missing_event_id', 'eventId is required.');
        }

        $event = $this->eventQueryService->getById($principal->tenantId, $eventId);
        if ($event === null) {
            throw new ApiException(404, 'event_not_found', 'The requested event does not exist.');
        }

        $this->auditLogger->logSuccess(
            $request,
            'events.read.single',
            $principal->principalType,
            $principal->principalId,
            ['event_id' => $eventId]
        );

        return JsonResponse::success([
            'ok' => true,
            'requestId' => $request->getRequestId(),
            'data' => $event,
        ]);
    }
}
