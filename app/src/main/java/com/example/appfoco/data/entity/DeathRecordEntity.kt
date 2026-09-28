package com.example.appfoco.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "death_records")
data class DeathRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long,
    val date: String,
    val reason: String,
    val levelAtDeath: Int
)
