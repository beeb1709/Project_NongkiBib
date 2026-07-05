package com.example.nongkibib.model

import com.google.gson.annotations.SerializedName

data class MessageItem(
    @SerializedName("id") val id: String,
    @SerializedName("chatId") val chatId: String,
    @SerializedName("senderName") val senderName: String,
    @SerializedName("senderAvatar") val senderAvatar: String,
    @SerializedName("text") val text: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("isMe") val isMe: Boolean
)
