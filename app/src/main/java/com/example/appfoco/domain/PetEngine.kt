package com.example.appfoco.domain

import com.example.appfoco.data.entity.PetEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object PetEngine {

    fun getPetPhase(level: Int): String {
        return when {
            level >= 100 -> "Fogo Supremo (Nível 100)"
            level >= 90 -> "Chama Voraz (Nível 90-99)"
            level >= 80 -> "Fogo Intenso (Nível 80-89)"
            level >= 70 -> "Chama Brilhante (Nível 70-79)"
            level >= 60 -> "Fogo Forte (Nível 60-69)"
            level >= 50 -> "Chama Estável (Nível 50-59)"
            level >= 40 -> "Fogo Crescente (Nível 40-49)"
            level >= 30 -> "Chama Atenta (Nível 30-39)"
            level >= 20 -> "Fogo Nascente (Nível 20-29)"
            level >= 10 -> "Centelha Forte (Nível 10-19)"
            else -> "Centelha (Nível 1-9)"
        }
    }

    fun calculateNewLevel(currentLevel: Int, lastProgressDate: String?, currentDate: String): Pair<Int, Boolean> {
        if (currentLevel >= 100) {
            return 100 to false // Max level 100, no +1
        }
        if (lastProgressDate == currentDate) {
            return currentLevel to false // Already gained level today
        }
        val newLevel = minOf(100, currentLevel + 1)
        return newLevel to true
    }

    fun checkSurvival(pet: PetEntity, currentDateStr: String): Boolean {
        if (!pet.isAlive) return false
        val lastActive = pet.lastActiveDate ?: currentDateStr
        try {
            val last = LocalDate.parse(lastActive, DateTimeFormatter.ISO_DATE)
            val current = LocalDate.parse(currentDateStr, DateTimeFormatter.ISO_DATE)
            val daysBetween = ChronoUnit.DAYS.between(last, current)
            // If more than 1 day passed without activity, pet dies
            if (daysBetween > 1) {
                return false
            }
        } catch (e: Exception) {
            // fallback
        }
        return true
    }
}
