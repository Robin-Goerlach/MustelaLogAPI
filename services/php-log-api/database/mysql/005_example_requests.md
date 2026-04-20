# Beispiel-Requests

## Health

```bash
curl -s \
  "https://example.com/index.php?route=/api/v1/health"
```

## Ingest

```bash
curl -s \
  -X POST \
  -H "Authorization: Bearer source-demo-token" \
  -H "Content-Type: application/json" \
  "https://example.com/index.php?route=/api/v1/ingest/events" \
  -d '{
    "occurredAt": "2026-04-20T10:20:30Z",
    "observedAt": "2026-04-20T10:20:31Z",
    "severityNumber": 17,
    "severityText": "ERROR",
    "eventName": "user.login.failed",
    "eventCategory": "authentication",
    "eventAction": "login",
    "eventOutcome": "failure",
    "message": "Login failed for supplied credentials",
    "hostName": "sample-api-01",
    "serviceName": "sample-api",
    "traceId": "trace-123",
    "correlationId": "corr-123",
    "attributes": {
      "usernameHint": "masked-user",
      "reason": "invalid_credentials"
    }
  }'
```

## Events lesen

```bash
curl -s \
  -H "Authorization: Bearer reader-demo-token" \
  "https://example.com/index.php?route=/api/v1/events&page=1&pageSize=20"
```

## Einzelnes Event lesen

```bash
curl -s \
  -H "Authorization: Bearer reader-demo-token" \
  "https://example.com/index.php?route=/api/v1/events/000000000001ABCDEF000999"
```

## Quellen lesen

```bash
curl -s \
  -H "Authorization: Bearer reader-demo-token" \
  "https://example.com/index.php?route=/api/v1/sources"
```
