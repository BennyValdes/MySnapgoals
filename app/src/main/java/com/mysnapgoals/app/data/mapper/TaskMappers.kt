package com.mysnapgoals.app.data.mapper

import com.mysnapgoals.app.data.local.entity.TaskEntity
import com.mysnapgoals.app.data.local.entity.TaskEntity.Companion.TYPE_TODO
import com.mysnapgoals.app.ui.components.TodayItemType.GOAL
import com.mysnapgoals.app.ui.components.TodayItemType.TODO
import com.mysnapgoals.app.ui.components.TodayItemUiModel

fun TaskEntity.toUiModel(): TodayItemUiModel =
    TodayItemUiModel(
        id = id,
        type = if (type == TYPE_TODO) TODO else GOAL,
        title = title,
        isDone = isDone,
        current = current,
        target = target
    )