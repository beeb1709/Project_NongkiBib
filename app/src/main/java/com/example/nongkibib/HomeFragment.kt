package com.example.nongkibib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val profileHeader = view.findViewById<CardView>(R.id.cv_profile_header)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_header)
        val tvName = view.findViewById<TextView>(R.id.tv_profile_name_header)

        // Header tetap sebagai judul fitur
        tvName.text = "BIB Community"
        
        val user = SessionManager(requireContext()).getUser()
        if (user != null) {
            // Kita bisa mengupdate foto profil di header jika ingin
            // ivProfile.load(user.avatar)
        }

        profileHeader.setOnClickListener {
            // Navigasi ke fragment profil
            val activity = activity as? MainActivity
            val navView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView?.selectedItemId = R.id.nav_profile
        }

        // Event/Scholarship Click Listeners
        view.findViewById<View>(R.id.card_event_1).setOnClickListener {
            openEventDetail("Networking Brunch", "Senopati Spot (Luring)", "This Weekend | 10:00 AM", false, "This Weekend")
        }

        view.findViewById<View>(R.id.card_event_2).setOnClickListener {
            openEventDetail("Design Thinking Workshop", "Zoom Meeting (Online)", "30 Oct | 02:00 PM", true, "Workshop")
        }

        return view
    }

    private fun openEventDetail(title: String, location: String, dateTime: String, isOnline: Boolean, tag: String) {
        val intent = Intent(requireContext(), EventDetailActivity::class.java).apply {
            putExtra("TITLE", title)
            putExtra("LOCATION", location)
            putExtra("DATE_TIME", dateTime)
            putExtra("IS_ONLINE", isOnline)
            putExtra("TAG", tag)
        }
        startActivity(intent)
    }

    private fun openScholarshipDetail(title: String, deadline: String, organizer: String) {
        val intent = Intent(requireContext(), ScholarshipDetailActivity::class.java).apply {
            putExtra("TITLE", title)
            putExtra("DEADLINE", deadline)
            putExtra("ORGANIZER", organizer)
        }
        startActivity(intent)
    }
}
