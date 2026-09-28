package com.example.appfoco.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.example.appfoco.data.AppDatabase
import com.example.appfoco.data.entity.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupData(
    val ofensivas: List<OfensivaEntity>,
    val habits: List<HabitEntity>,
    val habitLogs: List<HabitLogEntity>,
    val tasks: List<TaskEntity>,
    val rules: List<ForbiddenRuleEntity>,
    val ruleLogs: List<RuleLogEntity>,
    val settings: AppSettingsEntity?
)

object BackupManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportBackup(context: Context): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getDatabase(context).appDao()
            val cleanHabits = dao.getAllHabits().filter { !it.name.contains("estudar 30", ignoreCase = true) }
            val cleanTasks = dao.getAllTasks().filter { !it.name.contains("estudar 30", ignoreCase = true) }

            val backupData = BackupData(
                ofensivas = dao.getAllOfensivas(),
                habits = cleanHabits,
                habitLogs = dao.getAllHabitLogs(),
                tasks = cleanTasks,
                rules = dao.getAllRules(),
                ruleLogs = dao.getAllRuleLogs(),
                settings = dao.getSettings()
            )

            val jsonString = gson.toJson(backupData)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val timeStamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(Date())
            val file = File(downloadsDir, "backup_appFoco_$timeStamp.txt")
            FileOutputStream(file).use { it.write(jsonString.toByteArray()) }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("text/plain"),
                null
            )

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentJsonData(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getDatabase(context).appDao()
            val cleanHabits = dao.getAllHabits().filter { !it.name.contains("estudar 30", ignoreCase = true) }
            val cleanTasks = dao.getAllTasks().filter { !it.name.contains("estudar 30", ignoreCase = true) }

            val backupData = BackupData(
                ofensivas = dao.getAllOfensivas(),
                habits = cleanHabits,
                habitLogs = dao.getAllHabitLogs(),
                tasks = cleanTasks,
                rules = dao.getAllRules(),
                ruleLogs = dao.getAllRuleLogs(),
                settings = dao.getSettings()
            )
            gson.toJson(backupData)
        } catch (e: Exception) {
            "{}"
        }
    }

    suspend fun importBackupFromJsonString(context: Context, jsonString: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (jsonString.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("JSON vazio."))
            }
            val backupData = try {
                gson.fromJson(jsonString, BackupData::class.java)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("JSON inválido: ${e.message}"))
            }

            if (backupData == null) {
                return@withContext Result.failure(Exception("Estrutura do backup vazia ou inválida."))
            }

            val dao = AppDatabase.getDatabase(context).appDao()
            dao.clearHabitLogs()
            dao.clearHabits()
            dao.clearTasks()
            dao.clearRules()
            dao.clearRuleLogs()
            dao.clearDeathRecords()
            dao.clearMilestones()
            dao.clearOfensivas()

            backupData.ofensivas.forEach { dao.insertOrUpdateOfensiva(it) }
            backupData.habits
                .filter { !it.name.contains("estudar 30", ignoreCase = true) }
                .forEach { dao.insertHabit(it) }
            backupData.habitLogs.forEach { dao.insertOrUpdateHabitLog(it) }
            backupData.tasks
                .filter { !it.name.contains("estudar 30", ignoreCase = true) }
                .forEach { dao.insertTask(it) }
            backupData.rules.forEach { dao.insertRule(it) }
            backupData.ruleLogs.forEach { dao.insertOrUpdateRuleLog(it) }
            if (backupData.settings != null) {
                dao.insertOrUpdateSettings(backupData.settings)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getJsonTemplate(): String {
        val sample = BackupData(
            ofensivas = listOf(OfensivaEntity(id = 1, name = "Ofensiva Exemplo", streakCount = 1, highestStreak = 1, isAlive = true)),
            habits = listOf(HabitEntity(id = 1, ofensivaId = 1, name = "Exercício Físico", daysOfWeek = "1,2,3,4,5,6,7")),
            habitLogs = emptyList(),
            tasks = listOf(TaskEntity(id = 1, ofensivaId = 1, name = "Ler 10 páginas", date = "14/05/2025", isCompleted = false)),
            rules = listOf(ForbiddenRuleEntity(id = 1, name = "Usar celular na cama")),
            ruleLogs = emptyList(),
            settings = AppSettingsEntity(id = 1)
        )
        return gson.toJson(sample)
    }
}
