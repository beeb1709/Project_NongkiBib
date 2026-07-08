package com.example.nongkibib

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.nongkibib.api.ApiClient
import com.example.nongkibib.api.AuthResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class EditProfileFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etDob: TextInputEditText
    private lateinit var etBio: EditText
    private lateinit var tvDomisili: TextView
    private lateinit var tvKampus: TextView
    private lateinit var tvFakultas: TextView
    private lateinit var tvProdi: TextView
    private lateinit var tvAngkatan: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        sessionManager = SessionManager(requireContext())

        // Initialize Views
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_save)
        val btnEditPhoto = view.findViewById<MaterialCardView>(R.id.btn_edit_photo)
        
        etName = view.findViewById(R.id.et_name)
        etDob = view.findViewById(R.id.et_dob)
        etBio = view.findViewById(R.id.et_bio)
        tvDomisili = view.findViewById(R.id.tv_domisili)
        tvKampus = view.findViewById(R.id.tv_kampus)
        tvFakultas = view.findViewById(R.id.tv_fakultas)
        tvProdi = view.findViewById(R.id.tv_prodi)
        tvAngkatan = view.findViewById(R.id.tv_angkatan)

        loadUserData()

        // Set Click Listeners
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        etDob.setOnClickListener { showDatePicker() }
        btnSave.setOnClickListener { saveProfile() }
        btnEditPhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Change photo coming soon", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.container_domisili).setOnClickListener {
            showListDialog("Select Domicile", resources.getStringArray(R.array.indonesia_cities), tvDomisili)
        }
        view.findViewById<View>(R.id.container_kampus).setOnClickListener {
            showListDialog("Select Campus", resources.getStringArray(R.array.indonesia_universities), tvKampus)
        }
        view.findViewById<View>(R.id.container_fakultas).setOnClickListener {
            showListDialog("Select Faculty", resources.getStringArray(R.array.faculties), tvFakultas)
        }
        view.findViewById<View>(R.id.container_prodi).setOnClickListener {
            showListDialog("Select Study Program", resources.getStringArray(R.array.study_programs), tvProdi)
        }
        view.findViewById<View>(R.id.container_angkatan).setOnClickListener {
            showListDialog("Select Class Year", resources.getStringArray(R.array.class_years), tvAngkatan)
        }

        return view
    }

    private fun loadUserData() {
        val user = sessionManager.getUser()
        user?.let {
            etName.setText(it.name)
            etDob.setText(it.dob ?: "")
            etBio.setText(it.bio ?: "")
            tvDomisili.text = it.city ?: "Select Domicile"
            tvKampus.text = it.campus ?: "Select Campus"
            tvFakultas.text = it.faculty ?: "Select Faculty"
            tvProdi.text = it.studyProgram ?: "Select Study Program"
            tvAngkatan.text = it.classYear ?: "Select Class Year"
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                etDob.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showListDialog(title: String, items: Array<String>, targetView: TextView) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setItems(items) { _, which ->
                targetView.text = items[which]
            }
            .show()
    }

    private fun saveProfile() {
        val body = mapOf(
            "name" to etName.text.toString(),
            "dob" to etDob.text.toString(),
            "bio" to etBio.text.toString(),
            "city" to tvDomisili.text.toString(),
            "campus" to tvKampus.text.toString(),
            "faculty" to tvFakultas.text.toString(),
            "studyProgram" to tvProdi.text.toString(),
            "classYear" to tvAngkatan.text.toString()
        )

        ApiClient.instance.updateProfile(body).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedUser = response.body()?.user
                    if (updatedUser != null) {
                        sessionManager.saveUser(updatedUser)
                    }
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        (activity as? MainActivity)?.setBottomNavigationVisibility(false)
    }

    override fun onStop() {
        super.onStop()
        (activity as? MainActivity)?.setBottomNavigationVisibility(true)
    }
}
