<?php

declare(strict_types=1);

namespace App\Repository;

use PDO;

/**
 * Liest Events aus freigegebenen Views.
 */
final class EventReadRepository
{
    public function __construct(private readonly PDO $pdo)
    {
    }

    /**
     * @param array<string, string> $filters
     *
     * @return array{items:list<array<string,mixed>>,total:int}
     */
    public function findEvents(string $tenantId, array $filters, int $page, int $pageSize): array
    {
        $where = ['tenant_id = :tenant_id'];
        $params = ['tenant_id' => $tenantId];

        if (($filters['sourceKey'] ?? '') !== '') {
            $where[] = 'source_key = :source_key';
            $params['source_key'] = $filters['sourceKey'];
        }

        if (($filters['severityText'] ?? '') !== '') {
            $where[] = 'severity_text = :severity_text';
            $params['severity_text'] = $filters['severityText'];
        }

        if (($filters['traceId'] ?? '') !== '') {
            $where[] = 'trace_id = :trace_id';
            $params['trace_id'] = $filters['traceId'];
        }

        if (($filters['correlationId'] ?? '') !== '') {
            $where[] = 'correlation_id = :correlation_id';
            $params['correlation_id'] = $filters['correlationId'];
        }

        if (($filters['from'] ?? '') !== '') {
            $where[] = 'occurred_at >= :date_from';
            $params['date_from'] = $filters['from'];
        }

        if (($filters['to'] ?? '') !== '') {
            $where[] = 'occurred_at <= :date_to';
            $params['date_to'] = $filters['to'];
        }

        $whereSql = implode(' AND ', $where);

        $countStmt = $this->pdo->prepare(
            "SELECT COUNT(*) FROM api_v1_log_events WHERE {$whereSql}"
        );
        $countStmt->execute($params);
        $total = (int) $countStmt->fetchColumn();

        $offset = max(0, ($page - 1) * $pageSize);

        // Sortierung wird absichtlich hart auf sichere Whitelist abgebildet.
        $sortFieldMap = [
            'occurredAt' => 'occurred_at',
            'severityNumber' => 'severity_number',
            'sourceKey' => 'source_key',
        ];

        $sortField = $sortFieldMap[$filters['sort'] ?? 'occurredAt'] ?? 'occurred_at';
        $sortDirection = strtoupper($filters['direction'] ?? 'DESC') === 'ASC' ? 'ASC' : 'DESC';

        $sql = <<<SQL
SELECT *
FROM api_v1_log_events
WHERE {$whereSql}
ORDER BY {$sortField} {$sortDirection}
LIMIT :limit OFFSET :offset
SQL;

        $stmt = $this->pdo->prepare($sql);

        foreach ($params as $key => $value) {
            $stmt->bindValue(':' . $key, $value);
        }

        $stmt->bindValue(':limit', $pageSize, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        $items = $stmt->fetchAll();

        return [
            'items' => is_array($items) ? $items : [],
            'total' => $total,
        ];
    }

    /**
     * @return array<string,mixed>|null
     */
    public function findEventById(string $tenantId, string $eventId): ?array
    {
        $stmt = $this->pdo->prepare(
            'SELECT * FROM api_v1_log_events WHERE tenant_id = :tenant_id AND log_event_id = :event_id LIMIT 1'
        );
        $stmt->execute([
            'tenant_id' => $tenantId,
            'event_id' => $eventId,
        ]);

        $row = $stmt->fetch();

        return is_array($row) ? $row : null;
    }
}
