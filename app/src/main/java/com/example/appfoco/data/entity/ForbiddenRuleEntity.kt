package com.example.appfoco.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forbidden_rules")
data class ForbiddenRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ofensivaId: Long = 0L,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true
)
