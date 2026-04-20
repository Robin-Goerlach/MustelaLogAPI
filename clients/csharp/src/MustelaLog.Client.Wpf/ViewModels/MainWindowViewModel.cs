using System.Collections.ObjectModel;
using MustelaLog.Client.Core.Abstractions;
using MustelaLog.Client.Core.Configuration;
using MustelaLog.Client.Core.Diagnostics;
using MustelaLog.Client.Core.Enums;
using MustelaLog.Client.Core.Models;
using MustelaLog.Client.Core.Services;

namespace MustelaLog.Client.Wpf.ViewModels;

/// <summary>
/// Zentrales ViewModel des Hauptfensters.
/// 
/// Die Klasse bündelt die V1-Anwendungsfälle: Laden, Filtern, Paging,
/// Aggregation, Related Events, Saved Views und Exportvorbereitung.
/// </summary>
public sealed class MainWindowViewModel : ObservableObject
{
    private readonly ClientSettings _settings;
    private readonly ILogApiClient _apiClient;
    private readonly AggregationService _aggregationService;
    private readonly SavedViewService _savedViewService;
    private readonly ExportService _exportService;
    private readonly EventCorrelationService _correlationService;
    private readonly TimeDisplayService _timeDisplayService;
    private readonly IAppLogger _logger;
    private readonly InMemoryAppLogger _inMemoryLogger;

    private bool _isBusy;
    private string _statusMessage = "Ready.";
    private string _connectionStatus = "Unknown";
    private string _lastRefreshText = "-";
    private string _lastResponseText = "-";
    private int _currentPage = 1;
    private int _pageSize = 100;
    private int _totalCount;
    private string _sortField = "occurredAt";
    private bool _sortAscending;
    private TimeDisplayMode _timeMode = TimeDisplayMode.Local;
    private LogEventRowViewModel? _selectedEvent;
    private SourceRecord? _selectedEventSource;
    private string _selectedSavedViewName = string.Empty;
    private string _savedViewEditName = string.Empty;
    private string _relatedEventsTitle = "No related events loaded.";

    private DateTime? _fromDateLocal;
    private DateTime? _toDateLocal;
    private string? _selectedQuickRange = "Last 24 hours";
    private string? _sourceKeyFilter;
    private string? _hostnameFilter;
    private string? _serviceFilter;
    private string? _severityFilter;
    private string? _categoryFilter;
    private string? _actionFilter;
    private string? _outcomeFilter;
    private string? _textSearchFilter;
    private string? _correlationFilter;
    private string? _traceFilter;
    private string? _requestFilter;
    private string? _componentFilter;
    private string? _actorUserFilter;
    private string? _actorPrincipalFilter;
    private string? _sessionFilter;
    private string? _clientIpFilter;
    private string? _serverIpFilter;
    private bool _onlyWithPayload;
    private bool _onlyWithCorrelation;
    private bool _onlyWithActor;

    public MainWindowViewModel(
        ClientSettings settings,
        ILogApiClient apiClient,
        AggregationService aggregationService,
        SavedViewService savedViewService,
        ExportService exportService,
        EventCorrelationService correlationService,
        TimeDisplayService timeDisplayService,
        IAppLogger logger,
        InMemoryAppLogger inMemoryLogger)
    {
        _settings = settings;
        _apiClient = apiClient;
        _aggregationService = aggregationService;
        _savedViewService = savedViewService;
        _exportService = exportService;
        _correlationService = correlationService;
        _timeDisplayService = timeDisplayService;
        _logger = logger;
        _inMemoryLogger = inMemoryLogger;

        _pageSize = Math.Max(10, settings.Api.DefaultPageSize);
        _timeMode = Enum.TryParse<TimeDisplayMode>(settings.Ui.DefaultTimeMode, true, out var mode)
            ? mode
            : TimeDisplayMode.Local;

        EventRows = new ObservableCollection<LogEventRowViewModel>();
        RelatedEventRows = new ObservableCollection<LogEventRowViewModel>();
        Sources = new ObservableCollection<SourceRecord>();
        SavedViews = new ObservableCollection<SavedViewDefinition>();
        ActiveFilterChips = new ObservableCollection<string>();
        SeverityAggregation = new ObservableCollection<CountBucket>();
        SourceAggregation = new ObservableCollection<CountBucket>();
        ServiceAggregation = new ObservableCollection<CountBucket>();
        CategoryAggregation = new ObservableCollection<CountBucket>();
        OutcomeAggregation = new ObservableCollection<CountBucket>();
        TimeAggregation = new ObservableCollection<TimeBucket>();
        ColumnOptions = new ObservableCollection<ColumnOptionViewModel>(BuildDefaultColumns());
        QuickRanges = new ObservableCollection<string>(new[]
        {
            "Last 5 minutes",
            "Last 15 minutes",
            "Last hour",
            "Today",
            "Last 24 hours",
            "Last 7 days"
        });

        LoadSavedViews();
    }

