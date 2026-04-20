using System.Net.Http.Headers;
using MustelaLog.Client.Core.Abstractions;

namespace MustelaLog.Client.Core.Services;

/// <summary>Einfacher Credential-Provider für V1.</summary>
public sealed class StaticTokenCredentialProvider : ICredentialProvider
{
    private readonly string _token;

    public StaticTokenCredentialProvider(string token)
    {
        _token = token ?? string.Empty;
    }

    public ValueTask<AuthenticationHeaderValue?> GetAuthorizationHeaderAsync(CancellationToken cancellationToken = default)
    {
        if (string.IsNullOrWhiteSpace(_token))
        {
            return ValueTask.FromResult<AuthenticationHeaderValue?>(null);
        }

        return ValueTask.FromResult<AuthenticationHeaderValue?>(new AuthenticationHeaderValue("Bearer", _token));
    }
}
