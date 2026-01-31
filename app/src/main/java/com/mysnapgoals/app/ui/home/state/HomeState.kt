package com.mysnapgoals.app.ui.home.state

import com.mysnapgoals.app.ui.home.components.TodayItemUiModel

data class HomeState(
    val todayItems: List<TodayItemUiModel> = emptyList(),
    val allItems: List<TodayItemUiModel> = emptyList(),
    val filteredItems: List<TodayItemUiModel> = emptyList(),
    val hiddenItemIds: Set<String> = emptySet(),
    val query: String = "",
    val filterType: TaskFilterType = TaskFilterType.ALL,
    val sortOrder: TaskSort = TaskSort.RECENT,
    val showDoneOnly: Boolean = false
)