    public ObservableCollection<LogEventRowViewModel> EventRows { get; }
    public ObservableCollection<LogEventRowViewModel> RelatedEventRows { get; }
    public ObservableCollection<SourceRecord> Sources { get; }
    public ObservableCollection<SavedViewDefinition> SavedViews { get; }
    public ObservableCollection<string> ActiveFilterChips { get; }
    public ObservableCollection<CountBucket> SeverityAggregation { get; }
    public ObservableCollection<CountBucket> SourceAggregation { get; }
    public ObservableCollection<CountBucket> ServiceAggregation { get; }
    public ObservableCollection<CountBucket> CategoryAggregation { get; }
    public ObservableCollection<CountBucket> OutcomeAggregation { get; }
    public ObservableCollection<TimeBucket> TimeAggregation { get; }
    public ObservableCollection<ColumnOptionViewModel> ColumnOptions { get; }
    public ObservableCollection<string> QuickRanges { get; }

    public bool IsBusy
    {
        get => _isBusy;
        private set => SetProperty(ref _isBusy, value);
    }

    public string WindowTitle => "MustelaLog Desktop Client V1";
    public string StatusMessage { get => _statusMessage; set => SetProperty(ref _statusMessage, value); }
    public string ConnectionStatus { get => _connectionStatus; set => SetProperty(ref _connectionStatus, value); }
    public string LastRefreshText { get => _lastRefreshText; set => SetProperty(ref _lastRefreshText, value); }
    public string LastResponseText { get => _lastResponseText; set => SetProperty(ref _lastResponseText, value); }

    public int CurrentPage
    {
        get => _currentPage;
        set
        {
            if (SetProperty(ref _currentPage, value))
            {
                OnPropertyChanged(nameof(HasNextPage));
                OnPropertyChanged(nameof(PageSummary));
            }
        }
    }

    public int PageSize { get => _pageSize; set => SetProperty(ref _pageSize, value); }

    public int TotalCount
    {
        get => _totalCount;
        set
        {
            if (SetProperty(ref _totalCount, value))
            {
                OnPropertyChanged(nameof(HasNextPage));
                OnPropertyChanged(nameof(PageSummary));
            }
        }
    }

    public bool HasNextPage => CurrentPage * PageSize < TotalCount;
    public string PageSummary => $"Page {CurrentPage} · {EventRows.Count} rows loaded · total {TotalCount}";
    public string SortField { get => _sortField; private set => SetProperty(ref _sortField, value); }
    public bool SortAscending { get => _sortAscending; private set => SetProperty(ref _sortAscending, value); }

