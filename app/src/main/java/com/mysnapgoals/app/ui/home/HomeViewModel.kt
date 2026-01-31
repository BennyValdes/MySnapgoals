package com.mysnapgoals.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mysnapgoals.app.domain.model.GoalProgressEvent
import com.mysnapgoals.app.domain.usecase.AddGoalUseCase
import com.mysnapgoals.app.domain.usecase.AddTodoUseCase
import com.mysnapgoals.app.domain.usecase.InsertGoalProgressEventUseCase
import com.mysnapgoals.app.domain.usecase.ObserveTasksUseCase
import com.mysnapgoals.app.domain.usecase.SetCurrentUseCase
import com.mysnapgoals.app.domain.usecase.SetDoneUseCase
import com.mysnapgoals.app.ui.home.components.TodayItemType
import com.mysnapgoals.app.ui.home.components.TodayItemUiModel
import com.mysnapgoals.app.ui.home.mapper.toUiModel
import com.mysnapgoals.app.ui.home.state.HomeEvent
import com.mysnapgoals.app.ui.home.state.HomeState
import com.mysnapgoals.app.ui.home.state.TaskFilterType
import com.mysnapgoals.app.ui.home.state.TaskSort
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val setCurrentUseCase: SetCurrentUseCase,
    private val insertGoalProgressEventUseCase: InsertGoalProgressEventUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private val _events = Channel<HomeEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val removedStack = ArrayDeque<TodayItemUiModel>()
    private var confirmTopJob: Job? = null

    init {
        viewModelScope.launch {
            observeTasks().collect { entities ->
                _state.update { current ->
                    val today = todayEpochDay()

                    val todaySource =
                        entities.asSequence()
                            .filter { !it.isDone }
                            .filter { (it.scheduledDay ?: today) == today }
                            .map { it.toUiModel() }
                            .filterNot { it.id in current.hiddenIds } // respeta undo overlay
                            .toList()

                    val totalSource =
                        entities.asSequence()
                            // si quieres incluir también scheduledDay==null o cualquier día, aquí es donde se define
                            .map { it.toUiModel() }
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

    fun addTodo(title: String) {
        viewModelScope.launch {
            addTodoUseCase(title = title, scheduledDay = todayEpochDay())
        }
    }

    fun addGoal(title: String, target: Int) {
        viewModelScope.launch {
            addGoalUseCase(title = title, target = target, scheduledDay = todayEpochDay())
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

        val reachedTarget = next >= target
        applyGoalIncrement(id = id, nextValue = next, markDone = reachedTarget)
    }

    fun onDecrementGoal(id: String) {
        val state = _state.value
        val item = state.totalAllItems.firstOrNull { it.id == id }
            ?: state.todayItems.firstOrNull { it.id == id }
            ?: return

        val current = item.current ?: 0
        val next = (current - 1).coerceAtLeast(0)
        if (next == current) return

        applyGoalDecrement(id = id, nextValue = next)
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

    private fun applyGoalIncrement(id: String, nextValue: Int, markDone: Boolean) {
        _state.update { current ->
            val newToday =
                if (markDone) current.todayItems.filterNot { it.id == id }
                else current.todayItems.map { if (it.id == id) it.copy(current = nextValue) else it }

            val newTotalAll =
                if (markDone) current.totalAllItems.filterNot { it.id == id }
                else current.totalAllItems.map { if (it.id == id) it.copy(current = nextValue) else it }

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

            setCurrentUseCase(id = id, current = nextValue, now = now)

            if (markDone) {
                setDoneUseCase(id = id, isDone = true, now = now)
            }
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

    private fun applyGoalDecrement(id: String, nextValue: Int) {
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

            setCurrentUseCase(id = id, current = nextValue, now = now)
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

    override fun onCleared() {
        confirmTopJob?.cancel()
        confirmTopJob = null
        removedStack.clear()
        super.onCleared()
    }
}
