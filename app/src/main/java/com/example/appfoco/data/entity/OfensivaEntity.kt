package com.example.appfoco.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ofensivas")
data class OfensivaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val streakCount: Int = 0,
    val lastProgressDate: String? = null,
    val lastActiveDate: String? = null,
    val isAlive: Boolean = true,
    val highestStreak: Int = 0
)
