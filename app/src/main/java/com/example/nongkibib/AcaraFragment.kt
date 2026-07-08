package com.example.nongkibib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        // Header tetap sebagai judul fitur
        tvName.text = "BIB Hub"

        profileHeader.setOnClickListener {
            val activity = activity as? MainActivity
            val navView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView?.selectedItemId = R.id.nav_profile
        }

        // --- SCHOLARSHIP 1 ---
        val sc1Click = View.OnClickListener {
            openScholarshipDetail("Tech Innovators Scholarship 2024", "15 Nov 2024", "Global Tech Foundation")
        }
        view.findViewById<View>(R.id.card_scholarship_1).setOnClickListener(sc1Click)
        view.findViewById<View>(R.id.btn_register_scholarship_1).setOnClickListener(sc1Click)

        // --- SCHOLARSHIP 2 ---
        val sc2Click = View.OnClickListener {
            openScholarshipDetail("Design Leadership Program", "01 Oct 2024", "Creative Hub Asia")
        }
        view.findViewById<View>(R.id.card_scholarship_2).setOnClickListener(sc2Click)
        view.findViewById<View>(R.id.btn_closed_scholarship_2).setOnClickListener(sc2Click)

        // --- EVENT COFFEE ---
        val coffeeClick = View.OnClickListener {
            openEventDetail("Morning Coffee Walk", "Central Park Pavilion (Luring)", "Sat, 24 Oct | 08:00 AM", false, "Local")
        }
        view.findViewById<View>(R.id.card_event_coffee).setOnClickListener(coffeeClick)
        view.findViewById<View>(R.id.btn_join_coffee).setOnClickListener(coffeeClick)

        // --- EVENT GALLERY ---
        val galleryClick = View.OnClickListener {
            openEventDetail("Gallery Mixer", "The Modernist Annex (Luring)", "Sun, 25 Oct | 04:00 PM", false, "National")
        }
        view.findViewById<View>(R.id.card_event_gallery).setOnClickListener(galleryClick)
        view.findViewById<View>(R.id.btn_join_gallery).setOnClickListener(galleryClick)

        // --- EVENT DINNER ---
        val dinnerClick = View.OnClickListener {
            openEventDetail("Farm to Table Dinner", "Google Meet (Online)", "Fri, 30 Oct | 07:00 PM", true, "Regional")
        }
        view.findViewById<View>(R.id.card_event_dinner).setOnClickListener(dinnerClick)
        view.findViewById<View>(R.id.btn_join_dinner).setOnClickListener(dinnerClick)

        return view
    }

    private fun openScholarshipDetail(title: String, deadline: String, organizer: String) {
        val intent = Intent(requireContext(), ScholarshipDetailActivity::class.java).apply {
            putExtra("TITLE", title)
            putExtra("DEADLINE", deadline)
            putExtra("ORGANIZER", organizer)
        }
        startActivity(intent)
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
}
