package com.example.nongkibib.model

import com.google.gson.annotations.SerializedName

data class EventItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("location") val location: String,
    @SerializedName("date") val date: String,
    @SerializedName("type") val type: String,
    @SerializedName("imageUrl") val imageUrl: String
)
