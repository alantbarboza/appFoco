package com.example.appfoco.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.appfoco.R
import com.example.appfoco.data.AppDatabase
import com.example.appfoco.data.formatToBrazilianDate
import com.example.appfoco.ui.WidgetSplashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FocoAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateWidgetForId(context, appWidgetManager, appWidgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            WidgetConfigureActivity.deleteWidgetSelection(context, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, FocoAppWidgetProvider::class.java)
            )
            CoroutineScope(Dispatchers.IO).launch {
                for (appWidgetId in appWidgetIds) {
                    updateWidgetForId(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.appfoco.ACTION_UPDATE_WIDGET"

        fun isOfensivaUsedByAnyWidget(context: Context, ofensivaId: Long): Boolean {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, FocoAppWidgetProvider::class.java)
                )
                for (widgetId in appWidgetIds) {
                    val boundId = WidgetConfigureActivity.getWidgetSelection(context, widgetId)
                    if (boundId == ofensivaId) {
                        return true
                    }
                }
            } catch (_: Exception) {
            }
            return false
        }

        suspend fun updateWidgetUI(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            for (appWidgetId in appWidgetIds) {
                updateWidgetForId(context, appWidgetManager, appWidgetId)
            }
        }

        suspend fun updateWidgetForId(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val dao = AppDatabase.getDatabase(context).appDao()
            val currentDate = formatToBrazilianDate(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))

            val ofensivas = dao.getAllOfensivas()
            val savedOfensivaId = WidgetConfigureActivity.getWidgetSelection(context, appWidgetId)
            val selectedOfensiva = ofensivas.find { it.id == savedOfensivaId } ?: ofensivas.firstOrNull()

            val views = RemoteViews(context.packageName, R.layout.widget_foco)

            if (selectedOfensiva == null) {
                views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_pending)
                views.setTextViewText(R.id.widget_fire_icon, "")
                views.setTextViewText(R.id.widget_streak_count, "")
                views.setTextViewText(R.id.widget_streak_label, "sem ofensivas\nconfiguradas")
                views.setTextColor(R.id.widget_streak_label, Color.parseColor("#B0B0B0"))
            } else {
                val freshOfensiva = dao.getOfensivaById(selectedOfensiva.id) ?: selectedOfensiva
                val habits = dao.getHabitsForOfensiva(freshOfensiva.id)
                val tasks = dao.getTasksForOfensivaAndDate(freshOfensiva.id, currentDate)
                val rules = dao.getRulesForOfensiva(freshOfensiva.id)

                val isProhibitedOnly = habits.isEmpty() && tasks.isEmpty() && rules.isNotEmpty()

                val isBurning = if (isProhibitedOnly) {
                    freshOfensiva.isAlive
                } else {
                    freshOfensiva.isAlive && freshOfensiva.lastProgressDate == currentDate
                }

                val fireEmoji = if (isBurning) "🔥" else ""
                val streakCount = freshOfensiva.streakCount
                val labelText = freshOfensiva.name

                val bgResource = if (isBurning) R.drawable.widget_bg_active else R.drawable.widget_bg_pending
                val streakTextColor = if (isBurning) Color.parseColor("#FFEA00") else Color.parseColor("#FF3D00")
                val labelTextColor = if (isBurning) Color.parseColor("#FFF3E0") else Color.parseColor("#B0B0B0")

                views.setInt(R.id.widget_root, "setBackgroundResource", bgResource)
                views.setTextViewText(R.id.widget_fire_icon, fireEmoji)
                views.setTextViewText(R.id.widget_streak_count, streakCount.toString())
                views.setTextColor(R.id.widget_streak_count, streakTextColor)
                views.setTextColor(R.id.widget_streak_label, labelTextColor)
                views.setTextViewText(R.id.widget_streak_label, labelText)
            }

            val intent = Intent(context, WidgetSplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun triggerUpdate(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, FocoAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

                if (appWidgetIds.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        for (appWidgetId in appWidgetIds) {
                            updateWidgetForId(context, appWidgetManager, appWidgetId)
                        }
                    }
                }

                val intent = Intent(context, FocoAppWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE_WIDGET
                }
                context.sendBroadcast(intent)
            } catch (_: Exception) {
            }
        }
    }
}
