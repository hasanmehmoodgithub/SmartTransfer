package com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.data.remote.api

import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Response

interface ApiService {
    @Multipart
    @POST("upload")
    suspend fun uploadFiles(
        @Part files: List<MultipartBody.Part>
    ): Response<UploadResponse>
}