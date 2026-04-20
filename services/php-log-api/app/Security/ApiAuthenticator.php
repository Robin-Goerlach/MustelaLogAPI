<?php

declare(strict_types=1);

namespace App\Security;

use App\Config\Config;
use App\Error\ApiException;
use App\Http\Request;
use App\Repository\ApiClientCredentialRepository;
use App\Repository\SourceCredentialRepository;

/**
 * Bearer-Token-Authentisierung für Quellen und API-Clients.
 *
 * Die Beispielimplementierung setzt bewusst auf ein Shared-Hosting-taugliches
 * Modell: TLS + Bearer-Token + Hashspeicherung + Scope-Prüfung + optionales
 * IP-Allowlisting. Höherwertige Verfahren wie Public-Key-Signaturen oder mTLS
 * können später ergänzt werden, ohne die Grundarchitektur zu zerlegen.
 */
final class ApiAuthenticator
{
    public function __construct(
        private readonly SourceCredentialRepository $sourceCredentialRepository,
        private readonly ApiClientCredentialRepository $apiClientCredentialRepository,
        private readonly Config $config
    ) {
    }

    /**
     * @throws ApiException
     */
    public function authenticate(Request $request, string $expectedType): AuthenticatedPrincipal
    {
        $authorization = $request->getHeader('authorization');
        if ($authorization === null || !preg_match('/^Bearer\s+(.+)$/i', $authorization, $matches)) {
            throw new ApiException(401, 'authentication_required', 'Authentication is required.');
        }

        $token = trim($matches[1]);
        if ($token === '') {
            throw new ApiException(401, 'invalid_token', 'Authentication failed.');
        }

        $tokenHash = $this->hashToken($token);

        if ($expectedType === 'source') {
            $principal = $this->sourceCredentialRepository->findActivePrincipalByTokenHash($tokenHash);
        } elseif ($expectedType === 'client') {
            $principal = $this->apiClientCredentialRepository->findActivePrincipalByTokenHash($tokenHash);
        } else {
            throw new ApiException(500, 'unsupported_auth_type', 'An internal error occurred.');
        }

        if ($principal === null) {
            throw new ApiException(401, 'invalid_token', 'Authentication failed.');
        }

        if ($principal->allowedNetworks !== [] && !$this->isAllowedIp($request->getRemoteIp(), $principal->allowedNetworks)) {
            throw new ApiException(403, 'source_network_denied', 'The calling IP address is not allowed.');
        }

        return $principal;
    }

    /**
     * Hashfunktion für Token.
     *
     * Optional kann ein Pepper verwendet werden. Der Pepper gehört aber nicht in
     * die Datenbank, sondern ausschließlich in die Anwendungs-Konfiguration.
     */
    private function hashToken(string $token): string
    {
        $pepper = $this->config->getNullableString('AUTH_TOKEN_PEPPER') ?? '';

        return hash('sha256', $pepper . $token);
    }

    /**
     * Prüft eine IP gegen erlaubte CIDR-Netze.
     *
     * @param array<int, string> $allowedNetworks
     */
    private function isAllowedIp(string $ip, array $allowedNetworks): bool
    {
        if ($ip === '') {
            return false;
        }

        foreach ($allowedNetworks as $cidr) {
            if ($this->ipMatchesCidr($ip, $cidr)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Führt eine einfache IPv4-CIDR-Prüfung durch.
     *
     * Für produktive Installationen mit IPv6-lastigen Umgebungen sollte diese
     * Hilfsfunktion später erweitert oder durch eine dedizierte Utility-Klasse
     * ersetzt werden.
     */
    private function ipMatchesCidr(string $ip, string $cidr): bool
    {
        if (!str_contains($cidr, '/')) {
            return $ip === $cidr;
        }

        [$network, $prefix] = explode('/', $cidr, 2);

        $ipLong = ip2long($ip);
        $networkLong = ip2long($network);

        if ($ipLong === false || $networkLong === false) {
            return false;
        }

        $prefixInt = (int) $prefix;
        $mask = -1 << (32 - $prefixInt);

        return ($ipLong & $mask) === ($networkLong & $mask);
    }
}
