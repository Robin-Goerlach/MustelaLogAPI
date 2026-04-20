using System.Net.Http.Headers;

namespace MustelaLog.Client.Core.Abstractions;

/// <summary>Liefert Authorization-Header für API-Requests.</summary>
public interface ICredentialProvider
{
    ValueTask<AuthenticationHeaderValue?> GetAuthorizationHeaderAsync(CancellationToken cancellationToken = default);
}
