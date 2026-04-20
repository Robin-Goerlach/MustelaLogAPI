using MustelaLog.Client.Core.Abstractions;
using MustelaLog.Client.Core.Enums;

namespace MustelaLog.Client.Core.Diagnostics;

/// <summary>Verteilt Diagnosemeldungen an mehrere Logger-Implementierungen.</summary>
public sealed class CompositeAppLogger : IAppLogger
{
    private readonly IReadOnlyList<IAppLogger> _targets;

    public CompositeAppLogger(params IAppLogger[] targets)
    {
        _targets = targets ?? Array.Empty<IAppLogger>();
    }

    public void Log(ClientLogLevel level, string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null)
    {
        foreach (var target in _targets)
        {
            target.Log(level, message, exception, context);
        }
    }

    public void Error(string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null)
    {
        foreach (var target in _targets)
        {
            target.Error(message, exception, context);
        }
    }

    public void Warning(string message, IReadOnlyDictionary<string, object?>? context = null)
    {
        foreach (var target in _targets)
        {
            target.Warning(message, context);
        }
    }

    public void Information(string message, IReadOnlyDictionary<string, object?>? context = null)
    {
        foreach (var target in _targets)
        {
            target.Information(message, context);
        }
    }

    public void Debug(string message, IReadOnlyDictionary<string, object?>? context = null)
    {
        foreach (var target in _targets)
        {
            target.Debug(message, context);
        }
    }
}
