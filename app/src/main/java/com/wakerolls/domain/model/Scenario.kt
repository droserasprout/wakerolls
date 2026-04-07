package com.wakerolls.domain.model

data class Scenario(
    val id: Long = 0,
    val name: String,
    val slots: List<ScenarioSlot>,
)

data class ScenarioSlot(
    val id: Long = 0,
    val category: String,
    val count: Int = 1,
)
