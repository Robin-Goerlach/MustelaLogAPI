using Xunit;
using MustelaLog.Client.Core.Diagnostics;

namespace MustelaLog.Client.Core.Tests;

/// <summary>
/// Sichert ab, dass Diagnose-Logging keine offensichtlichen Geheimnisse im Klartext enthält.
/// Diese Tests sind wichtig, weil der Client zwar lokal diagnostisch loggen soll,
/// dabei aber Tokens und ähnliche Werte maskieren muss.
/// </summary>
public sealed class LogSanitizerTests
{
    [Fact]
    public void MaskSecrets_ReplacesAuthorizationToken()
    {
        var input = "Authorization: Bearer very-secret-token";

        var result = LogSanitizer.MaskSecrets(input);

        Assert.DoesNotContain("very-secret-token", result);
        Assert.Contains("Authorization: ***", result);
    }

    [Fact]
    public void SerializeContext_MasksSensitiveKeys()
    {
        var context = new Dictionary<string, object?>
        {
            ["requestUrl"] = "http://localhost",
            ["accessToken"] = "top-secret",
            ["apiKey"] = "another-secret"
        };

        var result = LogSanitizer.SerializeContext(context);

        Assert.DoesNotContain("top-secret", result);
        Assert.DoesNotContain("another-secret", result);
        Assert.Contains("\"accessToken\":\"***\"", result);
    }
}
