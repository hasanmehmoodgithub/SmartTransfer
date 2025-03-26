package com.smart.transfer.app.com.smart.transfer.app.features.history.data.repository

import com.smart.transfer.app.com.smart.transfer.app.features.history.data.dao.HistoryDao
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    suspend fun insertHistory(history: History) = historyDao.insertHistory(history)
    suspend fun getHistoryByTagOrFrom(tag: String, from: String) = historyDao.getHistoryByTagOrFrom(tag, from)
    suspend fun deleteHistory(id: Int) = historyDao.deleteHistory(id)
    fun getAllHistory(): Flow<List<History>> = historyDao.getAllHistory()
}
