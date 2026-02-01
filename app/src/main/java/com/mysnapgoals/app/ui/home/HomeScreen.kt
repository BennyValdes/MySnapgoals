package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mysnapgoals.app.domain.model.GoalPeriodicity
import com.mysnapgoals.app.settings.ProfileAvatar
import com.mysnapgoals.app.settings.PomodoroSettings
import com.mysnapgoals.app.settings.SettingsRepository
import com.mysnapgoals.app.ui.home.components.CalendarBanner
import com.mysnapgoals.app.ui.home.components.CalendarBannerState
import com.mysnapgoals.app.ui.home.components.CalendarBannerViewModel
import com.mysnapgoals.app.ui.home.components.FilterLine
import com.mysnapgoals.app.ui.home.components.PercentageLine
import com.mysnapgoals.app.ui.home.components.SnapGoalsTopBar
import com.mysnapgoals.app.ui.home.components.TodayItemType
import com.mysnapgoals.app.ui.home.components.TodayItemUiModel
import com.mysnapgoals.app.ui.home.state.HomeEvent
import com.mysnapgoals.app.ui.home.state.HomeState
import com.mysnapgoals.app.ui.home.state.TaskFilterType
import com.mysnapgoals.app.ui.home.state.TaskSort
import com.mysnapgoals.app.ui.settings.SettingsScreen
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mysnapgoals.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val statsViewModel: HomeStatsViewModel = hiltViewModel()
    val statsState by statsViewModel.state.collectAsState()

    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val settings by settingsRepository.settingsFlow.collectAsState(initial = PomodoroSettings())

    val calendarViewModel: CalendarBannerViewModel = hiltViewModel()
    val calendarState by calendarViewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var showAddTodo by rememberSaveable { mutableStateOf(false) }
    var showAddGoal by rememberSaveable { mutableStateOf(false) }
    var showPomodoro by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var editingItemId by rememberSaveable { mutableStateOf<String?>(null) }

    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val avatarResId =
        when (settings.profileAvatar) {
            ProfileAvatar.MALE -> R.drawable.maleavatar
            ProfileAvatar.FEMALE -> R.drawable.femaleavatar
        }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
        when (event) {
                is HomeEvent.ShowUndoRemovedTodo -> {
                    snackbarHostState.currentSnackbarData?.dismiss()

                    val dismissJob = launch {
                        delay(3_000)
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }

                    val label = if (event.todo.type == TodayItemType.GOAL) {
                        "Objetivo removido de Hoy"
                    } else {
                        "ToDo removido de Hoy"
                    }

                    val result =
                        snackbarHostState.showSnackbar(
                            message = label,
                            actionLabel = "Deshacer",
                            duration = SnackbarDuration.Indefinite
                        )

                    dismissJob.cancel()

                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoRemoveTodo(event.todo.id)
                    }
                }
                is HomeEvent.ShowUndoUncompleteTodo -> {
                    snackbarHostState.currentSnackbarData?.dismiss()

                    val dismissJob = launch {
                        delay(3_000)
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }

                    val label = if (event.todo.type == TodayItemType.GOAL) {
                        "Objetivo reabierto"
                    } else {
                        "ToDo reabierto"
                    }

                    val result =
                        snackbarHostState.showSnackbar(
                            message = label,
                            actionLabel = "Deshacer",
                            duration = SnackbarDuration.Indefinite
                        )

                    dismissJob.cancel()

                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoUncomplete(event.todo.id)
                    }
                }
            }
        }
    }

    HomeContent(
        state = state,
        statsState = statsState,
        calendarState = calendarState,
        avatarResId = avatarResId,
        snackbarHostState = snackbarHostState,
        scrollBehavior = scrollBehavior,
        showAddTodo = showAddTodo,
        showAddGoal = showAddGoal,
        showPomodoro = showPomodoro,
        showSettings = showSettings,
        showFilterSheet = showFilterSheet,
        editingItemId = editingItemId,
        onShowAddTodo = { showAddTodo = true },
        onShowAddGoal = { showAddGoal = true },
        onShowPomodoro = { showPomodoro = true },
        onShowSettings = { showSettings = true },
        onDismissAddTodo = { showAddTodo = false },
        onDismissAddGoal = { showAddGoal = false },
        onDismissPomodoro = { showPomodoro = false },
        onDismissSettings = { showSettings = false },
        onDismissFilterSheet = { showFilterSheet = false },
        onAddTodo = viewModel::addTodo,
        onAddGoal = viewModel::addGoal,
        onUpdateTodo = viewModel::updateTodo,
        onUpdateGoal = viewModel::updateGoal,
        onDismissEdit = { editingItemId = null },
        onApplyFilters = viewModel::applyFilters,
        onClearFilters = { viewModel.applyFilters(TaskFilterType.ALL, TaskSort.RECENT, false) },
        onQueryChanged = viewModel::onQueryChanged,
        onToggleDone = viewModel::onToggleDone,
        onIncrementGoal = viewModel::onIncrementGoal,
        onDecrementGoal = viewModel::onDecrementGoal,
        onUncomplete = viewModel::onUncomplete,
        onEditItem = { editingItemId = it },
        onShowFilters = {
            focusManager.clearFocus()
            keyboardController?.hide()
            showFilterSheet = true
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    statsState: HomeStatsState,
    calendarState: CalendarBannerState,
    avatarResId: Int,
    snackbarHostState: SnackbarHostState,
    scrollBehavior: TopAppBarScrollBehavior,
    showAddTodo: Boolean,
    showAddGoal: Boolean,
    showPomodoro: Boolean,
    showSettings: Boolean,
    showFilterSheet: Boolean,
    editingItemId: String?,
    onShowAddTodo: () -> Unit,
    onShowAddGoal: () -> Unit,
    onShowPomodoro: () -> Unit,
    onShowSettings: () -> Unit,
    onDismissAddTodo: () -> Unit,
    onDismissAddGoal: () -> Unit,
    onDismissPomodoro: () -> Unit,
    onDismissSettings: () -> Unit,
    onDismissFilterSheet: () -> Unit,
    onAddTodo: (String, Long) -> Unit,
    onAddGoal: (String, GoalPeriodicity, Long) -> Unit,
    onUpdateTodo: (String, String, Long) -> Unit,
    onUpdateGoal: (String, String, GoalPeriodicity, Long) -> Unit,
    onDismissEdit: () -> Unit,
    onApplyFilters: (TaskFilterType, TaskSort, Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onToggleDone: (String) -> Unit,
    onIncrementGoal: (String) -> Unit,
    onDecrementGoal: (String) -> Unit,
    onUncomplete: (String) -> Unit,
    onEditItem: (String) -> Unit,
    onShowFilters: () -> Unit
) {
    if (showSettings) {
        SettingsScreen(onClose = onDismissSettings)
        return
    }

    if (showPomodoro) {
        PomodoroScreen(onClose = onDismissPomodoro)
        return
    }

    if (showAddTodo) {
        AddTodoComponent(
            onDismiss = onDismissAddTodo,
            onConfirm = { title, scheduledDay ->
                onAddTodo(title, scheduledDay)
                onDismissAddTodo()
            }
        )
    }

    if (showAddGoal) {
        AddGoalComponent(
            onDismiss = onDismissAddGoal,
            onConfirm = { title, periodicity, dueDay ->
                onAddGoal(title, periodicity, dueDay)
                onDismissAddGoal()
            }
        )
    }

    val editingItem = state.allItems.firstOrNull { it.id == editingItemId }
    if (editingItem != null) {
        if (editingItem.type == TodayItemType.TODO) {
            EditTodoComponent(
                initialTitle = editingItem.title,
                initialScheduledDay = editingItem.scheduledDay,
                onDismiss = onDismissEdit,
                onConfirm = { title, scheduledDay ->
                    onUpdateTodo(editingItem.id, title, scheduledDay)
                    onDismissEdit()
                }
            )
        } else {
            EditGoalComponent(
                initialTitle = editingItem.title,
                initialPeriodicity = editingItem.periodicity ?: GoalPeriodicity.MONTHLY,
                initialDueDay = editingItem.dueDay,
                onDismiss = onDismissEdit,
                onConfirm = { title, periodicity, dueDay ->
                    onUpdateGoal(editingItem.id, title, periodicity, dueDay)
                    onDismissEdit()
                }
            )
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            initialFilterType = state.filterType,
            initialSort = state.sortOrder,
            initialDoneOnly = state.showDoneOnly,
            onApply = onApplyFilters,
            onClear = onClearFilters,
            onDismiss = onDismissFilterSheet
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { SnapGoalsTopBar(scrollBehavior, avatarResId = avatarResId, onMenuClick = onShowSettings) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 5.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                CalendarBanner(
                    timeText = calendarState.timeText,
                    dayOfWeekText = calendarState.dayOfWeekText,
                    dateText = calendarState.dateText,
                    modifier = Modifier.padding(top = 6.dp),
                    onPomodoroClick = onShowPomodoro
                )
            }
            item {
                AddLine(
                    onAddGoal = onShowAddGoal,
                    onAddTodo = onShowAddTodo
                )
            }
            item {
                TodayLine(
                    items = state.todayItems,
                    onToggleDone = onToggleDone,
                    onIncrementGoal = onIncrementGoal,
                    onDecrementGoal = onDecrementGoal,
                    onUncomplete = onUncomplete,
                    onItemClick = onEditItem
                )
            }
            item {
            PercentageLine(
                dayCompleted = statsState.dayCompleted,
                dayPending = statsState.dayPending,
                dayOverdue = statsState.dayOverdue,
                dayCompletedCount = statsState.dayCompletedCount,
                dayPendingCount = statsState.dayPendingCount,
                dayOverdueCount = statsState.dayOverdueCount,
                weekCompleted = statsState.weekCompleted,
                weekPending = statsState.weekPending,
                weekOverdue = statsState.weekOverdue,
                weekCompletedCount = statsState.weekCompletedCount,
                weekPendingCount = statsState.weekPendingCount,
                weekOverdueCount = statsState.weekOverdueCount,
                monthCompleted = statsState.monthCompleted,
                monthPending = statsState.monthPending,
                monthOverdue = statsState.monthOverdue,
                monthCompletedCount = statsState.monthCompletedCount,
                monthPendingCount = statsState.monthPendingCount,
                monthOverdueCount = statsState.monthOverdueCount,
                yearCompleted = statsState.yearCompleted,
                yearPending = statsState.yearPending,
                yearOverdue = statsState.yearOverdue,
                yearCompletedCount = statsState.yearCompletedCount,
                yearPendingCount = statsState.yearPendingCount,
                yearOverdueCount = statsState.yearOverdueCount
            )
            }
            item {
                FilterLine(
                    query = state.query,
                    onQueryChanged = onQueryChanged,
                    onTrailingActionClick = onShowFilters
                )
            }
            item {
                TotalList(
                    items = state.filteredItems,
                    onToggleDone = onToggleDone,
                    onIncrementGoal = onIncrementGoal,
                    onDecrementGoal = onDecrementGoal,
                    onUncomplete = onUncomplete,
                    onItemClick = onEditItem
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showSystemUi = true)
fun HomeContentPreview() {
    SnapGoalsTheme {
        HomeContent(
            state = HomeState(
                todayItems = listOf(
                    TodayItemUiModel(
                        id = "1",
                        type = TodayItemType.TODO,
                        title = "Leer 10 paginas",
                        isDone = false
                    ),
                    TodayItemUiModel(
                        id = "2",
                        type = TodayItemType.GOAL,
                        title = "Meditacion",
                        isDone = false,
                        current = 2,
                        target = 5
                    )
                ),
                allItems = emptyList(),
                filteredItems = listOf(
                    TodayItemUiModel(
                        id = "3",
                        type = TodayItemType.TODO,
                        title = "Caminar 20 min",
                        isDone = true
                    )
                ),
                query = "",
                filterType = TaskFilterType.ALL,
                sortOrder = TaskSort.RECENT,
                showDoneOnly = false
            ),
            statsState = HomeStatsState(
                dayCompleted = 40,
                dayPending = 30,
                dayOverdue = 30,
                dayCompletedCount = 2,
                dayPendingCount = 1,
                dayOverdueCount = 1,
                weekCompleted = 20,
                weekPending = 50,
                weekOverdue = 30,
                weekCompletedCount = 3,
                weekPendingCount = 6,
                weekOverdueCount = 2,
                monthCompleted = 10,
                monthPending = 40,
                monthOverdue = 50,
                monthCompletedCount = 4,
                monthPendingCount = 12,
                monthOverdueCount = 8,
                yearCompleted = 70,
                yearPending = 20,
                yearOverdue = 10,
                yearCompletedCount = 120,
                yearPendingCount = 30,
                yearOverdueCount = 10
            ),
            calendarState = CalendarBannerState(
                timeText = "09:21",
                dayOfWeekText = "Lunes",
                dateText = "2026/01/31"
            ),
            avatarResId = R.drawable.maleavatar,
            snackbarHostState = SnackbarHostState(),
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
            showAddTodo = false,
            showAddGoal = false,
            showPomodoro = false,
            showSettings = false,
            showFilterSheet = false,
            editingItemId = null,
            onShowAddTodo = {},
            onShowAddGoal = {},
            onShowPomodoro = {},
            onShowSettings = {},
            onDismissAddTodo = {},
            onDismissAddGoal = {},
            onDismissPomodoro = {},
            onDismissSettings = {},
            onDismissFilterSheet = {},
            onAddTodo = { _, _ -> },
            onAddGoal = { _, _, _ -> },
            onUpdateTodo = { _, _, _ -> },
            onUpdateGoal = { _, _, _, _ -> },
            onDismissEdit = {},
            onApplyFilters = { _, _, _ -> },
            onClearFilters = {},
            onQueryChanged = {},
            onToggleDone = {},
            onIncrementGoal = {},
            onDecrementGoal = {},
            onUncomplete = {},
            onEditItem = {},
            onShowFilters = {}
        )
    }
}
