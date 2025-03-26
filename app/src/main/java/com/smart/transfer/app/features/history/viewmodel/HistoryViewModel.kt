package com.smart.transfer.app.com.smart.transfer.app.features.history.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.dao.HistoryDao
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.database.AppDatabase
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.repository.HistoryRepository
import kotlinx.coroutines.launch
class HistoryViewModel(private val historyDao: HistoryDao) : ViewModel() {
    private val _historyState = MutableLiveData<HistoryState>()
    val historyState: LiveData<HistoryState> = _historyState

    fun loadHistory(tag: String, from: String) {
        viewModelScope.launch {
            _historyState.value = HistoryState.Loading
            try {
                val history = historyDao.getHistoryByTagAndFrom(tag, from)
                _historyState.postValue(HistoryState.Success(history))
            } catch (e: Exception) {
                _historyState.postValue(HistoryState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    sealed class HistoryState {
        object Loading : HistoryState()
        data class Success(val data: List<History>) : HistoryState()
        data class Error(val message: String) : HistoryState()
    }
}