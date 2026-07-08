package com.example.nongkibib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class InboxFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)

        val profileHeader = view.findViewById<CardView>(R.id.cv_profile_header)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_header)
        val tvName = view.findViewById<TextView>(R.id.tv_profile_name_header)

        // Header tetap sebagai judul fitur
        tvName.text = "BIB Chats"

        val user = SessionManager(requireContext()).getUser()
        if (user != null) {
            // Update foto profil di header
        }

        profileHeader.setOnClickListener {
            val activity = activity as? MainActivity
            val navView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView?.selectedItemId = R.id.nav_profile
        }

        val chat1 = view.findViewById<View>(R.id.chat_item_1)
        val chat2 = view.findViewById<View>(R.id.chat_item_2)

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