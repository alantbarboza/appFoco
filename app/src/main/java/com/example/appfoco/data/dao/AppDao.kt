package com.example.appfoco.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appfoco.data.entity.AppSettingsEntity
import com.example.appfoco.data.entity.DeathRecordEntity
import com.example.appfoco.data.entity.ForbiddenRuleEntity
import com.example.appfoco.data.entity.HabitEntity
import com.example.appfoco.data.entity.HabitLogEntity
import com.example.appfoco.data.entity.MilestoneEntity
import com.example.appfoco.data.entity.OfensivaEntity
import com.example.appfoco.data.entity.PetEntity
import com.example.appfoco.data.entity.RuleLogEntity
import com.example.appfoco.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM ofensivas")
    fun getAllOfensivasFlow(): Flow<List<OfensivaEntity>>

    @Query("SELECT * FROM ofensivas")
    suspend fun getAllOfensivas(): List<OfensivaEntity>

    @Query("SELECT * FROM ofensivas WHERE id = :id")
    suspend fun getOfensivaById(id: Long): OfensivaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOfensiva(ofensiva: OfensivaEntity): Long

    @Delete
    suspend fun deleteOfensiva(ofensiva: OfensivaEntity)

    @Query("SELECT * FROM pet WHERE id = 1")
    fun getPetFlow(): Flow<PetEntity?>

    @Query("SELECT * FROM pet WHERE id = 1")
    suspend fun getPet(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePet(pet: PetEntity)

    @Query("SELECT * FROM habits")
    fun getAllHabitsFlow(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE ofensivaId = :ofensivaId")
    fun getHabitsForOfensivaFlow(ofensivaId: Long): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE ofensivaId = :ofensivaId")
    suspend fun getHabitsForOfensiva(ofensivaId: Long): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habit_logs")
    suspend fun getAllHabitLogs(): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getHabitLogsForDateFlow(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getHabitLogsForDate(date: String): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHabitLog(log: HabitLogEntity)

    @Query("SELECT * FROM tasks WHERE date = :date")
    fun getTasksForDateFlow(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date")
    suspend fun getTasksForDate(date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE ofensivaId = :ofensivaId AND date = :date")
    fun getTasksForOfensivaAndDateFlow(ofensivaId: Long, date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE ofensivaId = :ofensivaId AND date = :date")
    suspend fun getTasksForOfensivaAndDate(ofensivaId: Long, date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM forbidden_rules")
    fun getAllRulesFlow(): Flow<List<ForbiddenRuleEntity>>

    @Query("SELECT * FROM forbidden_rules")
    suspend fun getAllRules(): List<ForbiddenRuleEntity>

    @Query("SELECT * FROM forbidden_rules WHERE ofensivaId = :ofensivaId")
    fun getRulesForOfensivaFlow(ofensivaId: Long): Flow<List<ForbiddenRuleEntity>>

    @Query("SELECT * FROM forbidden_rules WHERE ofensivaId = :ofensivaId")
    suspend fun getRulesForOfensiva(ofensivaId: Long): List<ForbiddenRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ForbiddenRuleEntity): Long

    @Update
    suspend fun updateRule(rule: ForbiddenRuleEntity)

    @Delete
    suspend fun deleteRule(rule: ForbiddenRuleEntity)

    @Query("SELECT * FROM rule_logs")
    suspend fun getAllRuleLogs(): List<RuleLogEntity>

    @Query("SELECT * FROM rule_logs WHERE date = :date")
    fun getRuleLogsForDateFlow(date: String): Flow<List<RuleLogEntity>>

    @Query("SELECT * FROM rule_logs WHERE date = :date")
    suspend fun getRuleLogsForDate(date: String): List<RuleLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRuleLog(log: RuleLogEntity)

    @Query("SELECT * FROM death_records ORDER BY timestamp DESC")
    fun getAllDeathRecordsFlow(): Flow<List<DeathRecordEntity>>

    @Query("SELECT * FROM death_records ORDER BY timestamp DESC")
    suspend fun getAllDeathRecords(): List<DeathRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeathRecord(record: DeathRecordEntity)

    @Query("SELECT * FROM milestones ORDER BY id DESC")
    fun getAllMilestonesFlow(): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettingsEntity)

    @Query("DELETE FROM habit_logs")
    suspend fun clearHabitLogs()

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM forbidden_rules")
    suspend fun clearRules()

    @Query("DELETE FROM rule_logs")
    suspend fun clearRuleLogs()

    @Query("DELETE FROM death_records")
    suspend fun clearDeathRecords()

    @Query("DELETE FROM milestones")
    suspend fun clearMilestones()

    @Query("DELETE FROM ofensivas")
    suspend fun clearOfensivas()
}
