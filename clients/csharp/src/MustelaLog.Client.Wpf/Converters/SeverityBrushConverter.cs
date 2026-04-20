using System.Globalization;
using System.Windows.Data;
using System.Windows.Media;

namespace MustelaLog.Client.Wpf.Converters;

/// <summary>Hebt Severity-Werte farblich zurückhaltend hervor.</summary>
public sealed class SeverityBrushConverter : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        var text = (value?.ToString() ?? string.Empty).ToLowerInvariant();
        if (text.Contains("fatal") || text.Contains("critical") || text.Contains("error")) return Brushes.IndianRed;
        if (text.Contains("warn")) return Brushes.DarkOrange;
        if (text.Contains("debug") || text.Contains("trace")) return Brushes.SlateBlue;
        return Brushes.DarkGreen;
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => Binding.DoNothing;
}
