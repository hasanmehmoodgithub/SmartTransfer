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

    // LiveData holding the current history list
    private val _paginatedHistory = MutableLiveData<List<History>>()
    val paginatedHistory: LiveData<List<History>> get() = _paginatedHistory

    // Paging variables
    private var currentPage = 0
    private val pageSize = 20
    private var isLoading = false
    private val currentItems = mutableListOf<History>()

    fun loadPaginatedHistory(tag: String, from: String, reset: Boolean = false) {
        if (isLoading) return

        isLoading = true
        if (reset) {
            currentPage = 0
            currentItems.clear()
        }
        val offset = currentPage * pageSize

        viewModelScope.launch {
            val newItems = repository.getPaginatedHistory(tag, from, pageSize, offset)
            if (newItems.isNotEmpty()) {
                currentItems.addAll(newItems)
                _paginatedHistory.postValue(currentItems.toList())
                currentPage++
            }
            isLoading = false
        }
    }

    // Other existing functions (insertHistory, deleteHistory, etc.)...
}
