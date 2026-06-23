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

        // Sinkronisasi ViewPager swipe ke BottomNav
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> navView.selectedItemId = R.id.nav_home
                    1 -> navView.selectedItemId = R.id.nav_map
                    2 -> navView.selectedItemId = R.id.nav_acara
                    3 -> navView.selectedItemId = R.id.nav_inbox
                    4 -> navView.selectedItemId = R.id.nav_profile
                }
            }
        })

        // Sinkronisasi BottomNav click ke ViewPager
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_map -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_acara -> {
                    viewPager.currentItem = 2
                    true
                }
                R.id.nav_inbox -> {
                    viewPager.currentItem = 3
                    true
                }
                R.id.nav_profile -> {
                    viewPager.currentItem = 4
                    true
                }
                else -> false
            }
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
