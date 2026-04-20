using MustelaLog.Client.Core.Abstractions;
using MustelaLog.Client.Core.Enums;

namespace MustelaLog.Client.Core.Diagnostics;

/// <summary>Hält eine begrenzte Menge Diagnosemeldungen im Speicher.</summary>
public sealed class InMemoryAppLogger : IAppLogger
{
    private readonly object _sync = new();
    private readonly Queue<LogEntry> _entries;

    public InMemoryAppLogger(int maxEntries)
    {
        MaxEntries = Math.Max(50, maxEntries);
        _entries = new Queue<LogEntry>(MaxEntries);
    }

    public int MaxEntries { get; }
    public event EventHandler<LogEntry>? EntryWritten;

    public IReadOnlyList<LogEntry> Snapshot()
    {
        lock (_sync)
        {
            return _entries.ToList();
        }
    }

    public void Clear()
    {
        lock (_sync)
        {
            _entries.Clear();
        }
    }

    public void Log(ClientLogLevel level, string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null)
    {
        var entry = new LogEntry
        {
            TimestampUtc = DateTimeOffset.UtcNow,
            Level = level,
            Message = LogSanitizer.MaskSecrets(message),
            ContextText = LogSanitizer.MaskSecrets(LogSanitizer.SerializeContext(context)),
            ExceptionText = exception is null ? null : LogSanitizer.MaskSecrets(exception.ToString())
        };

        lock (_sync)
        {
            _entries.Enqueue(entry);
            while (_entries.Count > MaxEntries)
            {
                _entries.Dequeue();
            }
        }

        EntryWritten?.Invoke(this, entry);
    }

    public void Error(string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Error, message, exception, context);
    public void Warning(string message, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Warning, message, null, context);
    public void Information(string message, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Information, message, null, context);
    public void Debug(string message, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Debug, message, null, context);
}
