package com.smart.transfer.app.com.smart.transfer.app.features.history.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.dao.HistoryDao
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.database.AppDatabase
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {
    val allHistory: Flow<List<History>> = repository.getAllHistory()

    fun insertHistory(history: History) = viewModelScope.launch {
        repository.insertHistory(history)
    }

    fun getByTagOrFrom(tag: String, from: String) = viewModelScope.launch {
        val results = repository.getHistoryByTagOrFrom(tag, from)
        // Handle results
    }

    fun deleteHistory(id: Int) = viewModelScope.launch {
        repository.deleteHistory(id)
    }
}