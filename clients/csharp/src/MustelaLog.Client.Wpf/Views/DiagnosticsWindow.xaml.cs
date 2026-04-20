using System.Windows;
using MustelaLog.Client.Wpf.ViewModels;

namespace MustelaLog.Client.Wpf.Views;

/// <summary>Fenster für die optionale Live-Diagnoseansicht.</summary>
public partial class DiagnosticsWindow : Window
{
    private readonly DiagnosticsWindowViewModel _viewModel;

    public DiagnosticsWindow(DiagnosticsWindowViewModel viewModel)
    {
        InitializeComponent();
        _viewModel = viewModel;
        DataContext = _viewModel;
    }

    private void Copy_Click(object sender, RoutedEventArgs e) => _viewModel.CopyCommand.Execute(null);
    private void Clear_Click(object sender, RoutedEventArgs e) => _viewModel.ClearCommand.Execute(null);
    private void Pause_Click(object sender, RoutedEventArgs e) => _viewModel.TogglePauseCommand.Execute(null);
}
