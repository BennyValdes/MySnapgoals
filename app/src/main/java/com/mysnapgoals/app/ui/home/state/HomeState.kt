package com.mysnapgoals.app.ui.home.state

import com.mysnapgoals.app.ui.home.components.TodayItemUiModel

data class HomeState(
    val todayItems: List<TodayItemUiModel> = emptyList(),          // NO filtrable
    val totalAllItems: List<TodayItemUiModel> = emptyList(),       // source para TotalList
    val totalItems: List<TodayItemUiModel> = emptyList(),          // filtrado (TotalList)
    val hiddenIds: Set<String> = emptySet(),
    val query: String = "",
    val filterType: TaskFilterType = TaskFilterType.ALL,
    val sort: TaskSort = TaskSort.RECENT,
    val doneOnly: Boolean = false
)
