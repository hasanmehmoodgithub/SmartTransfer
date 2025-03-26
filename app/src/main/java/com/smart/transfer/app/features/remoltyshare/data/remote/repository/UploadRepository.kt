package com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.data.remote.repository

import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.data.remote.api.RetrofitClient

import okhttp3.MultipartBody


class UploadRepository {
    suspend fun uploadFiles(files: List<MultipartBody.Part>) = RetrofitClient.instance.uploadFiles(files)
}