    public DateTime? FromDateLocal { get => _fromDateLocal; set => SetProperty(ref _fromDateLocal, value); }
    public DateTime? ToDateLocal { get => _toDateLocal; set => SetProperty(ref _toDateLocal, value); }
    public string? SelectedQuickRange { get => _selectedQuickRange; set => SetProperty(ref _selectedQuickRange, value); }
    public string? SourceKeyFilter { get => _sourceKeyFilter; set => SetProperty(ref _sourceKeyFilter, value); }
    public string? HostnameFilter { get => _hostnameFilter; set => SetProperty(ref _hostnameFilter, value); }
    public string? ServiceFilter { get => _serviceFilter; set => SetProperty(ref _serviceFilter, value); }
    public string? SeverityFilter { get => _severityFilter; set => SetProperty(ref _severityFilter, value); }
    public string? CategoryFilter { get => _categoryFilter; set => SetProperty(ref _categoryFilter, value); }
    public string? ActionFilter { get => _actionFilter; set => SetProperty(ref _actionFilter, value); }
    public string? OutcomeFilter { get => _outcomeFilter; set => SetProperty(ref _outcomeFilter, value); }
    public string? TextSearchFilter { get => _textSearchFilter; set => SetProperty(ref _textSearchFilter, value); }
    public string? CorrelationFilter { get => _correlationFilter; set => SetProperty(ref _correlationFilter, value); }
    public string? TraceFilter { get => _traceFilter; set => SetProperty(ref _traceFilter, value); }
    public string? RequestFilter { get => _requestFilter; set => SetProperty(ref _requestFilter, value); }
    public string? ComponentFilter { get => _componentFilter; set => SetProperty(ref _componentFilter, value); }
    public string? ActorUserFilter { get => _actorUserFilter; set => SetProperty(ref _actorUserFilter, value); }
    public string? ActorPrincipalFilter { get => _actorPrincipalFilter; set => SetProperty(ref _actorPrincipalFilter, value); }
    public string? SessionFilter { get => _sessionFilter; set => SetProperty(ref _sessionFilter, value); }
    public string? ClientIpFilter { get => _clientIpFilter; set => SetProperty(ref _clientIpFilter, value); }
    public string? ServerIpFilter { get => _serverIpFilter; set => SetProperty(ref _serverIpFilter, value); }
    public bool OnlyWithPayload { get => _onlyWithPayload; set => SetProperty(ref _onlyWithPayload, value); }
    public bool OnlyWithCorrelation { get => _onlyWithCorrelation; set => SetProperty(ref _onlyWithCorrelation, value); }
    public bool OnlyWithActor { get => _onlyWithActor; set => SetProperty(ref _onlyWithActor, value); }

    public TimeDisplayMode TimeMode
    {
        get => _timeMode;
        set
        {
            if (SetProperty(ref _timeMode, value))
            {
                foreach (var item in EventRows)
                {
                    item.SetTimeMode(_timeMode);
                }

                foreach (var item in RelatedEventRows)
                {
                    item.SetTimeMode(_timeMode);
                }

                OnPropertyChanged(nameof(TimeModeLabel));
                OnPropertyChanged(nameof(SelectedEventPrettyAttributes));
                OnPropertyChanged(nameof(SelectedEventPrettyPayload));
                OnPropertyChanged(nameof(SelectedSourceVersion));
            }
        }
    }

    public string TimeModeLabel => TimeMode == TimeDisplayMode.Utc ? "UTC" : "Local";

    public LogEventRowViewModel? SelectedEvent
    {
        get => _selectedEvent;
        set
        {
            if (SetProperty(ref _selectedEvent, value))
            {
                SelectedEventSource = ResolveSourceForSelectedEvent();
                OnPropertyChanged(nameof(SelectedEventPrettyAttributes));
                OnPropertyChanged(nameof(SelectedEventPrettyPayload));
            }
        }
    }

    public SourceRecord? SelectedEventSource
    {
        get => _selectedEventSource;
        private set
        {
            if (SetProperty(ref _selectedEventSource, value))
            {
                OnPropertyChanged(nameof(SelectedSourceVersion));
            }
        }
    }

