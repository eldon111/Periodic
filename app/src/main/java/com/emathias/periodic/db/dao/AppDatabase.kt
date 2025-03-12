package com.emathias.periodic.db.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.emathias.periodic.db.entities.TodoItem

@Database(entities = [TodoItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDao
}