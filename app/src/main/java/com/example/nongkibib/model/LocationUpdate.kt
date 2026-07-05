package com.example.nongkibib.model

import com.google.gson.annotations.SerializedName

data class LocationUpdate(
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("avatar") val avatar: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("updatedAt") val updatedAt: String
)
