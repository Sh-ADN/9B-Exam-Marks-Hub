package com.abutorab.marks9b.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abutorab.marks9b.data.local.entity.*
import com.abutorab.marks9b.data.local.dao.*

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [YearEntity::class, TermEntity::class, StudentEntity::class, SubjectEntity::class, MarkEntity::class],
    version = 7,
    exportSchema = false
)
abstract class MarksDatabase : RoomDatabase() {

    abstract fun yearDao(): YearDao
    abstract fun termDao(): TermDao
    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun markDao(): MarkDao

    companion object {
        @Volatile
        private var INSTANCE: MarksDatabase? = null

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE marks ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): MarksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarksDatabase::class.java,
                    "marks_database"
                )
                .addMigrations(MIGRATION_6_7)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
