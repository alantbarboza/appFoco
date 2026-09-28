package com.example.appfoco.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet")
data class PetEntity(
    @PrimaryKey
    val id: Long = 1L,
    val level: Int = 1,
    val energy: Int = 100,
    val streakCount: Int = 0,
    val lastProgressDate: String? = null,
    val lastActiveDate: String? = null,
    val isAlive: Boolean = true,
    val highestLevel: Int = 1,
    val highestStreak: Int = 0,
    val deathCount: Int = 0
)
