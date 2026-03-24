package com.mysnapgoals.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mysnapgoals.app.domain.model.GoalPeriodicity
import com.mysnapgoals.app.domain.model.GoalProgressEvent
import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType
import com.mysnapgoals.app.domain.util.GoalPeriodMath
import com.mysnapgoals.app.domain.util.GoalProgressIndex
import com.mysnapgoals.app.domain.usecase.AddGoalUseCase
import com.mysnapgoals.app.domain.usecase.AddTodoUseCase
import com.mysnapgoals.app.domain.usecase.InsertGoalProgressEventUseCase
import com.mysnapgoals.app.domain.usecase.ObserveTasksUseCase
import com.mysnapgoals.app.domain.usecase.SetDoneUseCase
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
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
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
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val observeGoalProgressEventsUseCase: ObserveGoalProgressEventsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private val _events = Channel<HomeEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val todayEpochDayFlow = MutableStateFlow(todayEpochDay())

    private val removedTodosStack = ArrayDeque<TodayItemUiModel>()
    private var confirmRemovalJob: Job? = null
    private val defaultGoalPeriodicity = GoalPeriodicity.MONTHLY
    private var tasksById: Map<String, Task> = emptyMap()

    init {
        viewModelScope.launch {
            while (true) {
                val now = ZonedDateTime.now()
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
                val delayMs = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0)
                delay(delayMs)
                todayEpochDayFlow.value = todayEpochDay()
            }
        }

        viewModelScope.launch {
            combine(
                observeTasks(),
                observeGoalProgressEventsUseCase(),
                todayEpochDayFlow
            ) { tasks, events, _ -> tasks to events }
                .collect { (entities, events) ->
                val today = todayEpochDay()
                val goalProgressToday = mutableMapOf<String, Int>()
                val goalProgressCurrent = mutableMapOf<String, Int>()
                val goalTargetCurrent = mutableMapOf<String, Int>()
                val goalProgressIndex = GoalProgressIndex.from(events)
                tasksById = entities.associateBy { it.id }

                for (goal in entities.filter { it.type == TaskType.GOAL }) {
                    val periodicity = goal.periodicity ?: defaultGoalPeriodicity
                    val (periodStart, periodEnd) = GoalPeriodMath.periodRangeForToday(periodicity, today)
                    val progressCurrent = goalProgressIndex.sum(goal.id, periodStart, periodEnd)
                    val progressToday = goalProgressIndex.sum(goal.id, today, today)
                    val target = GoalPeriodMath.periodTarget(periodicity, today)
                    goalProgressCurrent[goal.id] = progressCurrent
                    goalProgressToday[goal.id] = progressToday
                    goalTargetCurrent[goal.id] = target
                }

                _state.update { current ->
                    val allItems =
                        entities.asSequence()
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
                            .filterNot { it.id in current.hiddenItemIds }
                            .toList()

                    val todayItems =
                        allItems.asSequence()
                            .filter { !it.isDone }
                            .filter { item ->
                                if (item.type == TodayItemType.GOAL) {
                                    val dueDay = item.dueDay ?: today
                                    dueDay >= today
                                } else {
                                    (item.scheduledDay ?: today) == today
                                }
                            }
                            .filterNot { item ->
                                item.type == TodayItemType.GOAL &&
                                    (goalProgressToday[item.id] ?: 0) > 0
                            }
                            .toList()

                    current.copy(
                        todayItems = todayItems,
                        allItems = allItems,
                        filteredItems = filterItems(
                            all = allItems,
                            hiddenItemIds = current.hiddenItemIds,
                            query = current.query,
                            filterType = current.filterType,
                            sortOrder = current.sortOrder,
                            showDoneOnly = current.showDoneOnly
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
        val task = tasksById[id] ?: return
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
        val task = tasksById[id] ?: return
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
        val item = findItem(id) ?: return

        if (item.type != TodayItemType.TODO) return
        removeTodoWithUndo(item)
    }

    fun onIncrementGoal(id: String) {
        val item = findItem(id) ?: return

        val current = item.current ?: 0
        val target = item.target ?: current
        val next = (current + 1).coerceAtMost(target)
        if (next == current) return

        updateGoalCurrent(id = id, nextValue = next)
        applyGoalIncrement(id = id)
    }

    fun onDecrementGoal(id: String) {
        val item = findItem(id) ?: return

        val current = item.current ?: 0
        val next = (current - 1).coerceAtLeast(0)
        if (next == current) return

        updateGoalCurrent(id = id, nextValue = next)
        applyGoalDecrement(id = id)
    }

    fun onUncomplete(id: String) {
        val item = findItem(id) ?: return

        if (!item.isDone) return

        _state.update { it.copy(hiddenItemIds = it.hiddenItemIds - id) }

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
            val epochDay = LocalDate.now().toEpochDay()

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
            val newHidden = current.hiddenItemIds + todo.id
            val newAllItems = current.allItems.filterNot { it.id == todo.id }
            current.copy(
                hiddenItemIds = newHidden,
                todayItems = current.todayItems.filterNot { it.id == todo.id },
                allItems = newAllItems,
                filteredItems = filterItems(
                    all = newAllItems,
                    hiddenItemIds = newHidden,
                    query = current.query,
                    filterType = current.filterType,
                    sortOrder = current.sortOrder,
                    showDoneOnly = current.showDoneOnly
                )
            )
        }

        removedTodosStack.addLast(todo.copy(isDone = true))
        emitUndoRemovedTodo()
        scheduleRemovalConfirmation()
    }

    private fun emitUndoRemovedTodo() {
        val top = removedTodosStack.lastOrNull() ?: return
        _events.trySend(HomeEvent.ShowUndoRemovedTodo(top))
    }

    private fun scheduleRemovalConfirmation() {
        confirmRemovalJob?.cancel()
        val expectedId = removedTodosStack.lastOrNull()?.id ?: return

        confirmRemovalJob =
            viewModelScope.launch {
                delay(3_000)

                val currentTop = removedTodosStack.lastOrNull()
                if (currentTop?.id != expectedId) return@launch

                val top = removedTodosStack.removeLast()
                val now = System.currentTimeMillis()

                setDoneUseCase(id = top.id, isDone = true, now = now)

                _state.update { current ->
                    val newHidden = current.hiddenItemIds - top.id
                    current.copy(
                        hiddenItemIds = newHidden,
                        filteredItems = filterItems(
                            all = current.allItems,
                            hiddenItemIds = newHidden,
                            query = current.query,
                            filterType = current.filterType,
                            sortOrder = current.sortOrder,
                            showDoneOnly = current.showDoneOnly
                        )
                    )
                }

                if (removedTodosStack.isNotEmpty()) {
                    emitUndoRemovedTodo()
                    scheduleRemovalConfirmation()
                }
            }
    }

    fun undoRemoveTodo(todoId: String) {
        val top = removedTodosStack.lastOrNull() ?: return
        if (top.id != todoId) return

        removedTodosStack.removeLast()
        confirmRemovalJob?.cancel()
        confirmRemovalJob = null

        _state.update { current ->
            val newHidden = current.hiddenItemIds - todoId
            val restoredTodo = top.copy(isDone = false)
            val newAllItems =
                if (current.allItems.none { it.id == todoId }) current.allItems + restoredTodo
                else current.allItems
            val todayEpochDay = todayEpochDay()
            val shouldAppearToday = (restoredTodo.scheduledDay ?: todayEpochDay) == todayEpochDay
            val newTodayItems =
                if (shouldAppearToday && current.todayItems.none { it.id == todoId }) {
                    current.todayItems + restoredTodo
                } else {
                    current.todayItems
                }

            current.copy(
                hiddenItemIds = newHidden,
                todayItems = newTodayItems,
                allItems = newAllItems,
                filteredItems = filterItems(
                    all = newAllItems,
                    hiddenItemIds = newHidden,
                    query = current.query,
                    filterType = current.filterType,
                    sortOrder = current.sortOrder,
                    showDoneOnly = current.showDoneOnly
                )
            )
        }

        if (removedTodosStack.isNotEmpty()) {
            emitUndoRemovedTodo()
            scheduleRemovalConfirmation()
        }
    }

    fun onQueryChanged(value: String) {
        _state.update { current ->
            current.copy(
                query = value,
                filteredItems = filterItems(current.allItems, current.hiddenItemIds, value, current.filterType, current.sortOrder, current.showDoneOnly)
            )
        }
    }

    private fun applyGoalDecrement(id: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val epochDay = LocalDate.now().toEpochDay()

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

            val newAllItems =
                current.allItems.map { if (it.id == id) it.copy(current = nextValue) else it }

            current.copy(
                todayItems = newToday,
                allItems = newAllItems,
                filteredItems = filterItems(
                    all = newAllItems,
                    hiddenItemIds = current.hiddenItemIds,
                    query = current.query,
                    filterType = current.filterType,
                    sortOrder = current.sortOrder,
                    showDoneOnly = current.showDoneOnly
                )
            )
        }
    }

    private fun filterItems(
        all: List<TodayItemUiModel>,
        hiddenItemIds: Set<String>,
        query: String,
        filterType: TaskFilterType,
        sortOrder: TaskSort,
        showDoneOnly: Boolean
    ): List<TodayItemUiModel> {
        var seq = all.asSequence().filterNot { it.id in hiddenItemIds }

        seq =
            if (showDoneOnly) {
                seq.filter { it.isEffectivelyDone() }
            } else {
                seq.filter { !it.isEffectivelyDone() }
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

        return when (sortOrder) {
            TaskSort.RECENT -> list
            TaskSort.ALPHA -> list.sortedBy { it.title.lowercase() }
        }
    }

    private fun TodayItemUiModel.isEffectivelyDone(): Boolean {
        if (isDone) return true
        if (type != TodayItemType.GOAL) return false

        val currentValue = current ?: return false
        val targetValue = target ?: return false
        return targetValue > 0 && currentValue >= targetValue
    }

    fun applyFilters(type: TaskFilterType, sortOrder: TaskSort) {
        _state.update { current ->
            current.copy(
                filterType = type,
                sortOrder = sortOrder,
                filteredItems = filterItems(current.allItems, current.hiddenItemIds, current.query, type, sortOrder, current.showDoneOnly)
            )
        }
    }

    fun applyFilters(type: TaskFilterType, sortOrder: TaskSort, showDoneOnly: Boolean) {
        _state.update { current ->
            current.copy(
                filterType = type,
                sortOrder = sortOrder,
                showDoneOnly = showDoneOnly,
                filteredItems = filterItems(current.allItems, current.hiddenItemIds, current.query, type, sortOrder, showDoneOnly)
            )
        }
    }

    private fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    private fun findItem(id: String): TodayItemUiModel? {
        val current = _state.value
        return current.allItems.firstOrNull { it.id == id }
            ?: current.todayItems.firstOrNull { it.id == id }
    }

    override fun onCleared() {
        confirmRemovalJob?.cancel()
        confirmRemovalJob = null
        removedTodosStack.clear()
        super.onCleared()
    }
}

