package com.wakerolls.domain.model

data class Item(
    val id: Long = 0,
    val name: String,
    val category: Category,
    val rarity: Rarity,
    val enabled: Boolean = true,
)
