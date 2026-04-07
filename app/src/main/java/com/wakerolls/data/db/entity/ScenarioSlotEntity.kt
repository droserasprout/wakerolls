package com.wakerolls.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wakerolls.domain.model.Category

@Entity(
    tableName = "scenario_slots",
    foreignKeys = [ForeignKey(
        entity = ScenarioEntity::class,
        parentColumns = ["id"],
        childColumns = ["scenarioId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("scenarioId")],
)
data class ScenarioSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: Long,
    val category: Category,
    val count: Int,
)
