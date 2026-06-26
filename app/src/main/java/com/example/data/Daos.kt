package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryItem)

    @Delete
    suspend fun deleteHistory(item: HistoryItem)

    @Query("DELETE FROM history_items")
    suspend fun clearHistory()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_items ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteItem)

    @Delete
    suspend fun deleteFavorite(item: FavoriteItem)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_items WHERE text = :text LIMIT 1)")
    fun isFavorite(text: String): Flow<Boolean>

    @Query("DELETE FROM favorite_items WHERE text = :text")
    suspend fun deleteFavoriteByText(text: String)
}
