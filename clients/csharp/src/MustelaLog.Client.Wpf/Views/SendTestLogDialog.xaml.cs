using System.Windows;
using MustelaLog.Client.Wpf.ViewModels;

namespace MustelaLog.Client.Wpf.Views;

/// <summary>Dialog für manuell erzeugte Testevents.</summary>
public partial class SendTestLogDialog : Window
{
    private readonly SendTestLogDialogViewModel _viewModel;

    public SendTestLogDialog(SendTestLogDialogViewModel viewModel)
    {
        InitializeComponent();
        _viewModel = viewModel;
        DataContext = _viewModel;
    }

    private async void Send_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            await _viewModel.SendAsync();
            if (_viewModel.LastResult is not null)
            {
                MessageBox.Show(this, _viewModel.StatusMessage, "MustelaLog Client", MessageBoxButton.OK, MessageBoxImage.Information);
                DialogResult = true;
            }
        }
        catch (Exception exception)
        {
            MessageBox.Show(this, exception.Message, "MustelaLog Client", MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }

    private void Close_Click(object sender, RoutedEventArgs e)
    {
        DialogResult = _viewModel.LastResult is not null;
        Close();
    }
}
