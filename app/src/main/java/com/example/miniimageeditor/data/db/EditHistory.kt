package com.example.miniimageeditor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edit_history")
data class EditHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val timestamp: Long
)