    public string SelectedSourceVersion => SelectedEventSource?.VersionText ?? string.Empty;
    public string SelectedEventPrettyAttributes => SelectedEvent?.Record.GetPrettyAttributesJson() ?? string.Empty;
    public string SelectedEventPrettyPayload => SelectedEvent?.Record.GetPrettyRawPayloadJson() ?? string.Empty;
    public string SelectedSavedViewName { get => _selectedSavedViewName; set => SetProperty(ref _selectedSavedViewName, value); }
    public string SavedViewEditName { get => _savedViewEditName; set => SetProperty(ref _savedViewEditName, value); }
    public string RelatedEventsTitle { get => _relatedEventsTitle; set => SetProperty(ref _relatedEventsTitle, value); }

    public InMemoryAppLogger InMemoryLogger => _inMemoryLogger;
    public ILogApiClient ApiClient => _apiClient;
    public IAppLogger Logger => _logger;
    public IReadOnlyList<SourceRecord> SourceSnapshot => Sources.ToList();

    public async Task InitializeAsync()
    {
        ApplySelectedQuickRange();
        await RefreshAsync();
    }

    public async Task RefreshAsync()
    {
        if (IsBusy)
        {
            return;
        }

        IsBusy = true;
        StatusMessage = "Loading events...";
        var started = DateTimeOffset.UtcNow;

        try
        {
            if (Sources.Count == 0)
            {
                await LoadSourcesAsync();
            }

            var filter = BuildFilter();
            UpdateFilterChips(filter);

            var result = await _apiClient.GetEventsAsync(filter, CurrentPage, PageSize, SortField, SortAscending, CancellationToken.None);
            var refinedItems = ApplyClientSideRefinements(result.Items, filter);

            EventRows.Clear();
            foreach (var item in refinedItems)
            {
                EventRows.Add(new LogEventRowViewModel(item, _timeDisplayService, TimeMode));
            }

            TotalCount = result.Total;
            BuildAggregations(refinedItems);
            ConnectionStatus = "Connected";
            StatusMessage = $"Loaded {EventRows.Count} events.";
            LastRefreshText = DateTimeOffset.Now.ToString("yyyy-MM-dd HH:mm:ss");
            LastResponseText = $"{(DateTimeOffset.UtcNow - started).TotalMilliseconds:N0} ms";
        }
        catch (Exception exception)
        {
            ConnectionStatus = "Error";
            StatusMessage = "Loading events failed.";
            _logger.Error("Refreshing events failed", exception);
        }
        finally
        {
            IsBusy = false;
        }
    }

    public async Task LoadPreviousPageAsync()
    {
        if (CurrentPage <= 1)
        {
            return;
        }

        CurrentPage--;
        await RefreshAsync();
    }

    public async Task LoadNextPageAsync()
    {
        if (!HasNextPage)
        {
            return;
        }

        CurrentPage++;
        await RefreshAsync();
    }

    public void ClearFilters()
    {
        FromDateLocal = null;
        ToDateLocal = null;
        SourceKeyFilter = null;
        HostnameFilter = null;
        ServiceFilter = null;
        SeverityFilter = null;
        CategoryFilter = null;
        ActionFilter = null;
        OutcomeFilter = null;
        TextSearchFilter = null;
        CorrelationFilter = null;
        TraceFilter = null;
        RequestFilter = null;
        ComponentFilter = null;
        ActorUserFilter = null;
        ActorPrincipalFilter = null;
        SessionFilter = null;
        ClientIpFilter = null;
        ServerIpFilter = null;
        OnlyWithPayload = false;
        OnlyWithCorrelation = false;
        OnlyWithActor = false;
        SelectedQuickRange = null;
        ActiveFilterChips.Clear();
    }

