<?php

declare(strict_types=1);

namespace App\Service;

use App\Repository\EventReadRepository;

/**
 * Fachservice für Event-Lesezugriffe.
 */
final class EventQueryService
{
    public function __construct(private readonly EventReadRepository $eventReadRepository)
    {
    }

    /**
     * @param array<string, string> $filters
     *
     * @return array{items:list<array<string,mixed>>,total:int,page:int,pageSize:int}
     */
    public function search(string $tenantId, array $filters, int $page, int $pageSize): array
    {
        $result = $this->eventReadRepository->findEvents($tenantId, $filters, $page, $pageSize);

        return [
            'items' => $result['items'],
            'total' => $result['total'],
            'page' => $page,
            'pageSize' => $pageSize,
        ];
    }

    /**
     * @return array<string,mixed>|null
     */
    public function getById(string $tenantId, string $eventId): ?array
    {
        return $this->eventReadRepository->findEventById($tenantId, $eventId);
    }
}
