package com.example.appfoco.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ofensivaId: Long = 1L,
    val name: String,
    val description: String? = null,
    val date: String,
    val time: String? = null,
    val isCompleted: Boolean = false
)
