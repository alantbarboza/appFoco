package com.example.appfoco.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfoco.data.AppDatabase
import com.example.appfoco.data.AppRepository
import com.example.appfoco.data.entity.AppSettingsEntity
import com.example.appfoco.data.entity.ForbiddenRuleEntity
import com.example.appfoco.data.entity.HabitEntity
import com.example.appfoco.data.entity.HabitLogEntity
import com.example.appfoco.data.entity.OfensivaEntity
import com.example.appfoco.data.entity.TaskEntity
import com.example.appfoco.widget.FocoAppWidgetProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeveloperViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    private val repository = AppRepository(dao, application)

    val ofensivas: StateFlow<List<OfensivaEntity>> = dao.getAllOfensivasFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDate: StateFlow<String> = repository.settingsFlow
        .map { settings ->
            settings?.simulatedDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

    fun setOfensivaStreak(ofensivaId: Long, streak: Int) {
        viewModelScope.launch {
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
        }
    }

    fun simulateCompleteToday(ofensivaId: Long) {
        viewModelScope.launch {
            val date = repository.getCurrentDate()
            val habits = dao.getHabitsForOfensiva(ofensivaId)
            val tasks = dao.getTasksForOfensivaAndDate(ofensivaId, date)

            habits.forEach { h ->
                dao.insertOrUpdateHabitLog(HabitLogEntity(habitId = h.id, date = date, isCompleted = true))
            }
            tasks.forEach { t ->
                dao.updateTask(t.copy(isCompleted = true))
            }

            val ofensiva = dao.getOfensivaById(ofensivaId)
            if (ofensiva != null) {
                val newStreak = ofensiva.streakCount + 1
                dao.insertOrUpdateOfensiva(
                    ofensiva.copy(
                        streakCount = newStreak,
                        lastProgressDate = date,
                        isAlive = true
                    )
                )
            }
            FocoAppWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun simulateUncompleteToday(ofensivaId: Long) {
        viewModelScope.launch {
            val date = repository.getCurrentDate()
            val habits = dao.getHabitsForOfensiva(ofensivaId)
            val tasks = dao.getTasksForOfensivaAndDate(ofensivaId, date)

            habits.forEach { h ->
                dao.insertOrUpdateHabitLog(HabitLogEntity(habitId = h.id, date = date, isCompleted = false))
            }
            tasks.forEach { t ->
                dao.updateTask(t.copy(isCompleted = false))
            }

            val ofensiva = dao.getOfensivaById(ofensivaId)
            if (ofensiva != null && ofensiva.lastProgressDate == date) {
                val revoked = maxOf(0, ofensiva.streakCount - 1)
                dao.insertOrUpdateOfensiva(
                    ofensiva.copy(
                        streakCount = revoked,
                        lastProgressDate = null
                    )
                )
            }
            FocoAppWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun zeroOfensiva(ofensivaId: Long) {
        viewModelScope.launch {
            val ofensiva = dao.getOfensivaById(ofensivaId) ?: return@launch
            dao.insertOrUpdateOfensiva(
                ofensiva.copy(
                    streakCount = 0,
                    isAlive = false,
                    lastProgressDate = null
                )
            )
            FocoAppWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun reigniteOfensiva(ofensivaId: Long) {
        viewModelScope.launch {
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
        }
    }

    fun advanceDays(days: Int) {
        viewModelScope.launch {
            repository.devAdvanceDays(days)
        }
    }

    fun generateFakeTestData() {
        viewModelScope.launch {
            repository.resetAllData()
            val date = repository.getCurrentDate()

            val o1 = dao.insertOrUpdateOfensiva(
                OfensivaEntity(name = "Produtividade Diária", streakCount = 5, lastProgressDate = date, isAlive = true)
            )
            dao.insertHabit(HabitEntity(ofensivaId = o1, name = "Estudar Kotlin 30min", description = "Prática no IDE"))
            dao.insertHabit(HabitEntity(ofensivaId = o1, name = "Beber 2L de água", description = "Hidratação"))
            dao.insertTask(TaskEntity(ofensivaId = o1, name = "Entregar relatório do app", date = date, isCompleted = true))

            val o2 = dao.insertOrUpdateOfensiva(
                OfensivaEntity(name = "Saúde & Exercícios", streakCount = 12, lastProgressDate = date, isAlive = true)
            )
            dao.insertHabit(HabitEntity(ofensivaId = o2, name = "Caminhada de 5km", description = "Manhã"))
            dao.insertHabit(HabitEntity(ofensivaId = o2, name = "Alimentação saudável", description = "Sem açúcar"))

            dao.insertRule(ForbiddenRuleEntity(name = "Procrastinar nas redes sociais", description = "Mais de 2h no Instagram", isActive = true))

            val settings = dao.getSettings() ?: AppSettingsEntity()
            dao.insertOrUpdateSettings(settings.copy(hasInitialSeedBeenDone = true))
            FocoAppWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }
}
