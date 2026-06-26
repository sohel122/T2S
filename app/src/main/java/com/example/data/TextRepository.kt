package com.example.data

import kotlinx.coroutines.flow.Flow

class TextRepository(
    private val historyDao: HistoryDao,
    private val favoriteDao: FavoriteDao
) {
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()
    val allFavorites: Flow<List<FavoriteItem>> = favoriteDao.getAllFavorites()

    suspend fun insertHistory(text: String) {
        if (text.isNotBlank()) {
            historyDao.insertHistory(HistoryItem(text = text.trim()))
        }
    }

    suspend fun deleteHistory(item: HistoryItem) {
        historyDao.deleteHistory(item)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    suspend fun insertFavorite(text: String) {
        if (text.isNotBlank()) {
            favoriteDao.insertFavorite(FavoriteItem(text = text.trim()))
        }
    }

    suspend fun deleteFavorite(item: FavoriteItem) {
        favoriteDao.deleteFavorite(item)
    }

    suspend fun toggleFavorite(text: String, isFav: Boolean) {
        if (isFav) {
            favoriteDao.deleteFavoriteByText(text.trim())
        } else {
            favoriteDao.insertFavorite(FavoriteItem(text = text.trim()))
        }
    }

    fun isFavorite(text: String): Flow<Boolean> {
        return favoriteDao.isFavorite(text.trim())
    }
}
