package com.example.appfoco.utils

import android.content.Context
import androidx.work.*
import com.example.appfoco.data.AppDatabase
import com.example.appfoco.data.AppRepository
import com.example.appfoco.data.formatToBrazilianDate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getDatabase(applicationContext).appDao()
        val repository = AppRepository(dao, applicationContext)
        val allOfensivas = dao.getAllOfensivas()

        if (allOfensivas.isNotEmpty()) {
            val rawDate = repository.getCurrentDate()
            val currentDate = formatToBrazilianDate(rawDate)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDateObj = try {
                sdf.parse(currentDate)
            } catch (_: Exception) {
                Date()
            } ?: Date()

            val cal = Calendar.getInstance().apply { time = currentDateObj }
            val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK).toString()

            val logs = dao.getHabitLogsForDate(currentDate)

            var hasPending = false
            for (of in allOfensivas) {
                val habits = dao.getHabitsForOfensiva(of.id).filter { h ->
                    h.daysOfWeek.split(",").contains(todayDayOfWeek)
                }
                val tasks = dao.getTasksForOfensivaAndDate(of.id, currentDate)

                val pendingHabits = habits.any { h -> logs.none { it.habitId == h.id && it.isCompleted } }
                val pendingTasks = tasks.any { !it.isCompleted }

                if (pendingHabits || pendingTasks) {
                    hasPending = true
                    break
                }
            }

            if (hasPending) {
                NotificationHelper.sendNotification(
                    applicationContext,
                    "🔥 Ofensiva",
                    "Faça os seus hábitos e tarefas configurados até às 23:59 de hoje para salvar a sua ofensiva."
                )
            }
        }
        return Result.success()
    }

    companion object {
        fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
            val calendar = Calendar.getInstance()
            val now = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)

            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val initialDelay = calendar.timeInMillis - now.timeInMillis

            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "app_foco_reminder_work",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
        }

        fun cancelReminder(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("app_foco_reminder_work")
        }
    }
}
