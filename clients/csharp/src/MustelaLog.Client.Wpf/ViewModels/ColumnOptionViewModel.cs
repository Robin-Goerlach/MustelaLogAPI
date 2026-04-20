namespace MustelaLog.Client.Wpf.ViewModels;

/// <summary>Beschreibt eine ein- und ausblendbare Tabellen-Spalte.</summary>
public sealed class ColumnOptionViewModel : ObservableObject
{
    private bool _isVisible;

    public string Key { get; init; } = string.Empty;
    public string DisplayName { get; init; } = string.Empty;

    public bool IsVisible
    {
        get => _isVisible;
        set => SetProperty(ref _isVisible, value);
    }
}
