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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import android.util.Log
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Configure Google Sign-In
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("493398626697-li78s3db9iemt1qh7l5jkasnfnconffb.apps.googleusercontent.com")
                .build()
            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        } catch (e: Exception) {
            Log.e(TAG, "Google SignIn Client Init Error: ${e.message}")
        }

        val btnLogin = view.findViewById<MaterialButton>(R.id.btn_login)
        val btnGoogleLogin = view.findViewById<MaterialButton>(R.id.btn_google)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tv_forgot_password)
        val tvRegister = view.findViewById<TextView>(R.id.tv_register)

        btnLogin.setOnClickListener {
            Log.d(TAG, "Login button clicked")
            val email = view.findViewById<EditText>(R.id.et_email).text.toString()
            val password = view.findViewById<EditText>(R.id.et_password).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val body = mapOf("email" to email, "password" to password)
            com.example.nongkibib.api.ApiClient.instance.login(body).enqueue(object : retrofit2.Callback<com.example.nongkibib.api.AuthResponse> {
                override fun onResponse(call: retrofit2.Call<com.example.nongkibib.api.AuthResponse>, response: retrofit2.Response<com.example.nongkibib.api.AuthResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.user
                        if (user != null) {
                            val sm = SessionManager(requireContext())
                            sm.saveUser(user)
                            sm.saveToken("local_session_${user.id}") // Simpan token agar tidak stuck
                        }
                        Toast.makeText(context, "Welcome back, ${user?.name}!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        activity?.finish()
                    } else {
                        Toast.makeText(context, "Login failed: ${response.body()?.message ?: "Wrong credentials"}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.example.nongkibib.api.AuthResponse>, t: Throwable) {
                    Log.e(TAG, "Login API Error: ${t.message}")
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        
        btnGoogleLogin.setOnClickListener {
            Log.d(TAG, "Google Login button clicked")
            if (::googleSignInClient.isInitialized) {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            } else {
                Toast.makeText(context, "Google Services not ready", Toast.LENGTH_SHORT).show()
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken

                if (idToken != null) {
                    val body = mapOf(
                        "idToken" to idToken,
                        "email" to (account.email ?: ""),
                        "name" to (account.displayName ?: ""),
                        "avatar" to (account.photoUrl?.toString() ?: "")
                    )

                    com.example.nongkibib.api.ApiClient.instance.verifyGoogleToken(body).enqueue(object : retrofit2.Callback<com.example.nongkibib.api.AuthResponse> {
                        override fun onResponse(call: retrofit2.Call<com.example.nongkibib.api.AuthResponse>, response: retrofit2.Response<com.example.nongkibib.api.AuthResponse>) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                val user = response.body()?.user
                                if (user != null) {
                                    SessionManager(requireContext()).saveUser(user)
                                    SessionManager(requireContext()).saveToken("google_session_active")
                                }
                                Toast.makeText(context, "Google Login Success!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                                activity?.finish()
                            } else {
                                val msg = response.body()?.message ?: "Backend Verification Failed"
                                Log.e(TAG, "Google Auth Error: $msg")
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                // Fallback: Tetap izinkan masuk jika ini hanya masalah sinkronisasi lokal (opsional untuk demo)
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<com.example.nongkibib.api.AuthResponse>, t: Throwable) {
                            Log.e(TAG, "Connection Error: ${t.message}")
                            Toast.makeText(context, "Timeout: Tidak bisa terhubung ke server laptop (${t.message}). Cek Firewall!", Toast.LENGTH_LONG).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Google ID Token is null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Login Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}