package com.example.nongkibib

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class EditProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_save)
        val etDob = view.findViewById<TextInputEditText>(R.id.et_dob)
        val btnEditPhoto = view.findViewById<MaterialCardView>(R.id.btn_edit_photo)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        etDob.setOnClickListener {
            showDatePicker(etDob)
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnEditPhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur ganti foto akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavigationVisibility(false)
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.setBottomNavigationVisibility(true)
    }

    private fun showDatePicker(etDob: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                etDob.setText(date)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveProfile() {
        // Simpan data (untuk sementara hanya toast)
        Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
