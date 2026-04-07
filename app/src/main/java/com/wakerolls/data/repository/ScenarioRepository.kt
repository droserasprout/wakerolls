package com.wakerolls.data.repository

import com.wakerolls.data.db.dao.ScenarioDao
import com.wakerolls.data.db.entity.ScenarioEntity
import com.wakerolls.data.db.entity.ScenarioSlotEntity
import com.wakerolls.domain.model.Scenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScenarioRepository @Inject constructor(private val dao: ScenarioDao) {

    fun observeAll(): Flow<List<Scenario>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeById(id: Long): Flow<Scenario?> =
        dao.observeById(id).map { it?.toDomain() }

    suspend fun save(scenario: Scenario): Long {
        val scenarioId = if (scenario.id == 0L) {
            dao.insertScenario(ScenarioEntity(name = scenario.name))
        } else {
            dao.updateScenario(ScenarioEntity(id = scenario.id, name = scenario.name))
            dao.deleteSlotsByScenarioId(scenario.id)
            scenario.id
        }
        dao.insertSlots(scenario.slots.map {
            ScenarioSlotEntity(scenarioId = scenarioId, category = it.category, count = it.count)
        })
        return scenarioId
    }

    suspend fun getAll(): List<Scenario> = dao.getAll().map { it.toDomain() }

    suspend fun delete(id: Long) {
        dao.deleteScenario(id)
    }

    suspend fun deleteAll() {
        dao.deleteAllSlots()
        dao.deleteAllScenarios()
    }

    suspend fun seedIfEmpty(scenarios: List<Scenario>) {
        if (dao.count() == 0) {
            scenarios.forEach { save(it) }
        }
    }
}
