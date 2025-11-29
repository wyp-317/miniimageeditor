package com.example.miniimageeditor.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert

@Database(entities = [EditHistory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context, AppDatabase::class.java, "mini_image_editor.db").build().also { INSTANCE = it }
        }
    }
}

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(item: EditHistory)
}
