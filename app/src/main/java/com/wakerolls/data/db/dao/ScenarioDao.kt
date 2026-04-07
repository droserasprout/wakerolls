package com.wakerolls.data.db.dao

import androidx.room.*
import com.wakerolls.data.db.entity.ScenarioEntity
import com.wakerolls.data.db.entity.ScenarioSlotEntity
import com.wakerolls.data.db.entity.ScenarioWithSlots
import kotlinx.coroutines.flow.Flow

@Dao
interface ScenarioDao {
    @Transaction
    @Query("SELECT * FROM scenarios ORDER BY name")
    fun observeAll(): Flow<List<ScenarioWithSlots>>

    @Transaction
    @Query("SELECT * FROM scenarios WHERE id = :id")
    fun observeById(id: Long): Flow<ScenarioWithSlots?>

    @Insert
    suspend fun insertScenario(scenario: ScenarioEntity): Long

    @Insert
    suspend fun insertSlots(slots: List<ScenarioSlotEntity>)

    @Update
    suspend fun updateScenario(scenario: ScenarioEntity)

    @Query("DELETE FROM scenario_slots WHERE scenarioId = :scenarioId")
    suspend fun deleteSlotsByScenarioId(scenarioId: Long)

    @Query("DELETE FROM scenarios WHERE id = :id")
    suspend fun deleteScenario(id: Long)

    @Query("SELECT COUNT(*) FROM scenarios")
    suspend fun count(): Int
}
