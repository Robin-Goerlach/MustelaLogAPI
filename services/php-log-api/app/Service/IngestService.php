<?php

declare(strict_types=1);

namespace App\Service;

use App\Audit\AuditLogger;
use App\Config\Config;
use App\DTO\IngestEvent;
use App\Error\ApiException;
use App\Http\Request;
use App\Repository\EventWriteRepository;
use App\Security\AuthenticatedPrincipal;
use App\Validation\IngestEventValidator;

/**
 * Fachservice für den Ingest-Pfad.
 *
 * Hier werden Authentisierungskontext, Validierung, Normalisierung und
 * Persistierung zusammengeführt. Dadurch bleibt der Controller schlank.
 */
final class IngestService
{
    public function __construct(
        private readonly EventWriteRepository $eventWriteRepository,
        private readonly AuditLogger $auditLogger,
        private readonly IngestEventValidator $validator,
        private readonly Config $config
    ) {
    }

    /**
     * Nimmt einen Ingest-Request an und speichert die Events.
     *
     * @return array{ingestRequestId:string,eventIds:list<string>,accepted:int}
     *
     * @throws ApiException
     */
    public function ingest(Request $request, AuthenticatedPrincipal $principal): array
    {
        if ($principal->sourceId === null) {
            throw new ApiException(403, 'invalid_principal', 'Source authentication is required.');
        }

        $body = $request->getJsonBody();
        if (!is_array($body)) {
            throw new ApiException(400, 'missing_payload', 'A JSON body is required.');
        }

        $eventsPayload = [];

        if (isset($body['events'])) {
            if (!is_array($body['events'])) {
                throw new ApiException(422, 'invalid_events', 'events must be an array.');
            }

            $eventsPayload = $body['events'];
        } else {
            $eventsPayload = [$body];
        }

        if ($eventsPayload === []) {
            throw new ApiException(422, 'empty_batch', 'At least one event is required.');
        }

        $events = [];

        foreach ($eventsPayload as $eventPayload) {
            if (!is_array($eventPayload)) {
                throw new ApiException(422, 'invalid_event', 'Each event must be a JSON object.');
            }

            $events[] = $this->validator->validate($eventPayload);
        }

        $result = $this->eventWriteRepository->ingestBatch(
            $principal->tenantId,
            $principal->sourceId,
            $request->getRemoteIp(),
            $request->getRequestId(),
            $events
        );

        $this->auditLogger->logSuccess(
            $request,
            'events.ingest',
            $principal->principalType,
            $principal->principalId,
            [
                'tenant_id' => $principal->tenantId,
                'source_id' => $principal->sourceId,
                'accepted' => count($events),
                'ingest_request_id' => $result['ingestRequestId'],
            ]
        );

        return [
            'ingestRequestId' => $result['ingestRequestId'],
            'eventIds' => $result['eventIds'],
            'accepted' => count($events),
        ];
    }
}
