package com.emathias.periodic.db.converters

import androidx.room.TypeConverter
import com.cronutils.model.Cron
import com.emathias.periodic.util.CronUtils

class CronConverter {
    @TypeConverter
    fun fromString(value: String?): Cron? {
        return value?.let { CronUtils.parseCronExpression(it) }
    }

    @TypeConverter
    fun toString(cron: Cron?): String? {
        return cron?.asString()
    }
}