    public void ApplySelectedQuickRange()
    {
        var now = DateTime.Now;
        switch (SelectedQuickRange)
        {
            case "Last 5 minutes":
                FromDateLocal = now.AddMinutes(-5);
                ToDateLocal = now;
                break;
            case "Last 15 minutes":
                FromDateLocal = now.AddMinutes(-15);
                ToDateLocal = now;
                break;
            case "Last hour":
                FromDateLocal = now.AddHours(-1);
                ToDateLocal = now;
                break;
            case "Today":
                FromDateLocal = DateTime.Today;
                ToDateLocal = now;
                break;
            case "Last 24 hours":
                FromDateLocal = now.AddHours(-24);
                ToDateLocal = now;
                break;
            case "Last 7 days":
                FromDateLocal = now.AddDays(-7);
                ToDateLocal = now;
                break;
        }
    }

    public void SetSort(string sortField, bool ascending)
    {
        SortField = sortField;
        SortAscending = ascending;
    }

    public async Task ExportCsvAsync(string filePath)
    {
        await _exportService.ExportCsvAsync(filePath, EventRows.Select(e => e.Record));
        _logger.Information("CSV export completed", new Dictionary<string, object?> { ["filePath"] = filePath, ["count"] = EventRows.Count });
    }

    public async Task ExportJsonAsync(string filePath)
    {
        await _exportService.ExportJsonAsync(filePath, EventRows.Select(e => e.Record));
        _logger.Information("JSON export completed", new Dictionary<string, object?> { ["filePath"] = filePath, ["count"] = EventRows.Count });
    }

    public async Task LoadRelatedEventsAsync(string mode)
    {
        if (SelectedEvent is null)
        {
            return;
        }

        try
        {
            RelatedEventRows.Clear();
            IReadOnlyList<LogEventRecord> items;
            var record = SelectedEvent.Record;

            switch (mode.ToLowerInvariant())
            {
                case "correlation" when !string.IsNullOrWhiteSpace(record.CorrelationId):
                    items = await LoadRelatedViaServerAsync(new LogQueryFilter { CorrelationId = record.CorrelationId });
                    RelatedEventsTitle = $"Related by Correlation ID: {record.CorrelationId}";
                    break;
                case "trace" when !string.IsNullOrWhiteSpace(record.TraceId):
                    items = await LoadRelatedViaServerAsync(new LogQueryFilter { TraceId = record.TraceId });
                    RelatedEventsTitle = $"Related by Trace ID: {record.TraceId}";
                    break;
                case "request":
                    items = _correlationService.FindLocally(EventRows.Select(e => e.Record), record, "request");
                    RelatedEventsTitle = $"Related by Request ID (local V1 fallback): {record.RequestCorrelationId}";
                    break;
                case "source" when !string.IsNullOrWhiteSpace(record.SourceKey):
                    items = await LoadRelatedViaServerAsync(new LogQueryFilter { SourceKey = record.SourceKey });
                    RelatedEventsTitle = $"Related by Source: {record.SourceKey}";
                    break;
                case "session":
                    items = _correlationService.FindLocally(EventRows.Select(e => e.Record), record, "session");
                    RelatedEventsTitle = "Related by Session Hash (local V1 fallback)";
                    break;
                case "actor":
                    items = _correlationService.FindLocally(EventRows.Select(e => e.Record), record, "actor");
                    RelatedEventsTitle = "Related by Actor (local V1 fallback)";
                    break;
                default:
                    items = Array.Empty<LogEventRecord>();
                    RelatedEventsTitle = "No related criteria available for the selected event.";
                    break;
            }

            foreach (var item in items)
            {
                RelatedEventRows.Add(new LogEventRowViewModel(item, _timeDisplayService, TimeMode));
            }
        }
        catch (Exception exception)
        {
            RelatedEventsTitle = "Loading related events failed.";
            _logger.Error("Loading related events failed", exception);
        }
    }

