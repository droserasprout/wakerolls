package com.wakerolls.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.wakerolls.domain.model.Scenario
import com.wakerolls.domain.model.ScenarioSlot

data class ScenarioWithSlots(
    @Embedded val scenario: ScenarioEntity,
    @Relation(parentColumn = "id", entityColumn = "scenarioId")
    val slots: List<ScenarioSlotEntity>,
) {
    fun toDomain() = Scenario(
        id = scenario.id,
        name = scenario.name,
        slots = slots.map {
            ScenarioSlot(id = it.id, category = it.category, count = it.count)
        },
    )
}
