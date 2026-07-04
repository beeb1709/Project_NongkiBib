package com.example.nongkibib.api

import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

object ApiClient {
    // Ganti dengan alamat IP server backend Anda (misalnya http://10.0.2.2:3000 untuk Android Emulator)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private var retrofit: Retrofit? = null

    val instance: ApiService
        get() {
            if (retrofit == null) {
                // Mengelola session cookie secara otomatis agar login state tetap terjaga
                val cookieManager = CookieManager().apply {
                    setCookiePolicy(CookiePolicy.ACCEPT_ALL)
                }

                val okHttpClient = OkHttpClient.Builder()
                    .cookieJar(JavaNetCookieJar(cookieManager))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(ApiService::class.java)
        }
}
