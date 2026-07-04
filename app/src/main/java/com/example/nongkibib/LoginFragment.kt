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

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val btnLogin = view.findViewById<Button>(R.id.btn_login)
        val btnGoogleLogin = view.findViewById<Button>(R.id.btn_google)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tv_forgot_password)
        val tvRegister = view.findViewById<TextView>(R.id.tv_register)

        btnLogin.setOnClickListener {
            Toast.makeText(context, "Masuk Berhasil!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            activity?.finish()
        }

        btnGoogleLogin.setOnClickListener {
            Toast.makeText(context, "Masuk dengan Google", Toast.LENGTH_SHORT).show()
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(context, "Fitur Reset Password", Toast.LENGTH_SHORT).show()
        }

        tvRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}