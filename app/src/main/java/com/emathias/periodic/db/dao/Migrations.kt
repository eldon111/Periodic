package com.emathias.periodic.db.dao

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * TodoItem becomes a generated item tied to a ScheduledItem. Old to-dos had no link to a
 * scheduled item, so they are discarded.
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `TodoItem`")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `TodoItem` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`scheduledItemId` INTEGER NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`dueDate` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`completedAt` INTEGER, " +
                "FOREIGN KEY(`scheduledItemId`) REFERENCES `ScheduledItem`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_TodoItem_scheduledItemId` ON `TodoItem` (`scheduledItemId`)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `ScheduledItem` ADD COLUMN `archivedAt` INTEGER")
    }
}

/** TodoItem.scheduledItemId becomes nullable so one-off to-dos can exist without a scheduled item. */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `TodoItem_new` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`scheduledItemId` INTEGER, " +
                "`title` TEXT NOT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`dueDate` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`completedAt` INTEGER, " +
                "FOREIGN KEY(`scheduledItemId`) REFERENCES `ScheduledItem`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL(
            "INSERT INTO `TodoItem_new` (id, scheduledItemId, title, description, dueDate, createdAt, completedAt) " +
                "SELECT id, scheduledItemId, title, description, dueDate, createdAt, completedAt FROM `TodoItem`"
        )
        db.execSQL("DROP TABLE `TodoItem`")
        db.execSQL("ALTER TABLE `TodoItem_new` RENAME TO `TodoItem`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_TodoItem_scheduledItemId` ON `TodoItem` (`scheduledItemId`)")
    }
}
