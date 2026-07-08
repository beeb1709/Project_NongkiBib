package com.example.nongkibib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view_container) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.map_view_container, it).commit()
            }
        mapFragment.getMapAsync(this)

        val profileHeader = view.findViewById<View>(R.id.cv_profile_header)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_header)
        val tvName = view.findViewById<TextView>(R.id.tv_profile_name_header)

        // Header tetap sebagai judul fitur
        tvName.text = "BIB Spots"

        val user = SessionManager(requireContext()).getUser()
        if (user != null) {
            // Update foto di header jika perlu
        }

        profileHeader.setOnClickListener {
            val activity = activity as? MainActivity
            val navView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView?.selectedItemId = R.id.nav_profile
        }

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Default position (Jakarta)
        val defaultLoc = LatLng(-6.2088, 106.8456)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 12f))
        googleMap?.addMarker(MarkerOptions().position(defaultLoc).title("Jakarta Center"))
    }
}
