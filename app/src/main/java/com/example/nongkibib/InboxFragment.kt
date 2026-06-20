package com.example.nongkibib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment

class InboxFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)

        val chat1 = view.findViewById<RelativeLayout>(R.id.chat_item_1)
        val chat2 = view.findViewById<RelativeLayout>(R.id.chat_item_2)

        chat1.setOnClickListener { openChat("Zhou Koo Wii - BIB 2024") }
        chat2.setOnClickListener { openChat("Prabowo (Ketua Angkatan)") }

        return view
    }

    private fun openChat(name: String) {
        val fragment = ChatFragment()
        val bundle = Bundle()
        bundle.putString("CHAT_NAME", name)
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}