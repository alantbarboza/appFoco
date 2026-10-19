package com.example.appfoco.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Long = 1L,
    val selectedWidgetOfensivaId: Long = 1L,
    val hasInitialSeedBeenDone: Boolean = false,
    val simulatedDate: String? = null,
    val isDevModeActive: Boolean = false,
    val totalLostOfensivasCount: Int = 0
)
