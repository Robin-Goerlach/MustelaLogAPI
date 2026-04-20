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
/// eng an WPF gekoppelt und würden in einem strengen MVVM-Modell eher mehr
/// Komplexität als Klarheit erzeugen.
/// </summary>
public partial class MainWindow : Window
{
    private readonly MainWindowViewModel _viewModel;
    private DiagnosticsWindow? _diagnosticsWindow;

    public MainWindow(MainWindowViewModel viewModel)
    {
        InitializeComponent();
        _viewModel = viewModel;
        DataContext = _viewModel;

        Loaded += async (_, _) =>
        {
            WireColumnOptionChanges();
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

    private void ApplyColumnVisibility()
    {
        SetColumnVisibility("occurredAt", OccurredAtColumn);
        SetColumnVisibility("ingestedAt", IngestedAtColumn);
        SetColumnVisibility("severity", SeverityColumn);
        SetColumnVisibility("sourceOrHost", SourceColumn);
        SetColumnVisibility("service", ServiceColumn);
        SetColumnVisibility("category", CategoryColumn);
        SetColumnVisibility("action", ActionColumn);
        SetColumnVisibility("outcome", OutcomeColumn);
        SetColumnVisibility("message", MessageColumn);
        SetColumnVisibility("correlation", CorrelationColumn);
        SetColumnVisibility("eventId", EventIdColumn);
    }

    private void SetColumnVisibility(string key, DataGridColumn column)
    {
        var visible = _viewModel.ColumnOptions.FirstOrDefault(c => c.Key == key)?.IsVisible ?? true;
        column.Visibility = visible ? Visibility.Visible : Visibility.Collapsed;
    }

    private async void Refresh_Click(object sender, RoutedEventArgs e) => await _viewModel.RefreshAsync();
    private void ClearFilters_Click(object sender, RoutedEventArgs e) => _viewModel.ClearFilters();
    private void ApplyQuickRange_Click(object sender, RoutedEventArgs e) => _viewModel.ApplySelectedQuickRange();
    private async void PreviousPage_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadPreviousPageAsync();
    private async void NextPage_Click(object sender, RoutedEventArgs e) => await _viewModel.LoadNextPageAsync();
    private void SaveView_Click(object sender, RoutedEventArgs e) => _viewModel.SaveCurrentView();
    private void LoadView_Click(object sender, RoutedEventArgs e) => _viewModel.LoadSelectedView();
    private void RenameView_Click(object sender, RoutedEventArgs e) => _viewModel.RenameSelectedView();
    private void DeleteView_Click(object sender, RoutedEventArgs e) => _viewModel.DeleteSelectedView();

    private void EventsDataGrid_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
    }

    private void EventsDataGrid_Sorting(object sender, DataGridSortingEventArgs e)
    {
        e.Handled = true;
        var ascending = e.Column.SortDirection != ListSortDirection.Ascending;
        foreach (var column in EventsDataGrid.Columns)
        {
            column.SortDirection = null;
        }

        e.Column.SortDirection = ascending ? ListSortDirection.Ascending : ListSortDirection.Descending;
        _viewModel.SetSort(e.Column.SortMemberPath, ascending);
        _ = _viewModel.RefreshAsync();
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
        if (!string.IsNullOrWhiteSpace(value))
        {
            Clipboard.SetText(value);
        }
    }
}
