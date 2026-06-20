package com.example.nongkibib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

class NongkiBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_AIRPLANE_MODE_CHANGED, 
            "android.net.conn.CONNECTIVITY_CHANGE",
            "android.net.wifi.WIFI_STATE_CHANGED" -> {
                if (isOnline(context)) {
                    Toast.makeText(context, "Aplikasi Online", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Aplikasi Offline: Tidak ada koneksi internet", Toast.LENGTH_LONG).show()
                }
            }
            Intent.ACTION_BATTERY_LOW -> {
                Toast.makeText(context, "Baterai Lemah! Segera isi daya.", Toast.LENGTH_LONG).show()
            }
            Intent.ACTION_POWER_CONNECTED -> {
                Toast.makeText(context, "Charger Terhubung", Toast.LENGTH_SHORT).show()
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                Toast.makeText(context, "Charger Dilepas", Toast.LENGTH_SHORT).show()
            }
            "android.provider.Telephony.SMS_RECEIVED" -> {
                Toast.makeText(context, "Ada SMS Masuk", Toast.LENGTH_SHORT).show()
            }
            "android.location.PROVIDERS_CHANGED" -> {
                Toast.makeText(context, "Status GPS/Lokasi berubah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
