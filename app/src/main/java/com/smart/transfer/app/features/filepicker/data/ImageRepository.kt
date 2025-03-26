package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.data

import android.content.ContentResolver
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class ImageRepository(private val contentResolver: ContentResolver) {
    fun getPagedImages(): Flow<PagingData<String>> {
        return Pager(
            PagingConfig(pageSize = 20, enablePlaceholders = false)
        ) {
            ImagesPagingSource(contentResolver)
        }.flow
    }
}
