package com.example.nongkibib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nongkibib.model.MessageItem

class MessageAdapter(private val messages: List<MessageItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ME = 1
    private val VIEW_TYPE_OTHER = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isMe) VIEW_TYPE_ME else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ME) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message_right)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time_right)

        fun bind(message: MessageItem) {
            tvMessage.text = message.text
            tvTime.text = message.timestamp
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message_left)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time_left)

        fun bind(message: MessageItem) {
            tvMessage.text = message.text
            tvTime.text = message.timestamp
        }
    }
}
