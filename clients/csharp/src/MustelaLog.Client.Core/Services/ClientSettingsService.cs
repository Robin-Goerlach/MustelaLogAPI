using System.Text.Json;
using MustelaLog.Client.Core.Configuration;

namespace MustelaLog.Client.Core.Services;

/// <summary>Lädt und speichert die Client-Konfiguration.</summary>
public sealed class ClientSettingsService
{
    private static readonly JsonSerializerOptions SerializerOptions = new()
    {
        PropertyNameCaseInsensitive = true,
        WriteIndented = true
    };

    public ClientSettings Load(string settingsFilePath)
    {
        ClientSettings settings;
        if (File.Exists(settingsFilePath))
        {
            var json = File.ReadAllText(settingsFilePath);
            settings = JsonSerializer.Deserialize<ClientSettings>(json, SerializerOptions) ?? new ClientSettings();
        }
        else
        {
            settings = new ClientSettings();
        }

        ApplyEnvironmentOverrides(settings);
        return settings;
    }

    public void EnsureExampleExists(string filePath, ClientSettings settings)
    {
        if (File.Exists(filePath))
        {
            return;
        }

        var directory = Path.GetDirectoryName(filePath);
        if (!string.IsNullOrWhiteSpace(directory))
        {
            Directory.CreateDirectory(directory);
        }

        File.WriteAllText(filePath, JsonSerializer.Serialize(settings, SerializerOptions));
    }

    private static void ApplyEnvironmentOverrides(ClientSettings settings)
    {
        var baseUrl = Environment.GetEnvironmentVariable("MUSTELA_CLIENT_BASE_URL");
        if (!string.IsNullOrWhiteSpace(baseUrl))
        {
            settings.Api.BaseUrl = baseUrl;
        }

        var token = Environment.GetEnvironmentVariable("MUSTELA_CLIENT_TECHNICAL_TOKEN");
        if (!string.IsNullOrWhiteSpace(token))
        {
            settings.Api.TechnicalAccessToken = token;
        }

        settings.Diagnostics.FilePath = Environment.ExpandEnvironmentVariables(settings.Diagnostics.FilePath);
    }
}
