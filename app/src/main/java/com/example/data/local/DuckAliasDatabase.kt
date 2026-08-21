package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BearerTokenEntity::class, AliasEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DuckAliasDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao
    abstract fun aliasDao(): AliasDao

    companion object {
        @Volatile
        private var INSTANCE: DuckAliasDatabase? = null

        fun getDatabase(context: Context): DuckAliasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DuckAliasDatabase::class.java,
                    "duck_alias_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
