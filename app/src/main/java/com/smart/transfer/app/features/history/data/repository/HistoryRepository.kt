package com.smart.transfer.app.com.smart.transfer.app.features.history.data.repository

import com.smart.transfer.app.com.smart.transfer.app.features.history.data.dao.HistoryDao
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History
import kotlinx.coroutines.flow.Flow
interface HistoryRepository {
    suspend fun insertHistory(history: History)
    suspend fun getHistoryByTagOrFrom(tag: String, from: String): List<History>
    suspend fun deleteHistory(id: Int)
    fun getAllHistory(): Flow<List<History>>
}

class HistoryRepositoryImpl(private val historyDao: HistoryDao) : HistoryRepository {
    override suspend fun insertHistory(history: History) = historyDao.insertHistory(history)
    override suspend fun getHistoryByTagOrFrom(tag: String, from: String) =
        historyDao.getHistoryByTagOrFrom(tag, from)
    override suspend fun deleteHistory(id: Int) = historyDao.deleteHistory(id)
    override fun getAllHistory(): Flow<List<History>> = historyDao.getAllHistory()
}