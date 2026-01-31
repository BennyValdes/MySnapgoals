package com.mysnapgoals.app.ui.home.state

import com.mysnapgoals.app.ui.home.components.TodayItemUiModel

sealed class HomeEvent {
    data class ShowUndoRemovedTodo(val todo: TodayItemUiModel) : HomeEvent()
    data class ShowUndoUncompleteTodo(val todo: TodayItemUiModel) : HomeEvent()
}
