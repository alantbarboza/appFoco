package com.example.appfoco.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.appfoco.R
import com.example.appfoco.ui.screens.EditOfensivaScreen
import com.example.appfoco.ui.screens.OfensivasScreen
import com.example.appfoco.ui.screens.SettingsScreen
import com.example.appfoco.ui.theme.AppFocoTheme
import com.example.appfoco.ui.viewmodel.OfensivasViewModel

class MainActivity : ComponentActivity() {

    private val ofensivasViewModel: OfensivasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppFocoTheme {
                var currentTab by rememberSaveable { mutableStateOf("ofensivas") }
                var isEditing by rememberSaveable { mutableStateOf(false) }

                BackHandler(enabled = isEditing || currentTab != "ofensivas") {
                    if (isEditing) {
                        isEditing = false
                    } else if (currentTab != "ofensivas") {
                        currentTab = "ofensivas"
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (!isEditing) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(painterResource(id = R.drawable.ic_fire_notification), contentDescription = "Ofensivas") },
                                    label = { Text("Ofensivas") },
                                    selected = currentTab == "ofensivas",
                                    onClick = { currentTab = "ofensivas" },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configurações") },
                                    label = { Text("Configurações") },
                                    selected = currentTab == "settings",
                                    onClick = { currentTab = "settings" },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isEditing) {
                            EditOfensivaScreen(
                                viewModel = ofensivasViewModel,
                                onBack = { isEditing = false }
                            )
                        } else {
                            when (currentTab) {
                                "ofensivas" -> OfensivasScreen(
                                    viewModel = ofensivasViewModel,
                                    onNavigateToEdit = { isEditing = true }
                                )
                                "settings" -> SettingsScreen(
                                    viewModel = ofensivasViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
