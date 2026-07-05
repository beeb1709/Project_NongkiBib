package com.example.nongkibib

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.example.nongkibib.api.ApiClient
import com.example.nongkibib.model.LocationUpdate
import com.example.nongkibib.model.SpotItem
import com.example.nongkibib.websocket.WebSocketClientManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, WebSocketClientManager.WebSocketListenerCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var webSocketManager: WebSocketClientManager? = null

    // Simpan list marker aktif
    private val userMarkers = HashMap<String, Marker>()
    private val spotMarkers = ArrayList<Marker>()

    // Ganti dengan User ID & nama dari session Google OAuth / Login Anda
    private var currentUserId = "usr-guest-1"
    private var currentUserName = "Habibie"
    private var currentUserAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Dapatkan data user login dari session / intent
        intent.getStringExtra("USER_ID")?.let { currentUserId = it }
        intent.getStringExtra("USER_NAME")?.let { currentUserName = it }
        intent.getStringExtra("USER_AVATAR")?.let { currentUserAvatar = it }

        // Inisialisasi Google Maps Fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Inisialisasi Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        // Hubungkan WebSocket untuk pelacakan lokasi real-time
        webSocketManager = WebSocketClientManager(this)
        webSocketManager?.connect()

        setupLocationUpdates()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 detik
            fastestInterval = 5000 // 5 detik
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun setupLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    onMyLocationChanged(location)
                }
            }
        }
    }

    private fun onMyLocationChanged(location: Location) {
        val myLatLng = LatLng(location.latitude, location.longitude)

        // 1. Kirim koordinat baru ke server via real-time WebSocket
        webSocketManager?.sendLocationUpdate(
            userId = currentUserId,
            name = currentUserName,
            avatar = currentUserAvatar,
            lat = location.latitude,
            lng = location.longitude
        )

        // 2. HTTP Fallback update (opsional jika WebSocket terputus)
        val body = mapOf(
            "userId" to currentUserId,
            "lat" to location.latitude,
            "lng" to location.longitude
        )
        ApiClient.instance.updateLocation(body).enqueue(object : Callback<com.example.nongkibib.api.LocationUpdateResponse> {
            override fun onResponse(call: Call<com.example.nongkibib.api.LocationUpdateResponse>, response: Response<com.example.nongkibib.api.LocationUpdateResponse>) {
                // Berhasil memperbarui via HTTP fallback
            }

            override fun onFailure(call: Call<com.example.nongkibib.api.LocationUpdateResponse>, t: Throwable) {
                // Gagal, namun websocket tetap berjalan mandiri
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        // Cek izin akses lokasi GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        mMap.isMyLocationEnabled = true
        startLocationUpdates()

        // Ambil lokasi default nongkrong dari MySQL
        loadNongkiSpots()

        // Pusatkan kamera ke Bandung / Jakarta
        val defaultCenter = LatLng(-6.2235, 106.8080)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 14f))
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun loadNongkiSpots() {
        ApiClient.instance.getSpots().enqueue(object : Callback<List<SpotItem>> {
            override fun onResponse(call: Call<List<SpotItem>>, response: Response<List<SpotItem>>) {
                if (response.isSuccessful && response.body() != null) {
                    spotMarkers.forEach { it.remove() }
                    spotMarkers.clear()

                    for (spot in response.body()!!) {
                        val pos = LatLng(spot.lat, spot.lng)
                        val iconRes = when (spot.type) {
                            "Cafe" -> BitmapDescriptorFactory.HUE_ORANGE
                            "Perpus" -> BitmapDescriptorFactory.HUE_AZURE
                            else -> BitmapDescriptorFactory.HUE_GREEN
                        }

                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title(spot.name)
                                .snippet("${spot.type} - Rating ⭐${spot.rating}\n${spot.address}")
                                .icon(BitmapDescriptorFactory.defaultMarker(iconRes))
                        )
                        if (marker != null) {
                            spotMarkers.add(marker)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<SpotItem>>, t: Throwable) {
                Toast.makeText(this@MapsActivity, "Gagal mengambil tempat nongkrong dari database", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- CALLBACK DARI WEBSOCKET (REALTIME LOCATION BROADCAST) ---
    override fun onLocationBroadcastReceived(locations: List<LocationUpdate>) {
        runOnUiThread {
            for (loc in locations) {
                // Abaikan jika itu posisi diri sendiri
                if (loc.userId == currentUserId) continue

                val userPos = LatLng(loc.lat, loc.lng)
                val activeMarker = userMarkers[loc.userId]

                if (activeMarker != null) {
                    // Update posisi marker yang sudah ada
                    activeMarker.position = userPos
                    activeMarker.snippet = "Aktif sekarang | ${loc.name}"
                } else {
                    // Tambahkan marker baru untuk teman / penerima beasiswa lain yang aktif
                    val newMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(userPos)
                            .title(loc.name)
                            .snippet("Aktif sekarang")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    )
                    if (newMarker != null) {
                        userMarkers[loc.userId] = newMarker
                    }
                }
            }
        }
    }

    override fun onNewMessageReceived(message: com.example.nongkibib.model.MessageItem) {
        // Log atau tampilkan notifikasi pop-up pesan baru saat sedang melihat peta
    }

    override fun onConnectionStatusChanged(connected: Boolean) {
        runOnUiThread {
            val status = if (connected) "Terhubung Realtime!" else "Koneksi Bermasalah. Menghubungkan ulang..."
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        webSocketManager?.disconnect()
    }
}
