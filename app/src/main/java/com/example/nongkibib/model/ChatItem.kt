package com.example.nongkibib.model

import com.google.gson.annotations.SerializedName

data class ChatItem(
    @SerializedName("id") val id: String,
    @SerializedName("isGroup") val isGroup: Boolean,
    @SerializedName("name") val name: String,
    @SerializedName("avatar") val avatar: String,
    @SerializedName("lastMessage") val lastMessage: String,
    @SerializedName("lastTime") val lastTime: String,
    @SerializedName("unreadCount") val unreadCount: Int
)
