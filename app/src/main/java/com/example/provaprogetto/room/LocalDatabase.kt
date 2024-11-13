package com.example.provaprogetto.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.provaprogetto.drink.IngredientListConverter

@Database(entities = [LocalPersonal::class], version = 1, exportSchema = false)
@TypeConverters(IngredientListConverter::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun personalDao(): PersonalDao

    companion object {
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getDatabase(context: Context): LocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
