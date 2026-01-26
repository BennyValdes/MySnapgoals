package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.mysnapgoals.app.ui.components.CalendarBanner
import com.mysnapgoals.app.ui.components.FilterLine
import com.mysnapgoals.app.ui.components.PercentageLine
import com.mysnapgoals.app.ui.components.SnapGoalsTopBar
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = mavericksViewModel()
    val state by viewModel.collectAsState()

    val statsViewModel: HomeStatsViewModel = mavericksViewModel()
    val statsState by statsViewModel.collectAsState()

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

                    val result =
                        snackbarHostState.showSnackbar(
                            message = "ToDo removido de Hoy",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Indefinite
                        )

                    dismissJob.cancel()

                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoRemoveTodo(event.todo.id)
                    }
                }
            }
        }
    }

    if (showAddTodo) {
        AddTodoComponent(
            onDismiss = { showAddTodo = false },
            onConfirm = { title ->
                viewModel.addTodo(title)
                showAddTodo = false
            }
        )
    }

    if (showAddGoal) {
        AddGoalComponent(
            onDismiss = { showAddGoal = false },
            onConfirm = { title, target ->
                viewModel.addGoal(title, target)
                showAddGoal = false
            }
        )
    }

    if (showFilterSheet) {
        FilterSheet(
            initialFilterType = state.filterType,
            initialSort = state.sort,
            onApply = { type, sort -> viewModel.applyFilters(type, sort) },
            onClear = { viewModel.applyFilters(TaskFilterType.ALL, TaskSort.RECENT) },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { SnapGoalsTopBar(scrollBehavior) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                CalendarBanner()
            }
            item {
                AddLine(
                    onAddGoal = { showAddGoal = true },
                    onAddTodo = { showAddTodo = true }
                )
            }
            item {
                TodayLine(
                    items = state.todayItems,
                    onToggleDone = viewModel::onToggleDone,
                    onIncrementGoal = viewModel::onIncrementGoal
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
                    onQueryChanged = viewModel::onQueryChanged,
                    onTrailingActionClick = {
                        if (state.query.isBlank()) {
                            showFilterSheet = true
                        } else {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                )
            }
            item {
                TotalList(
                    items = state.totalItems,
                    onToggleDone = viewModel::onToggleDone,
                    onIncrementGoal = viewModel::onIncrementGoal
                )
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun HomeScreenPreview() {
    SnapGoalsTheme {
        HomeScreen()
    }
}