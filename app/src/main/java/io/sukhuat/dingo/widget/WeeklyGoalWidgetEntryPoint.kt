package io.sukhuat.dingo.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.domain.repository.GoalRepository

/**
 * Hilt entry point for accessing dependencies in widget context
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WeeklyGoalWidgetEntryPoint {
    fun getWidgetRepository(): WeeklyGoalWidgetRepository
    fun getGoalRepository(): GoalRepository
    fun getWidgetDataLoader(): WidgetDataLoader
}
