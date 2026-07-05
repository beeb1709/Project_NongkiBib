package com.example.nongkibib.model

import com.google.gson.annotations.SerializedName

data class DiscussionItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("snippet") val snippet: String,
    @SerializedName("lastTime") val lastTime: String,
    @SerializedName("iconColor") val iconColor: String,
    @SerializedName("category") val category: String
)
