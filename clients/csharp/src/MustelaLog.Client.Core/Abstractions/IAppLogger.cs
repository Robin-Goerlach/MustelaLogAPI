using MustelaLog.Client.Core.Enums;

namespace MustelaLog.Client.Core.Abstractions;

/// <summary>Kleine Logging-Abstraktion für den Desktop-Client.</summary>
public interface IAppLogger
{
    void Log(ClientLogLevel level, string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null);
    void Error(string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null);
    void Warning(string message, IReadOnlyDictionary<string, object?>? context = null);
    void Information(string message, IReadOnlyDictionary<string, object?>? context = null);
    void Debug(string message, IReadOnlyDictionary<string, object?>? context = null);
}
