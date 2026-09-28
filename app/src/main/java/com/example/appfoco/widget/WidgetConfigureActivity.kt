package com.example.appfoco.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appfoco.data.AppDatabase
import com.example.appfoco.data.entity.OfensivaEntity
import com.example.appfoco.ui.theme.AppFocoTheme
import com.example.appfoco.ui.theme.DarkCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val dao = AppDatabase.getDatabase(this).appDao()
        var ofensivas by mutableStateOf<List<OfensivaEntity>>(emptyList())

        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            ofensivas = withContext(Dispatchers.IO) {
                dao.getAllOfensivas()
            }
        }

        setContent {
            AppFocoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Escolha a Ofensiva para este Widget",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Selecione qual ofensiva este widget irá monitorar:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (ofensivas.isEmpty()) {
                            Text("Nenhuma ofensiva cadastrada. Crie uma no aplicativo primeiro.", color = MaterialTheme.colorScheme.error)
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(ofensivas) { ofensiva ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                saveWidgetSelection(this@WidgetConfigureActivity, appWidgetId, ofensiva.id)

                                                val appWidgetManager = AppWidgetManager.getInstance(this@WidgetConfigureActivity)
                                                scope.launch {
                                                    withContext(Dispatchers.IO) {
                                                        FocoAppWidgetProvider.updateWidgetForId(this@WidgetConfigureActivity, appWidgetManager, appWidgetId)
                                                    }

                                                    val resultValue = Intent().apply {
                                                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                                    }
                                                    setResult(RESULT_OK, resultValue)
                                                    finish()
                                                }
                                            },
                                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(ofensiva.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("Ofensiva: ${ofensiva.streakCount} dias", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "com.example.appfoco.widget.WidgetPrefs"
        private const val PREF_PREFIX_KEY = "widget_ofensiva_id_"

        fun saveWidgetSelection(context: Context, appWidgetId: Int, ofensivaId: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
            prefs.putLong(PREF_PREFIX_KEY + appWidgetId, ofensivaId)
            prefs.apply()
        }

        fun getWidgetSelection(context: Context, appWidgetId: Int): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            return prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1L)
        }

        fun deleteWidgetSelection(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}
