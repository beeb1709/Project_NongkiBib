package com.example.nongkibib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class AcaraFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_acara, container, false)

        val profileHeader = view.findViewById<MaterialCardView>(R.id.cv_profile_header)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_header)
        val tvName = view.findViewById<TextView>(R.id.tv_profile_name_header)

        // Integrasi data user
        ivProfile.setImageResource(R.drawable.prabowo)
        tvName.text = "BIB Hub"

        profileHeader.setOnClickListener {
            val activity = activity as? MainActivity
            val navView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView?.selectedItemId = R.id.nav_profile
        }

        return view
    }
}
