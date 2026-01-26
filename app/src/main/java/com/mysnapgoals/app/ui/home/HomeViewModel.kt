package com.mysnapgoals.app.ui.home

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.mysnapgoals.app.SnapGoalsGraph
import com.mysnapgoals.app.data.local.entity.TaskEntity
import com.mysnapgoals.app.data.mapper.toUiModel
import com.mysnapgoals.app.ui.components.TodayItemType
import com.mysnapgoals.app.ui.components.TodayItemUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class TaskFilterType { ALL, TODO, GOAL }
enum class TaskSort { RECENT, ALPHA }

data class HomeState(
    val todayItems: List<TodayItemUiModel> = emptyList(),          // NO filtrable
    val totalAllItems: List<TodayItemUiModel> = emptyList(),       // source para TotalList
    val totalItems: List<TodayItemUiModel> = emptyList(),          // filtrado (TotalList)
    val hiddenIds: Set<String> = emptySet(),
    val query: String = "",
    val filterType: TaskFilterType = TaskFilterType.ALL,
    val sort: TaskSort = TaskSort.RECENT
) : MavericksState

sealed class HomeEvent {
    data class ShowUndoRemovedTodo(val todo: TodayItemUiModel) : HomeEvent()
}

class HomeViewModel(
    initialState: HomeState
) : MavericksViewModel<HomeState>(initialState) {

    companion object : MavericksViewModelFactory<HomeViewModel, HomeState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: HomeState
        ): HomeViewModel = HomeViewModel(state)
    }

    private val repo = SnapGoalsGraph.tasksRepository

    private val _events = Channel<HomeEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val removedStack = ArrayDeque<TodayItemUiModel>()
    private var confirmTopJob: Job? = null

    init {
        viewModelScope.launch {
            repo.tasksState.collect { entities ->
                setState {
                    val today = todayEpochDay()

                    val todaySource =
                        entities.asSequence()
                            .filter { !it.isDone }
                            .filter { it.scheduledDay == today }
                            .map { it.toUiModel() }
                            .filterNot { it.id in hiddenIds } // respeta undo overlay
                            .toList()

                    val totalSource =
                        entities.asSequence()
                            .filter { !it.isDone }
                            // si quieres incluir también scheduledDay==null o cualquier día, aquí es donde se define
                            .map { it.toUiModel() }
                            .filterNot { it.id in hiddenIds }
                            .toList()

                    copy(
                        todayItems = todaySource,
                        totalAllItems = totalSource,
                        totalItems = applyFilters(
                            all = totalSource,
                            hiddenIds = hiddenIds,
                            query = query,
                            filterType = filterType,
                            sort = sort
                        )
                    )
                }
            }
        }
    }

    fun addTodo(title: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repo.upsert(
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    type = TaskEntity.TYPE_TODO,
                    title = title,
                    isDone = false,
                    scheduledDay = todayEpochDay(),
                    createdAt = now,
                    updatedAt = now,
                    current = null,
                    target = null
                )
            )
        }
    }

    fun addGoal(title: String, target: Int) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repo.upsert(
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    type = TaskEntity.TYPE_GOAL,
                    title = title,
                    isDone = false,
                    scheduledDay = todayEpochDay(),
                    createdAt = now,
                    updatedAt = now,
                    current = 0,
                    target = target
                )
            )
        }
    }

    fun onToggleDone(id: String) {
        withState { state ->
            val item = state.totalAllItems.firstOrNull { it.id == id }
                ?: state.todayItems.firstOrNull { it.id == id }
                ?: return@withState

            if (item.type == TodayItemType.TODO) {
                removeTodoWithUndo(item)
            } else {
                // Por ahora no removemos goals.
                // Cuando quieras: toggle done en DB.
            }
        }
    }

    fun onIncrementGoal(id: String) {
        withState { st ->
            val item = st.totalAllItems.firstOrNull { it.id == id }
                ?: st.todayItems.firstOrNull { it.id == id }
                ?: return@withState

            val current = item.current ?: 0
            val target = item.target ?: current

            val next = (current + 1).coerceAtMost(target)
            if (next == current) return@withState


            val reachedTarget = next >= target
            applyGoalIncrement(id = id, nextValue = next, markDone = reachedTarget)
        }
    }

    private fun applyGoalIncrement(id: String, nextValue: Int, markDone: Boolean) {
        setState {
            val newToday =
                if (markDone) todayItems.filterNot { it.id == id }
                else todayItems.map { if (it.id == id) it.copy(current = nextValue) else it }

            val newTotalAll =
                if (markDone) totalAllItems.filterNot { it.id == id }
                else totalAllItems.map { if (it.id == id) it.copy(current = nextValue) else it }

            copy(
                todayItems = newToday,
                totalAllItems = newTotalAll,
                totalItems = applyFilters(
                    all = newTotalAll,
                    hiddenIds = hiddenIds,
                    query = query,
                    filterType = filterType,
                    sort = sort
                )
            )
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repo.setCurrent(id = id, current = nextValue, now = now)
            if (markDone) repo.setDone(id = id, isDone = true, now = now)
        }
    }

    private fun removeTodoWithUndo(todo: TodayItemUiModel) {
        if (todo.isDone) return

        setState {
            val newHidden = hiddenIds + todo.id

            copy(
                hiddenIds = newHidden,
                todayItems = todayItems.filterNot { it.id == todo.id },
                totalAllItems = totalAllItems.filterNot { it.id == todo.id },
                totalItems = applyFilters(
                    all = totalAllItems.filterNot { it.id == todo.id },
                    hiddenIds = newHidden,
                    query = query,
                    filterType = filterType,
                    sort = sort
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

                // Persistimos el done en DB
                repo.setDone(top.id, true, now)

                // Quitamos el hidden overlay (ya quedó confirmado)
                setState {
                    val newHidden = hiddenIds - top.id

                    // Nota: aquí NO reinsertamos nada; solo recalculamos totalItems
                    // totalAllItems ya no contiene el item porque lo removiste optimistamente.
                    copy(
                        hiddenIds = newHidden,
                        totalItems = applyFilters(
                            all = totalAllItems,
                            hiddenIds = newHidden,
                            query = query,
                            filterType = filterType,
                            sort = sort
                        )
                        // todayItems se reconstruirá por repo.tasksState, no hace falta tocarlo
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

        setState {
            val newHidden = hiddenIds - todoId

            val newTotalAll =
                if (totalAllItems.none { it.id == todoId }) totalAllItems + top.copy(isDone = false)
                else totalAllItems

            copy(
                hiddenIds = newHidden,
                totalAllItems = newTotalAll,
                totalItems = applyFilters(
                    all = newTotalAll,
                    hiddenIds = newHidden,
                    query = query,
                    filterType = filterType,
                    sort = sort
                )
                // todayItems NO lo forzamos aquí: lo reconstruye repo.tasksState
            )
        }

        if (removedStack.isNotEmpty()) {
            emitSnackbarForTop()
            scheduleTopConfirmation()
        }
    }

    fun onQueryChanged(value: String) {
        setState {
            copy(
                query = value,
                totalItems = applyFilters(totalAllItems, hiddenIds, value, filterType, sort)
            )
        }
    }

    private fun applyFilters(
        all: List<TodayItemUiModel>,
        hiddenIds: Set<String>,
        query: String,
        filterType: TaskFilterType,
        sort: TaskSort
    ): List<TodayItemUiModel> {
        var seq = all.asSequence().filterNot { it.id in hiddenIds }

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
        setState {
            copy(
                filterType = type,
                sort = sort,
                totalItems = applyFilters(totalAllItems, hiddenIds, query, type, sort)
            )
        }
    }

    private fun todayEpochDay(): Long {
        return java.time.LocalDate.now().toEpochDay()
    }


    override fun onCleared() {
        confirmTopJob?.cancel()
        confirmTopJob = null
        removedStack.clear()
        super.onCleared()
    }
}