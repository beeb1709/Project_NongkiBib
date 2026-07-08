package com.example.nongkibib

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.Locale

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
        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener { parentFragmentManager.popBackStack() }
        setupOtpInputs(view)
        startResendTimer()
        btnVerify.setOnClickListener { verifyOtp() }
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
                    if (s?.length == 1 && i < boxes.size - 1) boxes[i + 1].requestFocus()
                }
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 0 && i > 0) boxes[i - 1].requestFocus()
                }
            })
        }
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = String.format(Locale.getDefault(), "Resend in 00:%02d", millisUntilFinished / 1000)
            }
            override fun onFinish() {
                tvTimer.text = "Resend Code"
                tvTimer.setOnClickListener { startResendTimer() }
            }
        }.start()
    }

    private fun verifyOtp() {
        btnVerify.isEnabled = false
        btnVerify.text = "Verifying..."
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
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera() else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_take_selfie)?.setOnClickListener { checkCameraPermission() }
    }
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) openCamera()
        else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    private fun openCamera() { Toast.makeText(requireContext(), "Opening Camera...", Toast.LENGTH_SHORT).show() }
}

class SecuritySettingFragment : Fragment(R.layout.fragment_setting_security)
class PrivacySettingFragment : Fragment(R.layout.fragment_setting_privacy)
class NotificationSettingFragment : Fragment(R.layout.fragment_setting_notifications)

class ChatSettingFragment : Fragment(R.layout.fragment_setting_chat) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_save_chat)?.setOnClickListener {
            Toast.makeText(requireContext(), "Chat settings saved!", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }
}

class DisplaySettingFragment : Fragment(R.layout.fragment_setting_display) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rbLight = view.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rb_light)
        val rbDark = view.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rb_dark)
        val rbSystem = view.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rb_system)
        val radioButtons = listOf(rbLight, rbDark, rbSystem)
        fun selectOption(selectedId: Int) { radioButtons.forEach { it.isChecked = it.id == selectedId } }
        rbLight.setOnClickListener { selectOption(R.id.rb_light) }
        rbDark.setOnClickListener { selectOption(R.id.rb_dark) }
        rbSystem.setOnClickListener { selectOption(R.id.rb_system) }
        view.findViewById<View>(R.id.icon_light_container).parent.let { (it as View).setOnClickListener { selectOption(R.id.rb_light) } }
        view.findViewById<View>(R.id.icon_dark_container).parent.let { (it as View).setOnClickListener { selectOption(R.id.rb_dark) } }
        view.findViewById<View>(R.id.icon_system_container).parent.let { (it as View).setOnClickListener { selectOption(R.id.rb_system) } }
        view.findViewById<Button>(R.id.btn_save_display)?.setOnClickListener {
            Toast.makeText(requireContext(), "Display settings saved!", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }
}

class LanguageSettingFragment : Fragment(R.layout.fragment_setting_language) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_save_language)?.setOnClickListener {
            Toast.makeText(requireContext(), "Language saved!", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }
}

class HelpSettingFragment : Fragment(R.layout.fragment_setting_help)
class AboutSettingFragment : Fragment(R.layout.fragment_setting_about)
