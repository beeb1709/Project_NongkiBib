package com.example.nongkibib

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nongkibib.R
import com.example.nongkibib.api.ApiClient
import com.example.nongkibib.model.LocationUpdate
import com.example.nongkibib.model.MessageItem
import com.example.nongkibib.websocket.WebSocketClientManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity(), WebSocketClientManager.WebSocketListenerCallback {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private lateinit var adapter: MessageAdapter
    private val messagesList = ArrayList<MessageItem>()

    private var webSocketManager: WebSocketClientManager? = null

    // Ganti dengan Room Chat ID terpilih
    private var chatId = "chat-1" // Default chat dengan Zhou Koo Wii
    private var currentUserId = "usr-guest-1"
    private var currentUserName = "Habibie"
    private var currentUserAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatId = intent.getStringExtra("CHAT_ID") ?: "chat-1"
        currentUserId = intent.getStringExtra("USER_ID") ?: "usr-guest-1"
        currentUserName = intent.getStringExtra("USER_NAME") ?: "Habibie"
        currentUserAvatar = intent.getStringExtra("USER_AVATAR") ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80"

        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        // Setup RecyclerView
        adapter = MessageAdapter(messagesList)
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Mulai dari gelembung bawah
        }
        rvMessages.adapter = adapter

        // Hubungkan WebSocket untuk komunikasi real-time
        webSocketManager = WebSocketClientManager(this)
        webSocketManager?.connect()

        // Ambil riwayat chat sebelumnya dari database MySQL via REST API
        loadChatHistory()

        btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun loadChatHistory() {
        ApiClient.instance.getChatMessages(chatId).enqueue(object : Callback<List<MessageItem>> {
            override fun onResponse(call: Call<List<MessageItem>>, response: Response<List<MessageItem>>) {
                if (response.isSuccessful && response.body() != null) {
                    messagesList.clear()
                    messagesList.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                    rvMessages.scrollToPosition(messagesList.size - 1)
                }
            }

            override fun onFailure(call: Call<List<MessageItem>>, t: Throwable) {
                Toast.makeText(this@ChatActivity, "Gagal memuat pesan", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return

        // 1. Kirim real-time via WebSocket
        webSocketManager?.sendChatMessage(
            chatId = chatId,
            userId = currentUserId,
            userName = currentUserName,
            userAvatar = currentUserAvatar,
            text = text
        )

        // 2. HTTP backup ke MySQL server
        val body = mapOf("text" to text)
        ApiClient.instance.sendChatMessage(chatId, body).enqueue(object : Callback<MessageItem> {
            override fun onResponse(call: Call<MessageItem>, response: Response<MessageItem>) {
                val sentMsg = response.body()
                if (response.isSuccessful && sentMsg != null) {
                    // Update UI jika pengiriman sukses
                    etMessage.setText("")

                    // Supaya tidak dobel di UI, cek id sebelum menambahkan
                    if (messagesList.none { it.id == sentMsg.id }) {
                        messagesList.add(sentMsg)
                        adapter.notifyItemInserted(messagesList.size - 1)
                        rvMessages.scrollToPosition(messagesList.size - 1)
                    }
                }
            }

            override fun onFailure(call: Call<MessageItem>, t: Throwable) {
                Toast.makeText(this@ChatActivity, "Gagal mengirim pesan ke database", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- CALLBACK DARI WEBSOCKET ---
    override fun onNewMessageReceived(message: MessageItem) {
        runOnUiThread {
            // Hanya tambahkan jika pesan sesuai dengan ruang chat saat ini
            if (message.chatId == chatId) {
                if (messagesList.none { it.id == message.id }) {
                    messagesList.add(message)
                    adapter.notifyItemInserted(messagesList.size - 1)
                    rvMessages.scrollToPosition(messagesList.size - 1)
                }
            }
        }
    }

    override fun onLocationBroadcastReceived(locations: List<LocationUpdate>) {
        // Abaikan update maps di halaman chat
    }

    override fun onConnectionStatusChanged(connected: Boolean) {
        // Status koneksi WebSocket
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketManager?.disconnect()
    }
}
