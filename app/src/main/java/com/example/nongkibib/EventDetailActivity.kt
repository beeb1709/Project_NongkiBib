package com.example.nongkibib

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class EventDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        val btnBack = findViewById<MaterialCardView>(R.id.btn_back)
        val btnShare = findViewById<MaterialCardView>(R.id.btn_share)
        val btnRegister = findViewById<MaterialButton>(R.id.btn_register)

        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val tvLocation = findViewById<TextView>(R.id.tv_location)
        val tvDateTime = findViewById<TextView>(R.id.tv_date_time)
        val tvDescription = findViewById<TextView>(R.id.tv_description)
        val ivEventImage = findViewById<ImageView>(R.id.iv_event_image)
        val tvTag = findViewById<TextView>(R.id.tv_tag)

        // Get data from intent
        val title = intent.getStringExtra("TITLE") ?: "Networking Brunch"
        val location = intent.getStringExtra("LOCATION") ?: "Central Park Pavilion (Luring)"
        val dateTime = intent.getStringExtra("DATE_TIME") ?: "Sabtu, 24 Okt | 10:00 AM"
        val description = intent.getStringExtra("DESCRIPTION")
        val isOnline = intent.getBooleanExtra("IS_ONLINE", false)
        val tag = intent.getStringExtra("TAG") ?: "This Weekend"

        tvTitle.text = title
        tvDateTime.text = dateTime
        tvTag.text = tag
        
        if (description != null) {
            tvDescription.text = description
        }

        if (isOnline) {
            tvLocation.text = "Zoom Meeting (Online)"
            // Change icon if needed, though ic_location might still be used for "Virtual Link"
        } else {
            tvLocation.text = location
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnShare.setOnClickListener {
            // Share logic
        }

        btnRegister.setOnClickListener {
            // Registration logic
        }
    }
}
