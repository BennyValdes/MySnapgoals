package com.mysnapgoals.app.ui.home

import android.util.Log
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class TaskFilterType { ALL, TODO, GOAL }
enum class TaskSort { RECENT, ALPHA }

data class HomeState(
    val allItems: List<TodayItemUiModel> = emptyList(), // source (pendientes desde Room)
    val items: List<TodayItemUiModel> = emptyList(),    // vista (filtrado)
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

    private val removedStack = ArrayDeque<TodayItemUiModel>() // LIFO
    private var confirmTopJob: Job? = null

    init {
        viewModelScope.launch {
            repo.observeAll().collect { entities ->
                val e = entities.firstOrNull { it.id == "2" }
                Log.d("Special SnapGoals", "DB emit id=2 current=${e?.current}")

                val uiModels = entities.map { it.toUiModel() }


                setState {
                    val today = todayEpochDay()

                    val source =
                        entities
                            .asSequence()
                            .filter { !it.isDone } // solo pendientes
                            .filter { it.scheduledDay == null || it.scheduledDay == today } // solo "Hoy"
                            .map { it.toUiModel() }
                            .toList()

                    copy(
                        allItems = source,
                        items = applyFilters(
                            all = source,
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

    // Luego lo usaremos con AddLine
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
            val item = state.allItems.firstOrNull { it.id == id } ?: return@withState

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
            val item = st.allItems.firstOrNull { it.id == id } ?: return@withState

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
            val newAll =
                if (markDone) {
                    allItems.filterNot { it.id == id } // ya no debe aparecer en Hoy
                } else {
                    allItems.map { ui ->
                        if (ui.id == id) ui.copy(current = nextValue) else ui
                    }
                }

            copy(
                allItems = newAll,
                items = applyFilters(
                    all = newAll,
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

            if (markDone) {
                repo.setDone(id = id, isDone = true, now = now)
            }
        }
    }

    private fun removeTodoWithUndo(todo: TodayItemUiModel) {
        // Evita dobles taps raros
        if (todo.isDone) return

        // 1) ocultar optimistamente (overlay + lista visible)
        setState {
            val newHidden = hiddenIds + todo.id
            copy(
                hiddenIds = newHidden,
                items = applyFilters(
                    all = allItems,
                    hiddenIds = newHidden,
                    query = query,
                    filterType = filterType,
                    sort = sort
                )
            )
        }

        // 2) push stack
        removedStack.addLast(todo.copy(isDone = true))

        // 3) snackbar para el top actual
        emitSnackbarForTop()

        // 4) ventana de 3s para confirmar el top
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

                // Si el top cambió durante la espera, no confirmamos aquí
                val currentTop = removedStack.lastOrNull()
                if (currentTop?.id != expectedId) return@launch

                val top = removedStack.removeLast()
                val now = System.currentTimeMillis()

                repo.setDone(top.id, true, now)

                setState {
                    val newHidden = hiddenIds - top.id

                    copy(
                        hiddenIds = newHidden,
                        // allItems NO se toca aquí; será recalculado por el Flow de Room
                        items = applyFilters(
                            all = allItems,
                            hiddenIds = newHidden,
                            query = query,
                            filterType = filterType,
                            sort = sort
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

        // Sacar del stack
        removedStack.removeLast()

        // Cancelar confirmación del top actual
        confirmTopJob?.cancel()
        confirmTopJob = null

        setState {
            val newHidden = hiddenIds - todoId
            copy(
                hiddenIds = newHidden,
                items = applyFilters(
                    all = allItems,
                    hiddenIds = newHidden,
                    query = query,
                    filterType = filterType,
                    sort = sort
                )
            )
        }

        // Si quedan pendientes, reanudar confirmación del nuevo top
        if (removedStack.isNotEmpty()) {
            emitSnackbarForTop()
            scheduleTopConfirmation()
        }
    }

    fun onQueryChanged(value: String) {
        setState {
            copy(
                query = value,
                items = applyFilters(allItems, hiddenIds, value, filterType, sort)
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
                items = applyFilters(allItems, hiddenIds, query, type, sort)
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