package com.mysnapgoals.app.ui.home.mapper

import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType
import com.mysnapgoals.app.ui.home.components.TodayItemType.GOAL
import com.mysnapgoals.app.ui.home.components.TodayItemType.TODO
import com.mysnapgoals.app.ui.home.components.TodayItemUiModel

fun Task.toUiModel(): TodayItemUiModel =
    TodayItemUiModel(
        id = id,
        type = if (type == TaskType.TODO) TODO else GOAL,
        title = title,
        isDone = isDone,
        current = current,
        target = target
    )
