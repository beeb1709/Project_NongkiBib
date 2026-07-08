package com.example.nongkibib

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ScholarshipDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scholarship_detail)

        val btnBack = findViewById<MaterialCardView>(R.id.btn_back)
        val btnShare = findViewById<MaterialCardView>(R.id.btn_share)
        val btnApply = findViewById<MaterialButton>(R.id.btn_apply)

        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val tvDeadline = findViewById<TextView>(R.id.tv_deadline)
        val tvOrganizer = findViewById<TextView>(R.id.tv_organizer)
        val llRequirements = findViewById<LinearLayout>(R.id.ll_requirements)

        // Get data from intent
        val title = intent.getStringExtra("TITLE") ?: "Beasiswa Unggulan"
        val deadline = intent.getStringExtra("DEADLINE") ?: "30 Agustus"
        val organizer = intent.getStringExtra("ORGANIZER") ?: "Info Session"

        tvTitle.text = title
        tvDeadline.text = deadline
        tvOrganizer.text = organizer

        setupRequirements(llRequirements)

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnShare.setOnClickListener {
            // Share logic
        }

        btnApply.setOnClickListener {
            // Apply logic
        }
    }

    private fun setupRequirements(container: LinearLayout) {
        val requirements = listOf(
            "IPK Minimal 3.25",
            "Mahasiswa Aktif Semester 3-5",
            "Sertifikat Prestasi Akademik/Non-Akademik (Opsional)",
            "Surat Rekomendasi Fakultas"
        )

        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (req in requirements) {
            val itemView = inflater.inflate(R.layout.item_requirement, container, false)
            val tvText = itemView.findViewById<TextView>(R.id.tv_requirement_text)
            tvText.text = req
            container.addView(itemView)
        }
    }
}
