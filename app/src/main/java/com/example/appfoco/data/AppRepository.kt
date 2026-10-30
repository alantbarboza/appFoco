package com.example.appfoco.data

import android.content.Context
import com.example.appfoco.data.dao.AppDao
import com.example.appfoco.data.entity.AppSettingsEntity
import com.example.appfoco.data.entity.DeathRecordEntity
import com.example.appfoco.data.entity.ForbiddenRuleEntity
import com.example.appfoco.data.entity.HabitEntity
import com.example.appfoco.data.entity.HabitLogEntity
import com.example.appfoco.data.entity.MilestoneEntity
import com.example.appfoco.data.entity.PetEntity
import com.example.appfoco.data.entity.TaskEntity
import com.example.appfoco.widget.FocoAppWidgetProvider
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatToBrazilianDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
    if (dateStr.contains("/")) return dateStr
    return try {
        val isoSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = isoSdf.parse(dateStr)
        if (date != null) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        } else {
            dateStr
        }
    } catch (_: Exception) {
        dateStr
    }
}

class AppRepository(private val dao: AppDao, private val context: Context? = null) {

    val petFlow: Flow<PetEntity?> = dao.getPetFlow()
    val habitsFlow: Flow<List<HabitEntity>> = dao.getAllHabitsFlow()
    val rulesFlow: Flow<List<ForbiddenRuleEntity>> = dao.getAllRulesFlow()
    val settingsFlow: Flow<AppSettingsEntity?> = dao.getSettingsFlow()
    val deathRecordsFlow: Flow<List<DeathRecordEntity>> = dao.getAllDeathRecordsFlow()
    val milestonesFlow: Flow<List<MilestoneEntity>> = dao.getAllMilestonesFlow()

    private fun notifyWidgetUpdate() {
        context?.let { FocoAppWidgetProvider.triggerUpdate(it) }
    }

    suspend fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    suspend fun getOrCreatePet(): PetEntity {
        var pet = dao.getPet()
        if (pet == null) {
            val currentDate = getCurrentDate()
            pet = PetEntity(
                id = 1L,
                level = 1,
                energy = 100,
                streakCount = 0,
                lastProgressDate = null,
                lastActiveDate = currentDate,
                isAlive = true,
                highestLevel = 1,
                highestStreak = 0,
                deathCount = 0
            )
            dao.insertOrUpdatePet(pet)
            notifyWidgetUpdate()
        }
        return dao.getPet() ?: pet
    }

    fun getHabitLogsForDateFlow(date: String): Flow<List<HabitLogEntity>> = dao.getHabitLogsForDateFlow(date)
    suspend fun getHabitLogsForDate(date: String): List<HabitLogEntity> = dao.getHabitLogsForDate(date)
    suspend fun insertHabit(habit: HabitEntity) = dao.insertHabit(habit)
    suspend fun deleteHabit(habit: HabitEntity) {
        dao.deleteHabit(habit)
    }

    suspend fun setHabitCompleted(habitId: Long, date: String, completed: Boolean) {
        dao.insertOrUpdateHabitLog(HabitLogEntity(habitId = habitId, date = date, isCompleted = completed))
    }

    fun getTasksForDateFlow(date: String): Flow<List<TaskEntity>> = dao.getTasksForDateFlow(date)
    suspend fun insertTask(task: TaskEntity) = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        dao.deleteTask(task)
    }

    suspend fun insertRule(rule: ForbiddenRuleEntity) = dao.insertRule(rule)
    suspend fun updateRule(rule: ForbiddenRuleEntity) = dao.updateRule(rule)
    suspend fun deleteRule(rule: ForbiddenRuleEntity) = dao.deleteRule(rule)

    suspend fun resetAllData() {
        dao.clearHabitLogs()
        dao.clearHabits()
        dao.clearTasks()
        dao.clearRules()
        dao.clearRuleLogs()
        dao.clearDeathRecords()
        dao.clearMilestones()
        dao.clearOfensivas()
        dao.insertOrUpdateSettings(AppSettingsEntity(id = 1L, hasInitialSeedBeenDone = false, totalLostOfensivasCount = 0))
        notifyWidgetUpdate()
    }
}