    public void SaveCurrentView()
    {
        if (string.IsNullOrWhiteSpace(SavedViewEditName))
        {
            StatusMessage = "Please enter a saved view name.";
            return;
        }

        var definition = new SavedViewDefinition
        {
            Name = SavedViewEditName.Trim(),
            Filter = BuildFilter(),
            SortField = SortField,
            SortAscending = SortAscending,
            VisibleColumns = ColumnOptions.Where(c => c.IsVisible).Select(c => c.Key).ToList()
        };

        _savedViewService.Save(definition);
        LoadSavedViews();
        SelectedSavedViewName = definition.Name;
        StatusMessage = $"Saved view '{definition.Name}' stored.";
    }

    public void LoadSelectedView()
    {
        var selected = SavedViews.FirstOrDefault(v => string.Equals(v.Name, SelectedSavedViewName, StringComparison.OrdinalIgnoreCase));
        if (selected is null)
        {
            StatusMessage = "Select a saved view first.";
            return;
        }

        ApplyFilter(selected.Filter);
        SortField = selected.SortField;
        SortAscending = selected.SortAscending;
        SavedViewEditName = selected.Name;
        ApplyVisibleColumns(selected.VisibleColumns);
        StatusMessage = $"Loaded saved view '{selected.Name}'.";
    }

    public void DeleteSelectedView()
    {
        if (string.IsNullOrWhiteSpace(SelectedSavedViewName))
        {
            StatusMessage = "Select a saved view first.";
            return;
        }

        _savedViewService.Delete(SelectedSavedViewName);
        LoadSavedViews();
        StatusMessage = "Saved view deleted.";
    }

    public void RenameSelectedView()
    {
        if (string.IsNullOrWhiteSpace(SelectedSavedViewName) || string.IsNullOrWhiteSpace(SavedViewEditName))
        {
            StatusMessage = "Select a saved view and enter a new name.";
            return;
        }

        _savedViewService.Rename(SelectedSavedViewName, SavedViewEditName.Trim());
        LoadSavedViews();
        SelectedSavedViewName = SavedViewEditName.Trim();
        StatusMessage = "Saved view renamed.";
    }

    private async Task LoadSourcesAsync()
    {
        var sources = await _apiClient.GetSourcesAsync();
        Sources.Clear();
        foreach (var source in sources)
        {
            Sources.Add(source);
        }
    }

    private LogQueryFilter BuildFilter()
    {
        return new LogQueryFilter
        {
            FromUtc = _timeDisplayService.ToUtc(FromDateLocal),
            ToUtc = _timeDisplayService.ToUtc(ToDateLocal),
            SourceKey = NullIfWhite(SourceKeyFilter),
            Hostname = NullIfWhite(HostnameFilter),
            Service = NullIfWhite(ServiceFilter),
            SeverityText = NullIfWhite(SeverityFilter),
            EventCategory = NullIfWhite(CategoryFilter),
            EventAction = NullIfWhite(ActionFilter),
            EventOutcome = NullIfWhite(OutcomeFilter),
            TextSearch = NullIfWhite(TextSearchFilter),
            CorrelationId = NullIfWhite(CorrelationFilter),
            TraceId = NullIfWhite(TraceFilter),
            RequestId = NullIfWhite(RequestFilter),
            Component = NullIfWhite(ComponentFilter),
            ActorUserId = NullIfWhite(ActorUserFilter),
            ActorPrincipal = NullIfWhite(ActorPrincipalFilter),
            SessionHash = NullIfWhite(SessionFilter),
            ClientIp = NullIfWhite(ClientIpFilter),
            ServerIp = NullIfWhite(ServerIpFilter),
            OnlyWithPayload = OnlyWithPayload,
            OnlyWithCorrelation = OnlyWithCorrelation,
            OnlyWithActor = OnlyWithActor
        };
    }

    private void UpdateFilterChips(LogQueryFilter filter)
    {
        ActiveFilterChips.Clear();
        foreach (var chip in filter.ToChips())
        {
            ActiveFilterChips.Add(chip);
        }
    }

