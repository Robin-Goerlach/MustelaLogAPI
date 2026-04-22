using System.ComponentModel;
using System.Windows;
using System.Windows.Controls;
using Microsoft.Win32;
using MustelaLog.Client.Core.Enums;
using MustelaLog.Client.Wpf.ViewModels;

namespace MustelaLog.Client.Wpf.Views;

/// <summary>
/// Hauptfenster des Desktop-Clients.
/// 
/// Ein kleiner Teil der UI-spezifischen Logik bleibt bewusst im Code-Behind:
/// Fensteröffnung, Clipboard, SaveFileDialog und DataGrid-Spaltenzustand sind
/// eng an WPF gekoppelt und würden in einem strengeren MVVM-Modell eher mehr
/// Komplexität als Klarheit erzeugen.
/// </summary>
public partial class MainWindow : Window
{
    private static readonly HashSet<string> SupportedServerSortFields = new(StringComparer.OrdinalIgnoreCase)
    {
        "occurredAt",
        "severityNumber",
        "sourceKey"
    };

    private readonly MainWindowViewModel _viewModel;
    private DiagnosticsWindow? _diagnosticsWindow;

    /// <summary>Erzeugt das Hauptfenster für das bereitgestellte ViewModel.</summary>
    public MainWindow(MainWindowViewModel viewModel)
    {
        InitializeComponent();
        _viewModel = viewModel;
        DataContext = _viewModel;

        Loaded += async (_, _) =>
        {
            WireColumnOptionChanges();
            ApplyTimeModeSelection();
            ApplyColumnVisibility();
            await _viewModel.InitializeAsync();
        };
    }

    private void WireColumnOptionChanges()
    {
        foreach (var option in _viewModel.ColumnOptions)
        {
            option.PropertyChanged += ColumnOptionOnPropertyChanged;
        }
    }

    private void ColumnOptionOnPropertyChanged(object? sender, PropertyChangedEventArgs e)
    {
        if (e.PropertyName == nameof(ColumnOptionViewModel.IsVisible))
        {
            ApplyColumnVisibility();
        }
    }

    private void ApplyTimeModeSelection()
    {
        if (FindName("TimeModeComboBox") is ComboBox combo)
        {
            combo.SelectedIndex = _viewModel.TimeMode == TimeDisplayMode.Utc ? 1 : 0;
        }
    }

    private void ApplyColumnVisibility()
    {
        var grid = GetEventsDataGrid();
        if (grid is null)
        {
            return;
        }

        // Die Spaltenreihenfolge ist in V1 fest. Dadurch können wir die Sichtbarkeit
        // ohne WPF-spezifische Spalten-Felder zuverlässig über den Index steuern.
        SetColumnVisibility(grid, 0, "occurredAt");
        SetColumnVisibility(grid, 1, "ingestedAt");
        SetColumnVisibility(grid, 2, "severity");
        SetColumnVisibility(grid, 3, "sourceOrHost");
        SetColumnVisibility(grid, 4, "service");
        SetColumnVisibility(grid, 5, "category");
        SetColumnVisibility(grid, 6, "action");
        SetColumnVisibility(grid, 7, "outcome");
        SetColumnVisibility(grid, 8, "message");
        SetColumnVisibility(grid, 9, "correlation");
        SetColumnVisibility(grid, 10, "eventId");
    }

    private static void SetColumnVisibility(DataGrid grid, int index, string key)
    {
        if (index < 0 || index >= grid.Columns.Count)
        {
            return;
        }

        if (grid.DataContext is not MainWindowViewModel vm)
        {
            return;
        }

        var visible = vm.ColumnOptions.FirstOrDefault(c => c.Key == key)?.IsVisible ?? true;
        grid.Columns[index].Visibility = visible ? Visibility.Visible : Visibility.Collapsed;
    }

    private DataGrid? GetEventsDataGrid() => FindName("EventsDataGrid") as DataGrid;

    private async void Refresh_Click(object sender, RoutedEventArgs e) => await _viewModel.RefreshAsync();

    private async void ClearFilters_Click(object sender, RoutedEventArgs e)
    {
        _viewModel.ClearFilters();
        await _viewModel.RefreshAsync();
    }

    private async void ApplyQuickRange_Click(object sender, RoutedEventArgs e)
    {
        _viewModel.ApplySelectedQuickRange();
        await _viewModel.RefreshAsync();
    }

    private async void PreviousPage_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadPreviousPageAsync();
    private async void NextPage_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadNextPageAsync();
    private void SaveView_Click(object sender, RoutedEventArgs e) => _viewModel.SaveCurrentView();

    private async void LoadView_Click(object sender, RoutedEventArgs e)
    {
        _viewModel.LoadSelectedView();
        ApplyColumnVisibility();
        await _viewModel.RefreshAsync();
    }

    private void RenameView_Click(object sender, RoutedEventArgs e) => _viewModel.RenameSelectedView();
    private void DeleteView_Click(object sender, RoutedEventArgs e) => _viewModel.DeleteSelectedView();

