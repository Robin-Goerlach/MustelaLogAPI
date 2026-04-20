namespace MustelaLog.Client.Core.Configuration;

/// <summary>UI-nahe Grundeinstellungen.</summary>
public sealed class UiSettings
{
    public string DefaultTimeMode { get; set; } = "Local";
    public bool AutoRefreshEnabled { get; set; } = false;
    public int AutoRefreshSeconds { get; set; } = 30;
}
