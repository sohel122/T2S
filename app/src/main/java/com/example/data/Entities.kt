package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_items")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
