package com.example.nongkibib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val btnGoogle = view.findViewById<Button>(R.id.btn_google)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tv_forgot_password)
        val tvLogin = view.findViewById<TextView>(R.id.tv_login)

        btnRegister.setOnClickListener {
            Toast.makeText(context, "Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show()
            // Simulasi masuk ke MainActivity setelah daftar
            startActivity(Intent(requireContext(), MainActivity::class.java))
            activity?.finish()
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(context, "Simulasi Google Sign-In", Toast.LENGTH_SHORT).show()
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(context, "Fitur Reset Password", Toast.LENGTH_SHORT).show()
        }

        tvLogin.setOnClickListener {
            Toast.makeText(context, "Pindah ke Halaman Login", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}