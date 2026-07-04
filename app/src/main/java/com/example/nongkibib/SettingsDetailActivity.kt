package com.example.nongkibib

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.Locale
import kotlin.random.Random

class SettingsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_detail)

        val title = intent.getStringExtra("SETTING_TITLE") ?: "Settings"
        val type = intent.getStringExtra("SETTING_TYPE") ?: ""

        findViewById<TextView>(R.id.tv_title).text = title
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { onBackPressed() }

        if (savedInstanceState == null) {
            val fragment: Fragment = when (type) {
                "ACCOUNT" -> AccountSettingFragment()
                "KTM" -> KtmSettingFragment()
                "SECURITY" -> SecuritySettingFragment()
                "PRIVACY" -> PrivacySettingFragment()
                "NOTIFICATIONS" -> NotificationSettingFragment()
                "CHAT" -> ChatSettingFragment()
                "DISPLAY" -> DisplaySettingFragment()
                "LANGUAGE" -> LanguageSettingFragment()
                "HELP" -> HelpSettingFragment()
                "ABOUT" -> AboutSettingFragment()
                "EDIT_PROFILE" -> EditProfileFragment()
                else -> AccountSettingFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}

class AccountSettingFragment : Fragment(R.layout.fragment_setting_account) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSendOtp = view.findViewById<Button>(R.id.btn_send_otp)
        
        btnSendOtp?.setOnClickListener {
            // Navigate to OTP Verification Layout
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OtpVerificationFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}

class OtpVerificationFragment : Fragment(R.layout.fragment_otp_verification) {

    private lateinit var tvTimer: TextView
    private lateinit var btnVerify: Button
    private var countDownTimer: CountDownTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTimer = view.findViewById(R.id.tv_resend_timer)
        btnVerify = view.findViewById(R.id.btn_verify)
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        setupOtpInputs(view)
        startResendTimer()

        btnVerify.setOnClickListener {
            verifyOtp()
        }
    }

    private fun setupOtpInputs(view: View) {
        val boxes = arrayOf(
            view.findViewById<EditText>(R.id.otp_box_1),
            view.findViewById<EditText>(R.id.otp_box_2),
            view.findViewById<EditText>(R.id.otp_box_3),
            view.findViewById<EditText>(R.id.otp_box_4),
            view.findViewById<EditText>(R.id.otp_box_5),
            view.findViewById<EditText>(R.id.otp_box_6)
        )

        for (i in boxes.indices) {
            boxes[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < boxes.size - 1) {
                        boxes[i + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 0 && i > 0) {
                        boxes[i - 1].requestFocus()
                    }
                }
            })
        }
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvTimer.text = String.format(Locale.getDefault(), "Resend in 00:%02d", seconds)
            }

            override fun onFinish() {
                tvTimer.text = "Resend Code"
                tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                tvTimer.setOnClickListener {
                    // Logic to resend OTP
                    startResendTimer()
                }
            }
        }.start()
    }

    private fun verifyOtp() {
        // Logic to verify OTP with loading state
        btnVerify.isEnabled = false
        btnVerify.text = "Verifying..."
        
        // Simulating network call
        view?.postDelayed({
            Toast.makeText(requireContext(), "Verification Successful!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}

class KtmSettingFragment : Fragment(R.layout.fragment_setting_ktm) {
    
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<Button>(R.id.btn_take_selfie)?.setOnClickListener {
            checkCameraPermission()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        Toast.makeText(requireContext(), "Opening Camera for Verification...", Toast.LENGTH_SHORT).show()
    }
}

class SecuritySettingFragment : Fragment(R.layout.fragment_setting_security)
class PrivacySettingFragment : Fragment(R.layout.fragment_setting_privacy)
class NotificationSettingFragment : Fragment(R.layout.fragment_setting_notifications)
class ChatSettingFragment : Fragment(R.layout.fragment_setting_chat)
class DisplaySettingFragment : Fragment(R.layout.fragment_setting_display) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rbLight = view.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rb_light)
        val rbDark = view.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rb_dark)
        val rbSystem = view.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rb_system)

        val radioButtons = listOf(rbLight, rbDark, rbSystem)

        fun selectOption(selectedId: Int) {
            radioButtons.forEach { it.isChecked = it.id == selectedId }
        }

        // Set click listeners for the RadioButtons themselves
        rbLight.setOnClickListener { selectOption(R.id.rb_light) }
        rbDark.setOnClickListener { selectOption(R.id.rb_dark) }
        rbSystem.setOnClickListener { selectOption(R.id.rb_system) }

        // Also make the whole row clickable for better UX
        view.findViewById<View>(R.id.icon_light_container).parent.let { parent ->
            (parent as View).setOnClickListener { selectOption(R.id.rb_light) }
        }
        view.findViewById<View>(R.id.icon_dark_container).parent.let { parent ->
            (parent as View).setOnClickListener { selectOption(R.id.rb_dark) }
        }
        view.findViewById<View>(R.id.icon_system_container).parent.let { parent ->
            (parent as View).setOnClickListener { selectOption(R.id.rb_system) }
        }
    }
}
class LanguageSettingFragment : Fragment(R.layout.fragment_setting_language)
class HelpSettingFragment : Fragment(R.layout.fragment_setting_help)
class AboutSettingFragment : Fragment(R.layout.fragment_setting_about)
