package com.example.nongkibib

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val tvName = findViewById<TextView>(R.id.tv_profile_name)
        val tvEmail = findViewById<TextView>(R.id.tv_profile_email)

        val user = SessionManager(this).getUser()
        if (user != null) {
            tvName.text = user.name
            tvEmail.text = user.email
        }

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            onBackPressed()
        }

        val btnEditProfile = findViewById<MaterialButton>(R.id.btn_edit_profile)
        btnEditProfile.setOnClickListener {
            // We use FragmentManager if we want to display Fragment inside this Activity,
            // or if EditProfile is a Fragment, we can wrap it in a new Activity or
            // display it here if there's a container. For now we assume
            // navigation to detail "EDIT_PROFILE"
            openDetail("Edit Profile", "EDIT_PROFILE")
        }

        // Category 1: Account & Security
        findViewById<RelativeLayout>(R.id.item_account_info).setOnClickListener {
            openDetail("Phone Number", "ACCOUNT")
        }
        findViewById<RelativeLayout>(R.id.item_ktm_verification).setOnClickListener {
            openDetail("KTM Verification", "KTM")
        }
        findViewById<RelativeLayout>(R.id.item_security).setOnClickListener {
            openDetail("Security", "SECURITY")
        }

        // Category 2: Communication
        findViewById<RelativeLayout>(R.id.item_notifications).setOnClickListener {
            openDetail("Notifications", "NOTIFICATIONS")
        }
        findViewById<RelativeLayout>(R.id.item_chat_options).setOnClickListener {
            openDetail("Chat Options", "CHAT")
        }

        // Category 3: Preferences
        findViewById<RelativeLayout>(R.id.item_display).setOnClickListener {
            openDetail("Display", "DISPLAY")
        }
        findViewById<RelativeLayout>(R.id.item_language).setOnClickListener {
            openDetail("Language", "LANGUAGE")
        }

        // Category 4: Others
        findViewById<RelativeLayout>(R.id.item_support).setOnClickListener {
            openDetail("Support & Help", "HELP")
        }
        
        findViewById<RelativeLayout>(R.id.item_about)?.setOnClickListener {
            openDetail("About Application", "ABOUT")
        }

        findViewById<RelativeLayout>(R.id.btn_logout_item).setOnClickListener {
            showToast("Keluar clicked")
            // Handle Logout - Kembali ke Login
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        refreshProfileData()
    }

    private fun refreshProfileData() {
        val tvName = findViewById<TextView>(R.id.tv_profile_name)
        val tvEmail = findViewById<TextView>(R.id.tv_profile_email)
        val user = SessionManager(this).getUser()
        if (user != null) {
            tvName.text = user.name
            tvEmail.text = user.email
        }
    }

    private fun openDetail(title: String, type: String) {
        val intent = Intent(this, SettingsDetailActivity::class.java)
        intent.putExtra("SETTING_TITLE", title)
        intent.putExtra("SETTING_TYPE", type)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
