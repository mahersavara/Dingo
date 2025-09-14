package io.sukhuat.dingo.domain.service

/**
 * Service interface for notifying widgets about goal data changes
 * This allows the data layer to notify widgets without depending on the app module
 */
interface WidgetNotificationService {
    fun notifyGoalCreated(goalId: String)
    fun notifyGoalUpdated(goalId: String)
    fun notifyGoalDeleted(goalId: String)
    fun notifyGoalStatusChanged(goalId: String, newStatus: String)
    fun notifyWeekChanged()
}
