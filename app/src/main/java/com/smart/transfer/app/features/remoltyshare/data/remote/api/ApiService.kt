package com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.data.remote.api

import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.model.DownloadResponse
import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Url

interface ApiService {
    @Multipart
    @POST("files/upload")
    fun uploadFile(
        @Part files: MultipartBody.Part,
    ): Call<UploadResponse> // Use your response body class here

    @GET("files/{unique_id}")
    fun getFileDetails(@Path("unique_id") uniqueId: String): Call<DownloadResponse>
    fun downloadFile(@Url fileUrl: String): Call<ResponseBody>  // Download file using dynamic URL


}