    private IReadOnlyList<LogEventRecord> ApplyClientSideRefinements(IEnumerable<LogEventRecord> items, LogQueryFilter filter)
    {
        IEnumerable<LogEventRecord> query = items;

        if (!string.IsNullOrWhiteSpace(filter.Hostname))
            query = query.Where(e => Contains(e.HostName, filter.Hostname));
        if (!string.IsNullOrWhiteSpace(filter.Service))
            query = query.Where(e => Contains(e.ServiceName, filter.Service));
        if (!string.IsNullOrWhiteSpace(filter.EventCategory))
            query = query.Where(e => Contains(e.EventCategory, filter.EventCategory));
        if (!string.IsNullOrWhiteSpace(filter.EventAction))
            query = query.Where(e => Contains(e.EventAction, filter.EventAction));
        if (!string.IsNullOrWhiteSpace(filter.EventOutcome))
            query = query.Where(e => Contains(e.EventOutcome, filter.EventOutcome));
        if (!string.IsNullOrWhiteSpace(filter.TextSearch))
            query = query.Where(e => Contains(e.MessageText, filter.TextSearch) || Contains(e.EventName, filter.TextSearch) || Contains(e.ActorPrincipal, filter.TextSearch) || Contains(e.AttributesJson, filter.TextSearch));
        if (!string.IsNullOrWhiteSpace(filter.Component))
            query = query.Where(e => Contains(e.ComponentName, filter.Component));
        if (!string.IsNullOrWhiteSpace(filter.ActorUserId))
            query = query.Where(e => Contains(e.ActorUserId, filter.ActorUserId));
        if (!string.IsNullOrWhiteSpace(filter.ActorPrincipal))
            query = query.Where(e => Contains(e.ActorPrincipal, filter.ActorPrincipal));
        if (!string.IsNullOrWhiteSpace(filter.SessionHash))
            query = query.Where(e => Contains(e.SessionHashSha256, filter.SessionHash));
        if (!string.IsNullOrWhiteSpace(filter.ClientIp))
            query = query.Where(e => Contains(e.ClientIp, filter.ClientIp));
        if (!string.IsNullOrWhiteSpace(filter.ServerIp))
            query = query.Where(e => Contains(e.ServerIp, filter.ServerIp));
        if (!string.IsNullOrWhiteSpace(filter.RequestId))
            query = query.Where(e => Contains(e.RequestCorrelationId, filter.RequestId));
        if (filter.OnlyWithPayload)
            query = query.Where(e => !string.IsNullOrWhiteSpace(e.RawPayloadJson));
        if (filter.OnlyWithCorrelation)
            query = query.Where(e => !string.IsNullOrWhiteSpace(e.CorrelationId) || !string.IsNullOrWhiteSpace(e.TraceId));
        if (filter.OnlyWithActor)
            query = query.Where(e => !string.IsNullOrWhiteSpace(e.ActorPrincipal) || !string.IsNullOrWhiteSpace(e.ActorUserId));

        return query.ToList();
    }

    private async Task<IReadOnlyList<LogEventRecord>> LoadRelatedViaServerAsync(LogQueryFilter filter)
    {
        var page = await _apiClient.GetEventsAsync(filter, 1, 50, "occurredAt", false);
        return page.Items;
    }

    private void BuildAggregations(IReadOnlyList<LogEventRecord> items)
    {
        ReplaceCollection(SeverityAggregation, _aggregationService.CountBySeverity(items).Take(10));
        ReplaceCollection(SourceAggregation, _aggregationService.CountBySourceOrHost(items).Take(10));
        ReplaceCollection(ServiceAggregation, _aggregationService.CountByService(items).Take(10));
        ReplaceCollection(CategoryAggregation, _aggregationService.CountByCategory(items).Take(10));
        ReplaceCollection(OutcomeAggregation, _aggregationService.CountByOutcome(items).Take(10));
        ReplaceCollection(TimeAggregation, _aggregationService.CountOverTime(items));
    }

    private void ReplaceCollection<T>(ObservableCollection<T> target, IEnumerable<T> source)
    {
        target.Clear();
        foreach (var item in source)
        {
            target.Add(item);
        }
    }

