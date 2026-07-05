package com.example.nongkibib.model

import com.google.gson.annotations.SerializedName

data class SpotItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("address") val address: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("wifi") val wifi: Boolean
)
