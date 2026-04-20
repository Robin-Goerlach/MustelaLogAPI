using System.Collections.ObjectModel;
using System.Windows;
using MustelaLog.Client.Core.Diagnostics;

namespace MustelaLog.Client.Wpf.ViewModels;

/// <summary>ViewModel des optionalen Diagnosefensters.</summary>
public sealed class DiagnosticsWindowViewModel : ObservableObject
{
    private readonly InMemoryAppLogger _logger;
    private bool _isPaused;

    public DiagnosticsWindowViewModel(InMemoryAppLogger logger)
    {
        _logger = logger;
        Entries = new ObservableCollection<LogEntry>(_logger.Snapshot());
        _logger.EntryWritten += HandleEntryWritten;

        ClearCommand = new RelayCommand(Clear);
        CopyCommand = new RelayCommand(CopyAll);
        TogglePauseCommand = new RelayCommand(() =>
        {
            IsPaused = !IsPaused;
            OnPropertyChanged(nameof(PauseButtonText));
        });
    }

    public ObservableCollection<LogEntry> Entries { get; }
    public RelayCommand ClearCommand { get; }
    public RelayCommand CopyCommand { get; }
    public RelayCommand TogglePauseCommand { get; }

    public bool IsPaused
    {
        get => _isPaused;
        set => SetProperty(ref _isPaused, value);
    }

    public string PauseButtonText => IsPaused ? "Resume" : "Pause";

    private void HandleEntryWritten(object? sender, LogEntry entry)
    {
        if (IsPaused || Application.Current is null)
        {
            return;
        }

        Application.Current.Dispatcher.Invoke(() =>
        {
            Entries.Add(entry);
            while (Entries.Count > _logger.MaxEntries)
            {
                Entries.RemoveAt(0);
            }
        });
    }

    private void Clear()
    {
        _logger.Clear();
        Entries.Clear();
    }

    private void CopyAll()
    {
        var text = string.Join(Environment.NewLine, Entries.Select(e => e.ToSingleLineText()));
        Clipboard.SetText(text);
    }
}
