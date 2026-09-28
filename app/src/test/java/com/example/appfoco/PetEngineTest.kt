package com.example.appfoco

import com.example.appfoco.data.entity.PetEntity
import com.example.appfoco.domain.PetEngine
import org.junit.Assert.*
import org.junit.Test

class PetEngineTest {

    @Test
    fun testPetStartsAtLevel1() {
        val pet = PetEntity()
        assertEquals(1, pet.level)
        assertTrue(pet.isAlive)
    }

    @Test
    fun testGainLevelOncePerDay() {
        val currentDate = "2023-10-01"
        val (level1, gained1) = PetEngine.calculateNewLevel(1, null, currentDate)
        assertEquals(2, level1)
        assertTrue(gained1)

        // Try gaining again on the same day
        val (level2, gained2) = PetEngine.calculateNewLevel(level1, currentDate, currentDate)
        assertEquals(2, level2)
        assertFalse(gained2)
    }

    @Test
    fun testMaxLevelCap100() {
        val currentDate = "2023-10-01"
        val (level, gained) = PetEngine.calculateNewLevel(100, "2023-09-30", currentDate)
        assertEquals(100, level)
        assertFalse(gained)
    }

    @Test
    fun testPetPhases() {
        assertEquals("Centelha (Nível 1-9)", PetEngine.getPetPhase(1))
        assertEquals("Centelha (Nível 1-9)", PetEngine.getPetPhase(9))
        assertEquals("Centelha Forte (Nível 10-19)", PetEngine.getPetPhase(10))
        assertEquals("Fogo Supremo (Nível 100)", PetEngine.getPetPhase(100))
    }

    @Test
    fun testSurvivalCheck() {
        val pet = PetEntity(level = 5, lastActiveDate = "2023-10-01", isAlive = true)
        // Same day or next day is alive
        assertTrue(PetEngine.checkSurvival(pet, "2023-10-01"))
        assertTrue(PetEngine.checkSurvival(pet, "2023-10-02"))
        // 2 days later without activity means death
        assertFalse(PetEngine.checkSurvival(pet, "2023-10-03"))
    }
}
