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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mysnapgoals.app.ui.home.components.CalendarBanner
import com.mysnapgoals.app.ui.home.components.CalendarBannerState
import com.mysnapgoals.app.ui.home.components.CalendarBannerViewModel
import com.mysnapgoals.app.ui.home.components.FilterLine
import com.mysnapgoals.app.ui.home.components.PercentageLine
import com.mysnapgoals.app.ui.home.components.SnapGoalsTopBar
import com.mysnapgoals.app.ui.home.components.TodayItemType
import com.mysnapgoals.app.ui.home.state.HomeEvent
import com.mysnapgoals.app.ui.home.state.HomeState
import com.mysnapgoals.app.ui.home.state.TaskFilterType
import com.mysnapgoals.app.ui.home.state.TaskSort
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val statsViewModel: HomeStatsViewModel = hiltViewModel()
    val statsState by statsViewModel.state.collectAsState()

    val calendarViewModel: CalendarBannerViewModel = hiltViewModel()
    val calendarState by calendarViewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var showAddTodo by rememberSaveable { mutableStateOf(false) }
    var showAddGoal by rememberSaveable { mutableStateOf(false) }

    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
        snackbarHostState = snackbarHostState,
        scrollBehavior = scrollBehavior,
        showAddTodo = showAddTodo,
        showAddGoal = showAddGoal,
        showFilterSheet = showFilterSheet,
        onShowAddTodo = { showAddTodo = true },
        onShowAddGoal = { showAddGoal = true },
        onDismissAddTodo = { showAddTodo = false },
        onDismissAddGoal = { showAddGoal = false },
        onDismissFilterSheet = { showFilterSheet = false },
        onAddTodo = viewModel::addTodo,
        onAddGoal = viewModel::addGoal,
        onApplyFilters = viewModel::applyFilters,
        onClearFilters = { viewModel.applyFilters(TaskFilterType.ALL, TaskSort.RECENT, false) },
        onQueryChanged = viewModel::onQueryChanged,
        onToggleDone = viewModel::onToggleDone,
        onIncrementGoal = viewModel::onIncrementGoal,
        onDecrementGoal = viewModel::onDecrementGoal,
        onUncomplete = viewModel::onUncomplete,
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
    snackbarHostState: SnackbarHostState,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    showAddTodo: Boolean,
    showAddGoal: Boolean,
    showFilterSheet: Boolean,
    onShowAddTodo: () -> Unit,
    onShowAddGoal: () -> Unit,
    onDismissAddTodo: () -> Unit,
    onDismissAddGoal: () -> Unit,
    onDismissFilterSheet: () -> Unit,
    onAddTodo: (String) -> Unit,
    onAddGoal: (String, Int) -> Unit,
    onApplyFilters: (TaskFilterType, TaskSort, Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onToggleDone: (String) -> Unit,
    onIncrementGoal: (String) -> Unit,
    onDecrementGoal: (String) -> Unit,
    onUncomplete: (String) -> Unit,
    onShowFilters: () -> Unit
) {
    if (showAddTodo) {
        AddTodoComponent(
            onDismiss = onDismissAddTodo,
            onConfirm = { title ->
                onAddTodo(title)
                onDismissAddTodo()
            }
        )
    }

    if (showAddGoal) {
        AddGoalComponent(
            onDismiss = onDismissAddGoal,
            onConfirm = { title, target ->
                onAddGoal(title, target)
                onDismissAddGoal()
            }
        )
    }

    if (showFilterSheet) {
        FilterSheet(
            initialFilterType = state.filterType,
            initialSort = state.sort,
            initialDoneOnly = state.doneOnly,
            onApply = onApplyFilters,
            onClear = onClearFilters,
            onDismiss = onDismissFilterSheet
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { SnapGoalsTopBar(scrollBehavior) },
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
                    modifier = Modifier.padding(top = 6.dp)
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
                    onUncomplete = onUncomplete
                )
            }
            item {
                PercentageLine(
                    dayPercent = statsState.dayPercent,
                    weekPercent = statsState.weekPercent,
                    monthPercent = statsState.monthPercent,
                    yearPercent = statsState.yearPercent
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
                    items = state.totalItems,
                    onToggleDone = onToggleDone,
                    onIncrementGoal = onIncrementGoal,
                    onDecrementGoal = onDecrementGoal,
                    onUncomplete = onUncomplete
                )
            }
        }
    }
}