    private async void EventsDataGrid_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        await _viewModel.LoadSelectedEventDetailsAsync();
    }

    private async void EventsDataGrid_Sorting(object sender, DataGridSortingEventArgs e)
    {
        e.Handled = true;
        var sortField = e.Column.SortMemberPath;
        if (string.IsNullOrWhiteSpace(sortField) || !SupportedServerSortFields.Contains(sortField))
        {
            _viewModel.StatusMessage = $"Sorting by '{e.Column.Header}' is not exposed by the V1 API yet.";
            e.Column.SortDirection = null;
            return;
        }

        var ascending = e.Column.SortDirection != ListSortDirection.Ascending;

        if (sender is DataGrid grid)
        {
            foreach (var column in grid.Columns)
            {
                column.SortDirection = null;
            }
        }

        e.Column.SortDirection = ascending ? ListSortDirection.Ascending : ListSortDirection.Descending;
        _viewModel.SetSort(sortField, ascending);
        await _viewModel.RefreshAsync();
    }

    private void TimeModeCombo_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        if (!IsLoaded || sender is not ComboBox combo)
        {
            return;
        }

        _viewModel.TimeMode = combo.SelectedIndex == 1 ? TimeDisplayMode.Utc : TimeDisplayMode.Local;
    }

    private async void RelatedCorrelation_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadRelatedEventsAsync("correlation");
    private async void RelatedTrace_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadRelatedEventsAsync("trace");
    private async void RelatedRequest_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadRelatedEventsAsync("request");
    private async void RelatedSource_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadRelatedEventsAsync("source");
    private async void RelatedSession_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadRelatedEventsAsync("session");
    private async void RelatedActor_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadRelatedEventsAsync("actor");

    private async void ExportCsv_Click(object sender, RoutedEventArgs e)
    {
        var dialog = new SaveFileDialog
        {
            Filter = "CSV files (*.csv)|*.csv",
            FileName = $"mustelalog-events-{DateTime.Now:yyyyMMdd-HHmmss}.csv"
        };

        if (dialog.ShowDialog(this) == true)
        {
            try
            {
                await _viewModel.ExportCsvAsync(dialog.FileName);
                MessageBox.Show(this, "CSV export completed.", "MustelaLog Client", MessageBoxButton.OK, MessageBoxImage.Information);
            }
            catch (Exception exception)
            {
                MessageBox.Show(this, $"CSV export failed.\n\n{exception.Message}", "MustelaLog Client", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }

    private async void ExportJson_Click(object sender, RoutedEventArgs e)
    {
        var dialog = new SaveFileDialog
        {
            Filter = "JSON files (*.json)|*.json",
            FileName = $"mustelalog-events-{DateTime.Now:yyyyMMdd-HHmmss}.json"
        };

        if (dialog.ShowDialog(this) == true)
        {
            try
            {
                await _viewModel.ExportJsonAsync(dialog.FileName);
                MessageBox.Show(this, "JSON export completed.", "MustelaLog Client", MessageBoxButton.OK, MessageBoxImage.Information);
            }
            catch (Exception exception)
            {
                MessageBox.Show(this, $"JSON export failed.\n\n{exception.Message}", "MustelaLog Client", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }

    private void OpenDiagnostics_Click(object sender, RoutedEventArgs e)
    {
        if (_diagnosticsWindow is { IsVisible: true })
        {
            _diagnosticsWindow.Activate();
            return;
        }

        var vm = new DiagnosticsWindowViewModel(_viewModel.InMemoryLogger);
        _diagnosticsWindow = new DiagnosticsWindow(vm) { Owner = this };
        _diagnosticsWindow.Closed += (_, _) => _diagnosticsWindow = null;
        _diagnosticsWindow.Show();
    }

    private void SendTestLog_Click(object sender, RoutedEventArgs e)
    {
        var vm = new SendTestLogDialogViewModel(_viewModel.ApiClient, _viewModel.Logger, _viewModel.SourceSnapshot);
        var dialog = new SendTestLogDialog(vm) { Owner = this };
        if (dialog.ShowDialog() == true)
        {
            _ = _viewModel.RefreshAsync();
        }
    }

    private void CopyEventId_Click(object sender, RoutedEventArgs e) => CopyText(_viewModel.SelectedEvent?.Record.LogEventId);
    private void CopyMessage_Click(object sender, RoutedEventArgs e) => CopyText(_viewModel.SelectedEvent?.Record.MessageText);
    private void CopyJson_Click(object sender, RoutedEventArgs e) => CopyText(_viewModel.SelectedEventPrettyAttributes);
    private void CopyCorrelation_Click(object sender, RoutedEventArgs e) => CopyText(_viewModel.SelectedEvent?.Record.CorrelationId);
    private void CopyTrace_Click(object sender, RoutedEventArgs e) => CopyText(_viewModel.SelectedEvent?.Record.TraceId);

    private static void CopyText(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return;
        }

        try
        {
            Clipboard.SetText(value);
        }
        catch
        {
            // Ein blockiertes Clipboard soll keine Benutzeraktion mit Ausnahme abbrechen.
        }
    }
}
