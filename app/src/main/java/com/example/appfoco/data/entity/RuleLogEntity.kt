package com.example.appfoco.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rule_logs",
    indices = [Index(value = ["ruleId", "date"], unique = true)]
)
data class RuleLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ruleId: Long,
    val date: String,
    val isBroken: Boolean
)
