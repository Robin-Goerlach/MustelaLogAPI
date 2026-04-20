<?php

declare(strict_types=1);

namespace App\Routing;

use App\Error\ApiException;
use App\Http\Request;
use App\Http\Response;
use App\Security\ApiAuthenticator;
use App\Security\AuthorizationService;
use App\Security\FileRateLimiter;

/**
 * Kleiner Router ohne Framework-Abhängigkeit.
 *
 * Die Klasse löst eine Route anhand von HTTP-Methode und dem über `?route=`
 * übergebenen Pfad auf. Sie passt damit gut zu Shared Hosting ohne Rewrite.
 */
final class Router
{
    /**
     * @var list<Route>
     */
    private array $routes = [];

    private ?ApiAuthenticator $authenticator = null;

    private ?AuthorizationService $authorizationService = null;

    private ?FileRateLimiter $rateLimiter = null;

    public function __construct(private readonly string $routeParameterName)
    {
    }

    public function addRoute(Route $route): void
    {
        $this->routes[] = $route;
    }

    public function setAuthenticator(ApiAuthenticator $authenticator): void
    {
        $this->authenticator = $authenticator;
    }

    public function setAuthorizationService(AuthorizationService $authorizationService): void
    {
        $this->authorizationService = $authorizationService;
    }

    public function setRateLimiter(FileRateLimiter $rateLimiter): void
    {
        $this->rateLimiter = $rateLimiter;
    }

    /**
     * Dispatcht eine Anfrage an den passenden Controller.
     *
     * @throws ApiException
     */
    public function dispatch(Request $request): Response
    {
        foreach ($this->routes as $route) {
            if ($route->getMethod() !== $request->getMethod()) {
                continue;
            }

            $params = $this->matchPath($route->getPathPattern(), $request->getRoutePath());
            if ($params === null) {
                continue;
            }

            $request = $request->withPathParams($params);

            if ($route->getAuthType() !== null) {
                if ($this->authenticator === null || $this->authorizationService === null) {
                    throw new ApiException(500, 'router_not_initialized', 'An internal error occurred.');
                }

                $principal = $this->authenticator->authenticate($request, $route->getAuthType());
                $this->authorizationService->assertScopes($principal->scopes, $route->getRequiredScopes());
                if ($this->rateLimiter !== null) {
                    $this->rateLimiter->assertAllowed($principal, $request->getRoutePath());
                }
                $request = $request->withAuthenticatedPrincipal($principal);
            }

            $handler = $route->getHandler();

            return $handler($request);
        }

        throw new ApiException(404, 'route_not_found', 'The requested endpoint does not exist.');
    }

    /**
     * Prüft, ob ein Route-Pattern auf einen Pfad passt.
     *
     * @return array<string, string>|null
     */
    private function matchPath(string $pattern, string $path): ?array
    {
        $patternParts = array_values(array_filter(explode('/', trim($pattern, '/')), 'strlen'));
        $pathParts = array_values(array_filter(explode('/', trim($path, '/')), 'strlen'));

        if (count($patternParts) !== count($pathParts)) {
            return null;
        }

        $params = [];

        foreach ($patternParts as $index => $patternPart) {
            $pathPart = $pathParts[$index];

            if (preg_match('/^\{([a-zA-Z][a-zA-Z0-9_]*)\}$/', $patternPart, $matches) === 1) {
                $params[$matches[1]] = $pathPart;
                continue;
            }

            if ($patternPart !== $pathPart) {
                return null;
            }
        }

        return $params;
    }
}
