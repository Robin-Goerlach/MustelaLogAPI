using MustelaLog.Client.Core.Abstractions;
using MustelaLog.Client.Core.Enums;

namespace MustelaLog.Client.Core.Diagnostics;

/// <summary>
/// Verteilt Diagnosemeldungen an mehrere Logger-Implementierungen.
/// 
/// Fehler einzelner Targets werden geschluckt, damit ein defektes Diagnoseziel
/// nicht den restlichen Client beschädigt.
/// </summary>
public sealed class CompositeAppLogger : IAppLogger
{
    private readonly IReadOnlyList<IAppLogger> _targets;

    public CompositeAppLogger(params IAppLogger[] targets)
    {
        _targets = targets ?? Array.Empty<IAppLogger>();
    }

    public void Log(ClientLogLevel level, string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null)
        => ForEachTarget(target => target.Log(level, message, exception, context));

    public void Error(string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null)
        => ForEachTarget(target => target.Error(message, exception, context));

    public void Warning(string message, IReadOnlyDictionary<string, object?>? context = null)
        => ForEachTarget(target => target.Warning(message, context));

    public void Information(string message, IReadOnlyDictionary<string, object?>? context = null)
        => ForEachTarget(target => target.Information(message, context));

    public void Debug(string message, IReadOnlyDictionary<string, object?>? context = null)
        => ForEachTarget(target => target.Debug(message, context));

    private void ForEachTarget(Action<IAppLogger> action)
    {
        foreach (var target in _targets)
        {
            try
            {
                action(target);
            }
            catch
            {
                // Diagnosefehler sind absichtlich nicht fatal.
            }
        }
    }
}
