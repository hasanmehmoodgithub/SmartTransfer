package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.data

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState

class ImagesPagingSource(
    private val contentResolver: ContentResolver
) : PagingSource<Int, String>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        return try {
            val page = params.key ?: 0
            val images = getImagesPaged(page * 20, 20) // Fetch 20 images at a time

            LoadResult.Page(
                data = images,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (images.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun getImagesPaged(offset: Int, limit: Int): List<String> {
        val imageList = mutableListOf<String>()
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val cursor = contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET $offset"
        )

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                imageList.add(it.getString(columnIndex))
            }
        }
        return imageList
    }

    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return state.anchorPosition
    }
}
