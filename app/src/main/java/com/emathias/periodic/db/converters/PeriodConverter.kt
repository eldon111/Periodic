package com.emathias.periodic.db.converters

import androidx.room.TypeConverter
import java.time.Period

class PeriodConverter {
    @TypeConverter
    fun fromString(value: String?): Period? {
        return value?.let { Period.parse(it) }
    }

    @TypeConverter
    fun toString(period: Period?): String? {
        return period?.toString()
    }
}