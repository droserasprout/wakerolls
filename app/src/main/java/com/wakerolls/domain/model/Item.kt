package com.wakerolls.domain.model

data class Item(
    val id: Long = 0,
    val name: String,
    val category: String,
    val rarity: Rarity,
    val enabled: Boolean = true,
    val rolledCount: Int = 0,
    val completedCount: Int = 0,
)
