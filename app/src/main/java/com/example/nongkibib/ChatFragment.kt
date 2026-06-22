package com.example.nongkibib

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var rvChat: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        rvChat = view.findViewById(R.id.rv_chat)
        val btnBack = view.findViewById<ImageView>(R.id.btn_back_chat)
        val etChat = view.findViewById<EditText>(R.id.et_chat)
        val btnSend = view.findViewById<ImageButton>(R.id.btn_send)
        val tvChatName = view.findViewById<TextView>(R.id.tv_chat_name)

        val chatName = arguments?.getString("CHAT_NAME") ?: "Budi (BIB 2023)"
        tvChatName.text = chatName

        adapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(context)
        rvChat.adapter = adapter

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSend.setOnClickListener {
            val text = etChat.text.toString()
            if (text.isNotEmpty()) {
                sendMessage(text, true)
                etChat.setText("")

                // Simulasi Balasan Otomatis (Live Chat Feel)
                Handler(Looper.getMainLooper()).postDelayed({
                    val reply = when {
                        text.contains("halo", true) -> "Halo juga! Ada apa?"
                        text.contains("nongki", true) -> "Boleh, jam berapa?"
                        text.contains("dimana", true) -> "Di Cafe Sudut ya."
                        else -> "Oke, siap!"
                    }
                    sendMessage(reply, false)
                }, 1500) // Balas setelah 1.5 detik
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        (activity as? MainActivity)?.setBottomNavigationVisibility(false)
    }

    override fun onStop() {
        super.onStop()
        (activity as? MainActivity)?.setBottomNavigationVisibility(true)
    }

    private fun sendMessage(text: String, isMe: Boolean) {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        messages.add(ChatMessage(text, currentTime, isMe))
        adapter.notifyItemInserted(messages.size - 1)
        rvChat.scrollToPosition(messages.size - 1)
    }
}
