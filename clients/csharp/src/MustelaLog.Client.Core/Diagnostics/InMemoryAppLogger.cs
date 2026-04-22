using MustelaLog.Client.Core.Abstractions;
using MustelaLog.Client.Core.Enums;

namespace MustelaLog.Client.Core.Diagnostics;

/// <summary>
/// Hält eine begrenzte Menge Diagnosemeldungen im Speicher.
/// 
/// Der Logger darf die Anwendung nicht destabilisieren. Deshalb werden sowohl
/// das Puffern als auch das Benachrichtigen der Live-Ansicht defensiv behandelt.
/// </summary>
public sealed class InMemoryAppLogger : IAppLogger
{
    private readonly object _sync = new();
    private readonly Queue<LogEntry> _entries;

    public InMemoryAppLogger(int maxEntries, ClientLogLevel minimumLevel = ClientLogLevel.Trace)
    {
        MaxEntries = Math.Max(50, maxEntries);
        MinimumLevel = minimumLevel;
        _entries = new Queue<LogEntry>(MaxEntries);
    }

    public int MaxEntries { get; }

    /// <summary>Unterhalb dieses Levels werden Einträge verworfen.</summary>
    public ClientLogLevel MinimumLevel { get; }

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
        if (level > MinimumLevel)
        {
            return;
        }

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

        var handlers = EntryWritten;
        if (handlers is null)
        {
            return;
        }

        foreach (EventHandler<LogEntry> handler in handlers.GetInvocationList())
        {
            try
            {
                handler(this, entry);
            }
            catch
            {
                // Diagnosebeobachter dürfen nie den eigentlichen Programmfluss brechen.
            }
        }
    }

    public void Error(string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Error, message, exception, context);
    public void Warning(string message, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Warning, message, null, context);
    public void Information(string message, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Information, message, null, context);
    public void Debug(string message, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Debug, message, null, context);
}
