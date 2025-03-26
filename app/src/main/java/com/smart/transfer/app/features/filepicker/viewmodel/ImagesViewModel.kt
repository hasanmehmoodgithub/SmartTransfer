package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.data.ImageRepository

class ImagesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ImageRepository(application.contentResolver)

    val imageFlow = repository.getPagedImages().cachedIn(viewModelScope)
}