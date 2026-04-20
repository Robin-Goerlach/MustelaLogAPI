namespace MustelaLog.Client.Core.Configuration;

/// <summary>Konfiguration des lokalen Diagnose-Loggings.</summary>
public sealed class DiagnosticsSettings
{
    public bool Enabled { get; set; } = true;
    public string MinimumLevel { get; set; } = "Information";
    public string FilePath { get; set; } = @"%LocalAppData%\MustelaLogClient\logs\client.log";
    public long MaxFileSizeBytes { get; set; } = 1_048_576;
    public int MaxRetainedFiles { get; set; } = 5;
    public int InMemoryBufferSize { get; set; } = 500;
}
