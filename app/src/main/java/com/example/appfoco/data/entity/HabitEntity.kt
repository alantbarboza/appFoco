package com.example.appfoco.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ofensivaId: Long = 1L,
    val name: String,
    val description: String? = null,
    val time: String? = null,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
    val isActive: Boolean = true
)
