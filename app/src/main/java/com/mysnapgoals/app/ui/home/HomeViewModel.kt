package com.mysnapgoals.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mysnapgoals.app.domain.model.GoalPeriodicity
import com.mysnapgoals.app.domain.model.GoalProgressEvent
import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType
import com.mysnapgoals.app.domain.usecase.AddGoalUseCase
import com.mysnapgoals.app.domain.usecase.AddTodoUseCase
import com.mysnapgoals.app.domain.usecase.InsertGoalProgressEventUseCase
import com.mysnapgoals.app.domain.usecase.ObserveTasksUseCase
import com.mysnapgoals.app.domain.usecase.SetDoneUseCase
import com.mysnapgoals.app.domain.usecase.SumGoalProgressForGoalUseCase
import com.mysnapgoals.app.domain.usecase.UpdateTaskUseCase
import com.mysnapgoals.app.domain.usecase.ObserveGoalProgressEventsUseCase
import com.mysnapgoals.app.ui.home.components.TodayItemType
import com.mysnapgoals.app.ui.home.components.TodayItemUiModel
import com.mysnapgoals.app.ui.home.mapper.toUiModel
import com.mysnapgoals.app.ui.home.state.HomeEvent
import com.mysnapgoals.app.ui.home.state.HomeState
import com.mysnapgoals.app.ui.home.state.TaskFilterType
import com.mysnapgoals.app.ui.home.state.TaskSort
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val addTodoUseCase: AddTodoUseCase,
    private val addGoalUseCase: AddGoalUseCase,
    private val setDoneUseCase: SetDoneUseCase,
    private val insertGoalProgressEventUseCase: InsertGoalProgressEventUseCase,
    private val sumGoalProgressForGoalUseCase: SumGoalProgressForGoalUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val observeGoalProgressEventsUseCase: ObserveGoalProgressEventsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private val _events = Channel<HomeEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val dayTick = MutableStateFlow(todayEpochDay())

    private val removedStack = ArrayDeque<TodayItemUiModel>()
    private var confirmTopJob: Job? = null
    private val defaultGoalPeriodicity = GoalPeriodicity.MONTHLY
    private var taskById: Map<String, Task> = emptyMap()

    init {
        viewModelScope.launch {
            while (true) {
                val now = java.time.ZonedDateTime.now()
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
                val delayMs = java.time.Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0)
                delay(delayMs)
                dayTick.value = todayEpochDay()
            }
        }

        viewModelScope.launch {
            combine(
                observeTasks(),
                observeGoalProgressEventsUseCase(),
                dayTick
            ) { tasks, _, _ -> tasks }
                .collect { entities ->
                val today = todayEpochDay()
                val goalProgressToday = mutableMapOf<String, Int>()
                val goalProgressCurrent = mutableMapOf<String, Int>()
                val goalTargetCurrent = mutableMapOf<String, Int>()
                taskById = entities.associateBy { it.id }

                for (goal in entities.filter { it.type == TaskType.GOAL }) {
                    val periodicity = goal.periodicity ?: defaultGoalPeriodicity
                    val (periodStart, periodEnd) = periodRangeForToday(periodicity, today)
                    val progressCurrent = sumGoalProgressForGoalUseCase(goal.id, periodStart, periodEnd)
                    val progressToday = sumGoalProgressForGoalUseCase(goal.id, today, today)
                    val target = periodTarget(periodicity, today)
                    goalProgressCurrent[goal.id] = progressCurrent
                    goalProgressToday[goal.id] = progressToday
                    goalTargetCurrent[goal.id] = target
                }

                _state.update { current ->
                    val todaySource =
                        entities.asSequence()
                            .filter { !it.isDone }
                            .filter { (it.scheduledDay ?: today) == today }
                            .map { task ->
                                val base = task.toUiModel()
                                if (task.type == TaskType.GOAL) {
                                    val currentValue = goalProgressCurrent[task.id] ?: 0
                                    val target = goalTargetCurrent[task.id] ?: 0
                                    base.copy(current = currentValue, target = target)
                                } else {
                                    base
                                }
                            }
                            .filterNot { item ->
                                if (item.type != TodayItemType.GOAL) return@filterNot false
                                val progressToday = goalProgressToday[item.id] ?: 0
                                val target = goalTargetCurrent[item.id] ?: 0
                                val progressCurrent = goalProgressCurrent[item.id] ?: 0
                                progressToday > 0 || (target > 0 && progressCurrent >= target)
                            }
                            .filterNot { it.id in current.hiddenIds } // respeta undo overlay
                            .toList()

                        val totalSource =
                            entities.asSequence()
                            // si quieres incluir tambien scheduledDay==null o cualquier dia, aqui es donde se define
                            .map { task ->
                                val base = task.toUiModel()
                                if (task.type == TaskType.GOAL) {
                                    val currentValue = goalProgressCurrent[task.id] ?: 0
                                    val target = goalTargetCurrent[task.id] ?: 0
                                    base.copy(current = currentValue, target = target)
                                } else {
                                    base
                                }
                                }
                                .filterNot { it.id in current.hiddenIds }
                                .toList()

                        current.copy(
                            todayItems = todaySource,
                            totalAllItems = totalSource,
                            totalItems = applyFilters(
                                all = totalSource,
                                hiddenIds = current.hiddenIds,
                                query = current.query,
                                filterType = current.filterType,
                                sort = current.sort,
                                doneOnly = current.doneOnly
                            )
                        )
                    }
                }
        }
    }

    fun addTodo(title: String, scheduledDay: Long) {
        viewModelScope.launch {
            addTodoUseCase(title = title, scheduledDay = scheduledDay)
        }
    }

    fun addGoal(title: String, periodicity: GoalPeriodicity, dueDay: Long) {
        viewModelScope.launch {
            addGoalUseCase(
                title = title,
                periodicity = periodicity,
                dueDay = dueDay,
                scheduledDay = todayEpochDay()
            )
        }
    }

    fun updateTodo(id: String, title: String, scheduledDay: Long) {
        val task = taskById[id] ?: return
        if (task.type != TaskType.TODO) return
        viewModelScope.launch {
            updateTaskUseCase(
                task.copy(
                    title = title,
                    scheduledDay = scheduledDay
                )
            )
        }
    }

    fun updateGoal(id: String, title: String, periodicity: GoalPeriodicity, dueDay: Long) {
        val task = taskById[id] ?: return
        if (task.type != TaskType.GOAL) return
        viewModelScope.launch {
            updateTaskUseCase(
                task.copy(
                    title = title,
                    periodicity = periodicity,
                    dueDay = dueDay
                )
            )
        }
    }

    fun onToggleDone(id: String) {
        val state = _state.value
        val item = state.totalAllItems.firstOrNull { it.id == id }
            ?: state.todayItems.firstOrNull { it.id == id }
            ?: return

        if (item.type == TodayItemType.TODO) {
            removeTodoWithUndo(item)
        } else {
            // Por ahora no removemos goals.
            // Cuando quieras: toggle done en DB.
        }
    }

    fun onIncrementGoal(id: String) {
        val state = _state.value
        val item = state.totalAllItems.firstOrNull { it.id == id }
            ?: state.todayItems.firstOrNull { it.id == id }
            ?: return

        val current = item.current ?: 0
        val target = item.target ?: current
        val next = (current + 1).coerceAtMost(target)
        if (next == current) return

        updateGoalCurrent(id = id, nextValue = next)
        applyGoalIncrement(id = id)
    }

    fun onDecrementGoal(id: String) {
        val state = _state.value
        val item = state.totalAllItems.firstOrNull { it.id == id }
            ?: state.todayItems.firstOrNull { it.id == id }
            ?: return

        val current = item.current ?: 0
        val next = (current - 1).coerceAtLeast(0)
        if (next == current) return

        updateGoalCurrent(id = id, nextValue = next)
        applyGoalDecrement(id = id)
    }

    fun onUncomplete(id: String) {
        val state = _state.value
        val item = state.totalAllItems.firstOrNull { it.id == id }
            ?: state.todayItems.firstOrNull { it.id == id }
            ?: return

        if (!item.isDone) return

        _state.update { it.copy(hiddenIds = it.hiddenIds - id) }

        viewModelScope.launch {
            setDoneUseCase(id = id, isDone = false, now = System.currentTimeMillis())
            _events.trySend(HomeEvent.ShowUndoUncompleteTodo(item))
        }
    }

    fun undoUncomplete(id: String) {
        viewModelScope.launch {
            setDoneUseCase(id = id, isDone = true, now = System.currentTimeMillis())
        }
    }

    private fun applyGoalIncrement(id: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val epochDay = java.time.LocalDate.now().toEpochDay()

            insertGoalProgressEventUseCase(
                GoalProgressEvent(
                    id = UUID.randomUUID().toString(),
                    goalId = id,
                    delta = 1,
                    timestamp = now,
                    epochDay = epochDay
                )
            )
        }
    }

    private fun removeTodoWithUndo(todo: TodayItemUiModel) {
        if (todo.isDone) return

        _state.update { current ->
            val newHidden = current.hiddenIds + todo.id
            val newTotalAll = current.totalAllItems.filterNot { it.id == todo.id }
            current.copy(
                hiddenIds = newHidden,
                todayItems = current.todayItems.filterNot { it.id == todo.id },
                totalAllItems = newTotalAll,
                totalItems = applyFilters(
                    all = newTotalAll,
                    hiddenIds = newHidden,
                    query = current.query,
                    filterType = current.filterType,
                    sort = current.sort,
                    doneOnly = current.doneOnly
                )
            )
        }

        removedStack.addLast(todo.copy(isDone = true))
        emitSnackbarForTop()
        scheduleTopConfirmation()
    }

    private fun emitSnackbarForTop() {
        val top = removedStack.lastOrNull() ?: return
        _events.trySend(HomeEvent.ShowUndoRemovedTodo(top))
    }

    private fun scheduleTopConfirmation() {
        confirmTopJob?.cancel()
        val expectedId = removedStack.lastOrNull()?.id ?: return

        confirmTopJob =
            viewModelScope.launch {
                delay(3_000)

                val currentTop = removedStack.lastOrNull()
                if (currentTop?.id != expectedId) return@launch

                val top = removedStack.removeLast()
                val now = System.currentTimeMillis()

                setDoneUseCase(id = top.id, isDone = true, now = now)

                _state.update { current ->
                    val newHidden = current.hiddenIds - top.id
                    current.copy(
                        hiddenIds = newHidden,
                        totalItems = applyFilters(
                            all = current.totalAllItems,
                            hiddenIds = newHidden,
                            query = current.query,
                            filterType = current.filterType,
                            sort = current.sort,
                            doneOnly = current.doneOnly
                        )
                    )
                }

                if (removedStack.isNotEmpty()) {
                    emitSnackbarForTop()
                    scheduleTopConfirmation()
                }
            }
    }

    fun undoRemoveTodo(todoId: String) {
        val top = removedStack.lastOrNull() ?: return
        if (top.id != todoId) return

        removedStack.removeLast()
        confirmTopJob?.cancel()
        confirmTopJob = null

        _state.update { current ->
            val newHidden = current.hiddenIds - todoId
            val newTotalAll =
                if (current.totalAllItems.none { it.id == todoId }) current.totalAllItems + top.copy(isDone = false)
                else current.totalAllItems

            current.copy(
                hiddenIds = newHidden,
                totalAllItems = newTotalAll,
                totalItems = applyFilters(
                    all = newTotalAll,
                    hiddenIds = newHidden,
                    query = current.query,
                    filterType = current.filterType,
                    sort = current.sort,
                    doneOnly = current.doneOnly
                )
            )
        }

        if (removedStack.isNotEmpty()) {
            emitSnackbarForTop()
            scheduleTopConfirmation()
        }
    }

    fun onQueryChanged(value: String) {
        _state.update { current ->
            current.copy(
                query = value,
                totalItems = applyFilters(current.totalAllItems, current.hiddenIds, value, current.filterType, current.sort, current.doneOnly)
            )
        }
    }

    private fun applyGoalDecrement(id: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val epochDay = java.time.LocalDate.now().toEpochDay()

            insertGoalProgressEventUseCase(
                GoalProgressEvent(
                    id = UUID.randomUUID().toString(),
                    goalId = id,
                    delta = -1,
                    timestamp = now,
                    epochDay = epochDay
                )
            )
        }
    }

    private fun updateGoalCurrent(id: String, nextValue: Int) {
        _state.update { current ->
            val newToday =
                current.todayItems.map { if (it.id == id) it.copy(current = nextValue) else it }

            val newTotalAll =
                current.totalAllItems.map { if (it.id == id) it.copy(current = nextValue) else it }

            current.copy(
                todayItems = newToday,
                totalAllItems = newTotalAll,
                totalItems = applyFilters(
                    all = newTotalAll,
                    hiddenIds = current.hiddenIds,
                    query = current.query,
                    filterType = current.filterType,
                    sort = current.sort,
                    doneOnly = current.doneOnly
                )
            )
        }
    }

    private fun applyFilters(
        all: List<TodayItemUiModel>,
        hiddenIds: Set<String>,
        query: String,
        filterType: TaskFilterType,
        sort: TaskSort,
        doneOnly: Boolean
    ): List<TodayItemUiModel> {
        var seq = all.asSequence().filterNot { it.id in hiddenIds }

        seq =
            if (doneOnly) {
                seq.filter { it.isDone }
            } else {
                seq.filter { !it.isDone }
            }

        seq =
            when (filterType) {
                TaskFilterType.ALL -> seq
                TaskFilterType.TODO -> seq.filter { it.type == TodayItemType.TODO }
                TaskFilterType.GOAL -> seq.filter { it.type == TodayItemType.GOAL }
            }

        val q = query.trim()
        if (q.isNotBlank()) {
            val qLower = q.lowercase()
            seq = seq.filter { it.title.lowercase().contains(qLower) }
        }

        val list = seq.toList()

        return when (sort) {
            TaskSort.RECENT -> list
            TaskSort.ALPHA -> list.sortedBy { it.title.lowercase() }
        }
    }

    fun applyFilters(type: TaskFilterType, sort: TaskSort) {
        _state.update { current ->
            current.copy(
                filterType = type,
                sort = sort,
                totalItems = applyFilters(current.totalAllItems, current.hiddenIds, current.query, type, sort, current.doneOnly)
            )
        }
    }

    fun applyFilters(type: TaskFilterType, sort: TaskSort, doneOnly: Boolean) {
        _state.update { current ->
            current.copy(
                filterType = type,
                sort = sort,
                doneOnly = doneOnly,
                totalItems = applyFilters(current.totalAllItems, current.hiddenIds, current.query, type, sort, doneOnly)
            )
        }
    }

    private fun todayEpochDay(): Long = java.time.LocalDate.now().toEpochDay()

    private fun periodRangeForToday(periodicity: GoalPeriodicity, todayEpochDay: Long): Pair<Long, Long> {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        return when (periodicity) {
            GoalPeriodicity.DAILY -> todayEpochDay to todayEpochDay
            GoalPeriodicity.WEEKLY -> {
                val start = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                val end = start.plusDays(6)
                start.toEpochDay() to end.toEpochDay()
            }
            GoalPeriodicity.MONTHLY -> {
                val start = today.withDayOfMonth(1)
                val end = start.plusMonths(1).minusDays(1)
                start.toEpochDay() to end.toEpochDay()
            }
            GoalPeriodicity.SEMESTRAL -> {
                val start = if (today.monthValue <= 6) {
                    LocalDate.of(today.year, 1, 1)
                } else {
                    LocalDate.of(today.year, 7, 1)
                }
                val end = start.plusMonths(6).minusDays(1)
                start.toEpochDay() to end.toEpochDay()
            }
            GoalPeriodicity.ANNUAL -> {
                val start = LocalDate.of(today.year, 1, 1)
                val end = LocalDate.of(today.year, 12, 31)
                start.toEpochDay() to end.toEpochDay()
            }
        }
    }

    private fun periodTarget(periodicity: GoalPeriodicity, todayEpochDay: Long): Int {
        val (start, end) = periodRangeForToday(periodicity, todayEpochDay)
        return (end - start + 1).toInt().coerceAtLeast(1)
    }

    override fun onCleared() {
        confirmTopJob?.cancel()
        confirmTopJob = null
        removedStack.clear()
        super.onCleared()
    }
}
