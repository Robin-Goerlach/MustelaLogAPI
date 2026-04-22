using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace MustelaLog.Client.Wpf.Converters;

/// <summary>
/// Wandelt einen String in <see cref="Visibility"/> um.
/// Leere oder nur aus Leerzeichen bestehende Texte werden ausgeblendet.
/// </summary>
public sealed class StringHasTextToVisibilityConverter : IValueConverter
{
    /// <inheritdoc />
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        => value is string text && !string.IsNullOrWhiteSpace(text) ? Visibility.Visible : Visibility.Collapsed;

    /// <inheritdoc />
    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => Binding.DoNothing;
}
