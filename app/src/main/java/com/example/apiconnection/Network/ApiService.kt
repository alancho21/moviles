package com.example.apiconnection.Network

import com.example.apiconnection.MODELS.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

data class ArticlesResponse(
    val data: List<Article>
)

interface ApiService {

    // ---------- Auth ----------
    @POST("api/register")
    suspend fun register(@Body body: Map<String, String>): AuthResponse

    @POST("api/login")
    suspend fun login(@Body body: Map<String, String>): AuthResponse

    @GET("api/me")
    suspend fun me(): UserDto

    @POST("api/logout")
    suspend fun logout(): Response<Void>

    // ---------- Articles ----------
    @GET("api/articles")
    suspend fun getArticulos(): ArticlesResponse

    @GET("api/articles/{id}")
    suspend fun getArticulo(@Path("id") id: Int): Article

    @DELETE("api/articles/{id}")
    suspend fun deleteArticulo(@Path("id") id: Int): Response<Void>

    @Multipart
    @POST("api/articles")
    suspend fun createArticulo(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<ResponseBody>

    // PUT via POST + _method=PUT
    @Multipart
    @POST("api/articles/{id}")
    suspend fun updateArticulo(
        @Path("id") id: Int,
        @Part("_method") method: RequestBody,          // "PUT"
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        // ⬇️ Ambas listas son MultipartBody.Part (NO RequestBody) para evitar el wildcard
        @Part deletedImageUrls: List<MultipartBody.Part> = emptyList(), // cada part con nombre "deletedImageUrls[]"
        @Part images: List<MultipartBody.Part> = emptyList()            // cada part con nombre "images[]"
    ): Response<ResponseBody>
}
