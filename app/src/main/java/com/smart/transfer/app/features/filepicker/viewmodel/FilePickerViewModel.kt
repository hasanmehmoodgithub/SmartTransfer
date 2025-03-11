package com.smart.transfer.app.features.filepicker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FilePickerViewModel : ViewModel() {

    private val _selectedCategory = MutableLiveData<Int>()
    val selectedCategory: LiveData<Int> get() = _selectedCategory

    fun setSelectedCategory(index: Int) {
        _selectedCategory.value = index
    }
}
