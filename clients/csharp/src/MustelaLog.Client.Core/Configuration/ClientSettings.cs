namespace MustelaLog.Client.Core.Configuration;

/// <summary>Oberste Konfigurationsklasse für den Desktop-Client.</summary>
public sealed class ClientSettings
{
    public ApiSettings Api { get; set; } = new();
    public DiagnosticsSettings Diagnostics { get; set; } = new();
    public UiSettings Ui { get; set; } = new();
}
