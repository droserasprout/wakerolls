package com.wakerolls.data.db.entity

import androidx.room.TypeConverter
import com.wakerolls.domain.model.Rarity

class Converters {
    @TypeConverter fun fromRarity(value: Rarity): String = value.name
    @TypeConverter fun toRarity(value: String): Rarity = Rarity.valueOf(value)
}
