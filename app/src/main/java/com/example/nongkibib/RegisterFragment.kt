package com.example.nongkibib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nongkibib.api.ApiClient
import com.example.nongkibib.api.AuthResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.util.Log
import com.google.android.material.button.MaterialButton

class RegisterFragment : Fragment() {

    private val TAG = "RegisterFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val btnRegister = view.findViewById<MaterialButton>(R.id.btn_register)
        val btnGoogle = view.findViewById<MaterialButton>(R.id.btn_google)
        val tvLogin = view.findViewById<TextView>(R.id.tv_login)

        val etFullName = view.findViewById<EditText>(R.id.et_full_name)
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val etPassword = view.findViewById<EditText>(R.id.et_password)

        btnRegister.setOnClickListener {
            Log.d(TAG, "Register button clicked")
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val body = mapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "password" to password
            )

            ApiClient.instance.register(body).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.user
                        if (user != null) {
                            val sm = SessionManager(requireContext())
                            sm.saveUser(user)
                            sm.saveToken("local_session_new_${user.id}") // Simpan token agar tidak stuck
                        }
                        Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        activity?.finish()
                    } else {
                        val errorMsg = response.body()?.message ?: "Registration failed"
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Log.e(TAG, "Register API Error: ${t.message}")
                    Toast.makeText(context, "Timeout: Gagal menghubungi server laptop. Cek Firewall!", Toast.LENGTH_LONG).show()
                }
            })
        }
        
        btnGoogle.setOnClickListener {
            Log.d(TAG, "Google Signup button clicked")
            Toast.makeText(context, "Google Sign-In integration in progress", Toast.LENGTH_SHORT).show()
        }

        tvLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}