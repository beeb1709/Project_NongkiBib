package com.example.nongkibib.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.example.nongkibib.model.LocationUpdate
import com.example.nongkibib.model.MessageItem
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketClientManager(private val listener: WebSocketListenerCallback) {

    // Ganti dengan alamat WS server backend Anda (misalnya ws://10.0.2.2:3000/ws untuk Android Emulator)
    private val wsUrl = "ws://10.0.2.2:3000/ws"
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Keep-alive websocket
        .build()

    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private val handler = Handler(Looper.getMainLooper())
    private var isConnected = false

    interface WebSocketListenerCallback {
        fun onLocationBroadcastReceived(locations: List<LocationUpdate>)
        fun onNewMessageReceived(message: MessageItem)
        fun onConnectionStatusChanged(connected: Boolean)
    }

    fun connect() {
        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.d("WS_MANAGER", "WebSocket Connection opened successfully.")
                handler.post { listener.onConnectionStatusChanged(true) }

                // Kirim pendaftaran awal ke server jika diperlukan
                val regMsg = mapOf("type" to "register")
                webSocket.send(gson.toJson(regMsg))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WS_MANAGER", "Message received: $text")
                try {
                    val mapType = gson.fromJson(text, Map::class.java)
                    val type = mapType["type"] as? String

                    when (type) {
                        "location_broadcast" -> {
                            val response = gson.fromJson(text, LocationBroadcastResponse::class.java)
                            handler.post {
                                listener.onLocationBroadcastReceived(response.locations)
                            }
                        }
                        "new_message" -> {
                            val response = gson.fromJson(text, NewMessageResponse::class.java)
                            handler.post {
                                listener.onNewMessageReceived(response.message)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WS_MANAGER", "Error parsing WebSocket message", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                isConnected = false
                Log.d("WS_MANAGER", "WebSocket Connection is closing: $reason")
                handler.post { listener.onConnectionStatusChanged(false) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("WS_MANAGER", "WebSocket Connection failure: ${t.message}", t)
                handler.post { listener.onConnectionStatusChanged(false) }
                // Coba hubungkan kembali secara otomatis setelah 5 detik
                handler.postDelayed({ connect() }, 5000)
            }
        })
    }

    fun sendLocationUpdate(userId: String, name: String, avatar: String, lat: Double, lng: Double) {
        if (!isConnected || webSocket == null) return
        val payload = mapOf(
            "type" to "location_update",
            "userId" to userId,
            "name" to name,
            "avatar" to avatar,
            "lat" to lat,
            "lng" to lng
        )
        webSocket?.send(gson.toJson(payload))
    }

    fun sendChatMessage(chatId: String, userId: String, userName: String, userAvatar: String, text: String) {
        if (!isConnected || webSocket == null) return
        val payload = mapOf(
            "type" to "chat_message",
            "chatId" to chatId,
            "userId" to userId,
            "userName" to userName,
            "userAvatar" to userAvatar,
            "text" to text
        )
        webSocket?.send(gson.toJson(payload))
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        webSocket = null
        isConnected = false
    }

    private data class LocationBroadcastResponse(
        val type: String,
        val locations: List<LocationUpdate>
    )

    private data class NewMessageResponse(
        val type: String,
        val message: MessageItem
    )
}
