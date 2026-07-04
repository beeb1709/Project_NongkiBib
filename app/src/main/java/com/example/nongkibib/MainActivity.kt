package com.example.nongkibib

import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var receiver: NongkiBroadcastReceiver
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        viewPager = findViewById(R.id.viewPager)

        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter
        
        // Optimasi: Membatasi fragment yang dimuat di memori untuk kelancaran swipe
        viewPager.offscreenPageLimit = 2

        // Sinkronisasi ViewPager swipe ke BottomNav
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val menuId = when (position) {
                    0 -> R.id.nav_home
                    1 -> R.id.nav_acara
                    2 -> R.id.nav_map
                    3 -> R.id.nav_inbox
                    4 -> R.id.nav_profile
                    else -> R.id.nav_home
                }
                if (navView.selectedItemId != menuId) {
                    navView.selectedItemId = menuId
                }
            }
        })

        // Sinkronisasi BottomNav click ke ViewPager
        navView.setOnItemSelectedListener { item ->
            val position = when (item.itemId) {
                R.id.nav_home -> 0
                R.id.nav_acara -> 1
                R.id.nav_map -> 2
                R.id.nav_inbox -> 3
                R.id.nav_profile -> 4
                else -> 0
            }
            if (viewPager.currentItem != position) {
                viewPager.setCurrentItem(position, true) // Smooth scroll
            }
            true
        }
    }

    override fun onStart() {
        super.onStart()
        receiver = NongkiBroadcastReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction("android.provider.Telephony.SMS_RECEIVED")
            addAction("android.net.conn.CONNECTIVITY_CHANGE")
            addAction("android.net.wifi.WIFI_STATE_CHANGED")
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    fun setBottomNavigationVisibility(isVisible: Boolean) {
        val bottomAppBar: View = findViewById(R.id.bottomAppBar)
        bottomAppBar.visibility = if (isVisible) View.VISIBLE else View.GONE
        viewPager.isUserInputEnabled = isVisible
    }
}
