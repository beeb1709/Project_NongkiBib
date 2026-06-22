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

class MapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val profileHeader = view.findViewById<CardView>(R.id.cv_profile_header)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_header)
        val tvName = view.findViewById<TextView>(R.id.tv_profile_name_header)
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)

        // Integrasi data user
        ivProfile.setImageResource(R.drawable.profile_habibie)
        tvName.text = "Habibie"

        profileHeader.setOnClickListener {
            val activity = activity as? MainActivity
            val navView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView?.selectedItemId = R.id.nav_profile
        }

        btnBack.setOnClickListener {
            val activity = activity as? MainActivity
            val navView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView?.selectedItemId = R.id.nav_home
        }

        return view
    }
}
