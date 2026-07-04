package com.example.nongkibib.api

import com.example.nongkibib.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    fun register(@Body body: Map<String, String>): Call<AuthResponse>

    @POST("api/auth/login")
    fun login(@Body body: Map<String, String>): Call<AuthResponse>

    @GET("api/auth/current-user")
    fun getCurrentUser(): Call<AuthResponse>

    @POST("api/auth/google/verify-token")
    fun verifyGoogleToken(@Body body: Map<String, String>): Call<AuthResponse>

    @GET("api/spots")
    fun getSpots(): Call<List<SpotItem>>

    @GET("api/events")
    fun getEvents(): Call<List<EventItem>>

    @GET("api/discussions")
    fun getDiscussions(): Call<List<DiscussionItem>>

    @GET("api/chats")
    fun getChats(): Call<List<ChatItem>>

    @GET("api/chats/{id}/messages")
    fun getChatMessages(@Path("id") chatId: String): Call<List<MessageItem>>

    @POST("api/chats/{id}/messages")
    fun sendChatMessage(@Path("id") chatId: String, @Body body: Map<String, String>): Call<MessageItem>

    @POST("api/location/update")
    fun updateLocation(@Body body: Map<String, Any>): Call<LocationUpdateResponse>

    @GET("api/location/active")
    fun getActiveLocations(): Call<List<LocationUpdate>>

    @PUT("api/user/profile")
    fun updateProfile(@Body body: Map<String, String>): Call<AuthResponse>

    @POST("api/user/verify-ktm")
    fun verifyKtm(@Body body: Map<String, String>): Call<AuthResponse>
}

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val user: User?
)

data class LocationUpdateResponse(
    val success: Boolean,
    val location: LocationUpdate?
)
