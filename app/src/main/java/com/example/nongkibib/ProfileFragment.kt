package com.example.nongkibib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val menuEditProfile = view.findViewById<RelativeLayout>(R.id.menu_edit_profile)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)

        menuEditProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogout.setOnClickListener {
            logout()
        }

        return view
    }

    private fun logout() {
        Toast.makeText(requireContext(), "Berhasil Keluar", Toast.LENGTH_SHORT).show()
        
        // Kembali ke AuthActivity (Halaman Login)
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}
