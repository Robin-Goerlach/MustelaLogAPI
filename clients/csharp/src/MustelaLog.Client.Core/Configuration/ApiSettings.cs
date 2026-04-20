namespace MustelaLog.Client.Core.Configuration;

/// <summary>
/// Beschreibt die technische Verbindung zur MustelaLogAPI.
/// </summary>
public sealed class ApiSettings
{
    /// <summary>Basis-URL des Front Controllers inklusive <c>index.php</c>.</summary>
    public string BaseUrl { get; set; } = "http://127.0.0.1:8080/index.php";

    /// <summary>Fachliche API-Version im Route-Pfad.</summary>
    public string ApiVersion { get; set; } = "v1";

    /// <summary>Technischer Token für V1.</summary>
    public string TechnicalAccessToken { get; set; } = string.Empty;

    /// <summary>Timeout für HTTP-Requests.</summary>
    public int TimeoutSeconds { get; set; } = 30;

    /// <summary>Standardgröße für Eventseiten.</summary>
    public int DefaultPageSize { get; set; } = 100;
}
