using System.Text;
using MustelaLog.Client.Core.Abstractions;
using MustelaLog.Client.Core.Enums;

namespace MustelaLog.Client.Core.Diagnostics;

/// <summary>
/// Schreibt Diagnosemeldungen in eine rotierende lokale Datei.
/// 
/// Schreibfehler werden bewusst geschluckt, weil das Diagnose-Logging den
/// Client nicht destabilisieren darf.
/// </summary>
public sealed class FileAppLogger : IAppLogger
{
    private readonly object _sync = new();

    public FileAppLogger(string filePath, long maxFileSizeBytes, int maxRetainedFiles, ClientLogLevel minimumLevel = ClientLogLevel.Trace)
    {
        FilePath = Environment.ExpandEnvironmentVariables(filePath);
        MaxFileSizeBytes = Math.Max(128 * 1024, maxFileSizeBytes);
        MaxRetainedFiles = Math.Max(1, maxRetainedFiles);
        MinimumLevel = minimumLevel;
    }

    public string FilePath { get; }
    public long MaxFileSizeBytes { get; }
    public int MaxRetainedFiles { get; }
    public ClientLogLevel MinimumLevel { get; }

    public void Log(ClientLogLevel level, string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null)
    {
        if (level > MinimumLevel)
        {
            return;
        }

        try
        {
            var entry = new LogEntry
            {
                TimestampUtc = DateTimeOffset.UtcNow,
                Level = level,
                Message = LogSanitizer.MaskSecrets(message),
                ContextText = LogSanitizer.MaskSecrets(LogSanitizer.SerializeContext(context)),
                ExceptionText = exception is null ? null : LogSanitizer.MaskSecrets(exception.ToString())
            };
            WriteEntry(entry);
        }
        catch
        {
        }
    }

    public void Error(string message, Exception? exception = null, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Error, message, exception, context);
    public void Warning(string message, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Warning, message, null, context);
    public void Information(string message, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Information, message, null, context);
    public void Debug(string message, IReadOnlyDictionary<string, object?>? context = null) => Log(ClientLogLevel.Debug, message, null, context);

    private void WriteEntry(LogEntry entry)
    {
        lock (_sync)
        {
            var directory = Path.GetDirectoryName(FilePath);
            if (!string.IsNullOrWhiteSpace(directory))
            {
                Directory.CreateDirectory(directory);
            }

            RotateIfNeeded();
            File.AppendAllText(FilePath, entry.ToSingleLineText() + Environment.NewLine, new UTF8Encoding(true));
        }
    }

    private void RotateIfNeeded()
    {
        if (!File.Exists(FilePath))
        {
            return;
        }

        var info = new FileInfo(FilePath);
        if (info.Length < MaxFileSizeBytes)
        {
            return;
        }

        var directory = info.DirectoryName ?? ".";
        var fileName = Path.GetFileNameWithoutExtension(FilePath);
        var extension = Path.GetExtension(FilePath);

        for (var index = MaxRetainedFiles - 1; index >= 1; index--)
        {
            var older = Path.Combine(directory, $"{fileName}.{index}{extension}");
            var newer = Path.Combine(directory, $"{fileName}.{index + 1}{extension}");
            if (File.Exists(older))
            {
                if (index + 1 > MaxRetainedFiles)
                {
                    File.Delete(older);
                }
                else
                {
                    File.Move(older, newer, true);
                }
            }
        }

        var firstArchive = Path.Combine(directory, $"{fileName}.1{extension}");
        File.Move(FilePath, firstArchive, true);
    }
}
