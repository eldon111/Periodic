package com.emathias.periodic.db.converters

import androidx.room.TypeConverter
import com.emathias.periodic.model.Schedule

class ScheduleConverter {
    @TypeConverter
    fun fromString(value: String?): Schedule? = value?.let { Schedule.parse(it) }

    @TypeConverter
    fun toString(schedule: Schedule?): String? = schedule?.serialize()
}
