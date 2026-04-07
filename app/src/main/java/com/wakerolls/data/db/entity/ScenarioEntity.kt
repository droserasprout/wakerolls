package com.wakerolls.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scenarios")
data class ScenarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)