    private SourceRecord? ResolveSourceForSelectedEvent()
    {
        if (SelectedEvent?.Record.SourceId is null)
        {
            return null;
        }

        return Sources.FirstOrDefault(s => string.Equals(s.SourceId, SelectedEvent.Record.SourceId, StringComparison.OrdinalIgnoreCase))
            ?? Sources.FirstOrDefault(s => string.Equals(s.SourceKey, SelectedEvent.Record.SourceKey, StringComparison.OrdinalIgnoreCase));
    }

    private void LoadSavedViews()
    {
        SavedViews.Clear();
        foreach (var item in _savedViewService.LoadAll())
        {
            SavedViews.Add(item);
        }
    }

    private void ApplyFilter(LogQueryFilter filter)
    {
        FromDateLocal = filter.FromUtc?.ToLocalTime().DateTime;
        ToDateLocal = filter.ToUtc?.ToLocalTime().DateTime;
        SourceKeyFilter = filter.SourceKey;
        HostnameFilter = filter.Hostname;
        ServiceFilter = filter.Service;
        SeverityFilter = filter.SeverityText;
        CategoryFilter = filter.EventCategory;
        ActionFilter = filter.EventAction;
        OutcomeFilter = filter.EventOutcome;
        TextSearchFilter = filter.TextSearch;
        CorrelationFilter = filter.CorrelationId;
        TraceFilter = filter.TraceId;
        RequestFilter = filter.RequestId;
        ComponentFilter = filter.Component;
        ActorUserFilter = filter.ActorUserId;
        ActorPrincipalFilter = filter.ActorPrincipal;
        SessionFilter = filter.SessionHash;
        ClientIpFilter = filter.ClientIp;
        ServerIpFilter = filter.ServerIp;
        OnlyWithPayload = filter.OnlyWithPayload;
        OnlyWithCorrelation = filter.OnlyWithCorrelation;
        OnlyWithActor = filter.OnlyWithActor;
    }

    private void ApplyVisibleColumns(IEnumerable<string> visibleColumnKeys)
    {
        var set = new HashSet<string>(visibleColumnKeys, StringComparer.OrdinalIgnoreCase);
        foreach (var option in ColumnOptions)
        {
            option.IsVisible = set.Contains(option.Key);
        }
    }

    private static IEnumerable<ColumnOptionViewModel> BuildDefaultColumns()
    {
        return new[]
        {
            new ColumnOptionViewModel { Key = "occurredAt", DisplayName = "Occurred At", IsVisible = true },
            new ColumnOptionViewModel { Key = "ingestedAt", DisplayName = "Ingested At", IsVisible = true },
            new ColumnOptionViewModel { Key = "severity", DisplayName = "Severity", IsVisible = true },
            new ColumnOptionViewModel { Key = "sourceOrHost", DisplayName = "Source / Host", IsVisible = true },
            new ColumnOptionViewModel { Key = "service", DisplayName = "Service", IsVisible = true },
            new ColumnOptionViewModel { Key = "category", DisplayName = "Category", IsVisible = true },
            new ColumnOptionViewModel { Key = "action", DisplayName = "Action", IsVisible = true },
            new ColumnOptionViewModel { Key = "outcome", DisplayName = "Outcome", IsVisible = true },
            new ColumnOptionViewModel { Key = "message", DisplayName = "Message", IsVisible = true },
            new ColumnOptionViewModel { Key = "correlation", DisplayName = "Correlation / Trace", IsVisible = true },
            new ColumnOptionViewModel { Key = "eventId", DisplayName = "Event ID", IsVisible = false }
        };
    }

    private static bool Contains(string? haystack, string needle)
        => !string.IsNullOrWhiteSpace(haystack) && haystack.Contains(needle, StringComparison.OrdinalIgnoreCase);

    private static string? NullIfWhite(string? value) => string.IsNullOrWhiteSpace(value) ? null : value.Trim();
}
