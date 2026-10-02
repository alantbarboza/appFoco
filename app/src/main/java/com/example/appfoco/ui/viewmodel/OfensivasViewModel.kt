package com.example.appfoco.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfoco.data.AppDatabase
import com.example.appfoco.data.AppRepository
import com.example.appfoco.data.entity.AppSettingsEntity
import com.example.appfoco.data.entity.ForbiddenRuleEntity
import com.example.appfoco.data.entity.HabitEntity
import com.example.appfoco.data.entity.HabitLogEntity
import com.example.appfoco.data.entity.OfensivaEntity
import com.example.appfoco.data.entity.RuleLogEntity
import com.example.appfoco.data.entity.TaskEntity
import com.example.appfoco.data.formatToBrazilianDate
import com.example.appfoco.utils.BackupData
import com.example.appfoco.utils.BackupManager
import com.example.appfoco.widget.FocoAppWidgetProvider
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class OfensivasViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    private val repository = AppRepository(dao, application)

    private val prefs = application.getSharedPreferences("app_foco_dev_prefs", Context.MODE_PRIVATE)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _ofensivas = MutableStateFlow<List<OfensivaEntity>>(emptyList())
    val ofensivas: StateFlow<List<OfensivaEntity>> = _ofensivas

    val settings: StateFlow<AppSettingsEntity?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentDate: StateFlow<String> = repository.settingsFlow
        .map { settings ->
            val rawDate = if (settings?.isDevModeActive == true && !settings.simulatedDate.isNullOrBlank()) {
                settings.simulatedDate
            } else {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            }
            formatToBrazilianDate(rawDate)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, formatToBrazilianDate(null))

    val maxHighestStreak: StateFlow<Int> = ofensivas
        .map { list -> list.maxOfOrNull { it.highestStreak } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val extinguishedCount: StateFlow<Int> = repository.settingsFlow
        .map { it?.totalLostOfensivasCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _editingOfensivaId = MutableStateFlow<Long?>(null)
    val editingOfensivaId: StateFlow<Long?> = _editingOfensivaId

    private val _habitsForEditing = MutableStateFlow<List<HabitEntity>>(emptyList())
    val habitsForEditing: StateFlow<List<HabitEntity>> = _habitsForEditing

    private val _tasksForEditing = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasksForEditing: StateFlow<List<TaskEntity>> = _tasksForEditing

    private val _rulesForEditing = MutableStateFlow<List<ForbiddenRuleEntity>>(emptyList())
    val rulesForEditing: StateFlow<List<ForbiddenRuleEntity>> = _rulesForEditing

    private val _habitLogs = MutableStateFlow<List<HabitLogEntity>>(emptyList())
    val habitLogs: StateFlow<List<HabitLogEntity>> = _habitLogs

    private val _ruleLogs = MutableStateFlow<List<RuleLogEntity>>(emptyList())
    val ruleLogs: StateFlow<List<RuleLogEntity>> = _ruleLogs

    private val _allHabitLogs = MutableStateFlow<List<HabitLogEntity>>(emptyList())
    val allHabitLogs: StateFlow<List<HabitLogEntity>> = _allHabitLogs

    private val _allTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val allTasks: StateFlow<List<TaskEntity>> = _allTasks

    private val _allRuleLogs = MutableStateFlow<List<RuleLogEntity>>(emptyList())
    val allRuleLogs: StateFlow<List<RuleLogEntity>> = _allRuleLogs

    init {
        viewModelScope.launch {
            try {
                val settings = dao.getSettings()
                if (settings == null) {
                    dao.insertOrUpdateSettings(AppSettingsEntity(id = 1L))
                }

                launch {
                    try {
                        refreshHistoryData()
                    } catch (_: Exception) {
                    }
                }

                launch {
                    dao.getAllOfensivasFlow().collect {
                        _ofensivas.value = it
                        _isLoading.value = false
                    }
                }
                launch {
                    currentDate.flatMapLatest { date ->
                        dao.getHabitLogsForDateFlow(date)
                    }.collect { _habitLogs.value = it }
                }
                launch {
                    currentDate.flatMapLatest { date ->
                        dao.getRuleLogsForDateFlow(date)
                    }.collect { _ruleLogs.value = it }
                }
                launch {
                    _editingOfensivaId.flatMapLatest { id ->
                        if (id == null) flowOf(emptyList())
                        else dao.getHabitsForOfensivaFlow(id)
                    }.collect { _habitsForEditing.value = it }
                }
                launch {
                    _editingOfensivaId.flatMapLatest { id ->
                        val date = currentDate.value
                        if (id == null) flowOf(emptyList())
                        else dao.getTasksForOfensivaAndDateFlow(id, date)
                    }.collect { _tasksForEditing.value = it }
                }
                launch {
                    _editingOfensivaId.flatMapLatest { id ->
                        if (id == null) flowOf(emptyList())
                        else dao.getRulesForOfensivaFlow(id)
                    }.collect { _rulesForEditing.value = it }
                }
            } catch (_: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun setDevModeActive(isActive: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settings = dao.getSettings() ?: AppSettingsEntity()
                if (isActive) {
                    val backupData = BackupData(
                        ofensivas = dao.getAllOfensivas(),
                        habits = dao.getAllHabits().filter { !it.name.contains("estudar 30", ignoreCase = true) },
                        habitLogs = dao.getAllHabitLogs(),
                        tasks = dao.getAllTasks().filter { !it.name.contains("estudar 30", ignoreCase = true) },
                        rules = dao.getAllRules(),
                        ruleLogs = dao.getAllRuleLogs(),
                        settings = settings.copy(isDevModeActive = false)
                    )
                    val json = Gson().toJson(backupData)
                    prefs.edit().putString("real_data_snapshot", json).apply()
                    dao.insertOrUpdateSettings(settings.copy(isDevModeActive = true))
                } else {
                    val savedJson = prefs.getString("real_data_snapshot", null)
                    if (!savedJson.isNullOrBlank()) {
                        BackupManager.importBackupFromJsonString(getApplication(), savedJson)
                        prefs.edit().remove("real_data_snapshot").apply()
                    }
                    val currentSettings = dao.getSettings() ?: AppSettingsEntity()
                    dao.insertOrUpdateSettings(currentSettings.copy(isDevModeActive = false, simulatedDate = null))
                }
                FocoAppWidgetProvider.triggerUpdate(getApplication())
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun refreshHistoryData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _allHabitLogs.value = dao.getAllHabitLogs()
                _allTasks.value = dao.getAllTasks().filter { !it.name.contains("estudar 30", ignoreCase = true) }
                _allRuleLogs.value = dao.getAllRuleLogs()
                _ofensivas.value = dao.getAllOfensivas()
            } catch (_: Exception) {
            }
        }
    }

    fun setEditingOfensivaId(id: Long?) {
        _editingOfensivaId.value = id
    }

    fun advanceDays(days: Int) {
        viewModelScope.launch {
            try {
                repository.devAdvanceDays(days)
                dao.getAllOfensivas().forEach { evaluateOfensivaProgress(it.id) }
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun createOfensiva(name: String, initialItemName: String, type: String) {
        viewModelScope.launch {
            try {
                if (name.isBlank() || initialItemName.isBlank()) return@launch
                val date = repository.getCurrentDate()
                val ofensivaId = dao.insertOrUpdateOfensiva(OfensivaEntity(name = name, lastActiveDate = date))
                when (type) {
                    "Hábito" -> dao.insertHabit(HabitEntity(ofensivaId = ofensivaId, name = initialItemName, daysOfWeek = "1,2,3,4,5,6,7"))
                    "Tarefa" -> dao.insertTask(TaskEntity(ofensivaId = ofensivaId, name = initialItemName, date = date))
                    "Proibido" -> dao.insertRule(ForbiddenRuleEntity(ofensivaId = ofensivaId, name = initialItemName))
                }
                evaluateOfensivaProgress(ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun updateOfensivaName(ofensiva: OfensivaEntity, newName: String) {
        viewModelScope.launch {
            try {
                dao.insertOrUpdateOfensiva(ofensiva.copy(name = newName))
                FocoAppWidgetProvider.triggerUpdate(getApplication())
            } catch (_: Exception) {
            }
        }
    }

    fun deleteOfensiva(ofensiva: OfensivaEntity) {
        viewModelScope.launch {
            try {
                dao.deleteOfensiva(ofensiva)
                FocoAppWidgetProvider.triggerUpdate(getApplication())
            } catch (_: Exception) {
            }
        }
    }

    fun addHabit(ofensivaId: Long, name: String, description: String?, daysOfWeek: String) {
        viewModelScope.launch {
            try {
                dao.insertHabit(HabitEntity(ofensivaId = ofensivaId, name = name, description = description, daysOfWeek = daysOfWeek))
                evaluateOfensivaProgress(ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            try {
                dao.insertHabit(habit)
                evaluateOfensivaProgress(habit.ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            try {
                dao.deleteHabit(habit)
                evaluateOfensivaProgress(habit.ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun toggleHabitLog(habit: HabitEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val date = currentDate.value
                dao.insertOrUpdateHabitLog(HabitLogEntity(habitId = habit.id, date = date, isCompleted = isCompleted))
                evaluateOfensivaProgress(habit.ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun addTask(ofensivaId: Long, name: String, description: String?) {
        viewModelScope.launch {
            try {
                val date = currentDate.value
                dao.insertTask(TaskEntity(ofensivaId = ofensivaId, name = name, description = description, date = date))
                evaluateOfensivaProgress(ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                dao.updateTask(task)
                evaluateOfensivaProgress(task.ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun toggleTask(task: TaskEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                dao.updateTask(task.copy(isCompleted = isCompleted))
                evaluateOfensivaProgress(task.ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                dao.deleteTask(task)
                evaluateOfensivaProgress(task.ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun addRule(ofensivaId: Long, name: String, description: String?) {
        viewModelScope.launch {
            try {
                dao.insertRule(ForbiddenRuleEntity(ofensivaId = ofensivaId, name = name, description = description))
                evaluateOfensivaProgress(ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun updateRule(rule: ForbiddenRuleEntity) {
        viewModelScope.launch {
            try {
                dao.updateRule(rule)
                evaluateOfensivaProgress(rule.ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun deleteRule(rule: ForbiddenRuleEntity) {
        viewModelScope.launch {
            try {
                dao.deleteRule(rule)
                evaluateOfensivaProgress(rule.ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun toggleRuleLog(rule: ForbiddenRuleEntity, isBroken: Boolean) {
        viewModelScope.launch {
            try {
                val date = currentDate.value
                dao.insertOrUpdateRuleLog(RuleLogEntity(ruleId = rule.id, date = date, isBroken = isBroken))
                evaluateOfensivaProgress(rule.ofensivaId)
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun evaluateOfensivaProgress(ofensivaId: Long) {
        viewModelScope.launch {
            try {
                val ofensiva = dao.getOfensivaById(ofensivaId) ?: return@launch
                val date = currentDate.value

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val currentDateObj = try {
                    sdf.parse(date)
                } catch (_: Exception) {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                } ?: Date()

                val cal = Calendar.getInstance().apply { time = currentDateObj }
                val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK).toString()

                val allHabits = dao.getHabitsForOfensiva(ofensivaId)
                val habits = allHabits.filter { habit ->
                    val daysList = habit.daysOfWeek.split(",")
                    daysList.contains(todayDayOfWeek)
                }

                val logs = dao.getHabitLogsForDate(date)
                val tasks = dao.getTasksForOfensivaAndDate(ofensivaId, date)
                val rules = dao.getRulesForOfensiva(ofensivaId)
                val ruleLogs = dao.getRuleLogsForDate(date)

                val anyRuleBroken = rules.any { r -> ruleLogs.any { it.ruleId == r.id && it.isBroken } }

                if (anyRuleBroken) {
                    // Prohibited Rule broken during current day: turns off fire and zeros streak for today.
                    // (Perca de ofensiva +1 ONLY increments when 23:59 passes into new day!)
                    val updated = ofensiva.copy(
                        streakCount = 0,
                        isAlive = false,
                        lastProgressDate = null
                    )
                    dao.insertOrUpdateOfensiva(updated)
                } else {
                    val hasItems = habits.isNotEmpty() || tasks.isNotEmpty() || rules.isNotEmpty()
                    val allHabitsDone = habits.isEmpty() || habits.all { h -> logs.any { it.habitId == h.id && it.isCompleted } }
                    val allTasksDone = tasks.isEmpty() || tasks.all { it.isCompleted }
                    val allDone = hasItems && allHabitsDone && allTasksDone

                    if (allDone) {
                        if (ofensiva.lastProgressDate != date) {
                            val newStreak = ofensiva.streakCount + 1
                            val updated = ofensiva.copy(
                                streakCount = newStreak,
                                lastProgressDate = date,
                                lastActiveDate = date,
                                highestStreak = maxOf(ofensiva.highestStreak, newStreak),
                                isAlive = true
                            )
                            dao.insertOrUpdateOfensiva(updated)
                        }
                    } else {
                        if (ofensiva.lastProgressDate == date) {
                            val revokedStreak = maxOf(0, ofensiva.streakCount - 1)
                            val updated = ofensiva.copy(
                                streakCount = revokedStreak,
                                lastProgressDate = null,
                                isAlive = false
                            )
                            dao.insertOrUpdateOfensiva(updated)
                        } else {
                            dao.insertOrUpdateOfensiva(ofensiva.copy(isAlive = false, lastActiveDate = date))
                        }
                    }
                }
                FocoAppWidgetProvider.triggerUpdate(getApplication())
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun setOfensivaStreak(ofensivaId: Long, streak: Int) {
        viewModelScope.launch {
            try {
                val ofensiva = dao.getOfensivaById(ofensivaId) ?: return@launch
                val date = repository.getCurrentDate()
                val clamped = maxOf(0, streak)
                val updated = ofensiva.copy(
                    streakCount = clamped,
                    highestStreak = maxOf(ofensiva.highestStreak, clamped),
                    isAlive = true,
                    lastActiveDate = date,
                    lastProgressDate = if (clamped > 0) date else null
                )
                dao.insertOrUpdateOfensiva(updated)
                FocoAppWidgetProvider.triggerUpdate(getApplication())
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun reigniteOfensiva(ofensivaId: Long) {
        viewModelScope.launch {
            try {
                val ofensiva = dao.getOfensivaById(ofensivaId) ?: return@launch
                val date = repository.getCurrentDate()
                dao.insertOrUpdateOfensiva(
                    ofensiva.copy(
                        streakCount = 1,
                        isAlive = true,
                        lastActiveDate = date,
                        lastProgressDate = date
                    )
                )
                FocoAppWidgetProvider.triggerUpdate(getApplication())
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun zeroOfensiva(ofensivaId: Long) {
        viewModelScope.launch {
            try {
                val ofensiva = dao.getOfensivaById(ofensivaId) ?: return@launch
                dao.insertOrUpdateOfensiva(
                    ofensiva.copy(
                        streakCount = 0,
                        isAlive = false,
                        lastProgressDate = null
                    )
                )
                FocoAppWidgetProvider.triggerUpdate(getApplication())
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            try {
                repository.resetAllData()
                dao.clearRuleLogs()
                prefs.edit().remove("real_data_snapshot").apply()
                refreshHistoryData()
            } catch (_: Exception) {
            }
        }
    }
}
