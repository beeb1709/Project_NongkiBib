package com.example.nongkibib.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("dob") val dob: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("campus") val campus: String?,
    @SerializedName("faculty") val faculty: String?,
    @SerializedName("studyProgram") val studyProgram: String?,
    @SerializedName("classYear") val classYear: String?,
    @SerializedName("points") val points: Int,
    @SerializedName("nongkiHours") val nongkiHours: Double,
    @SerializedName("ktmStatus") val ktmStatus: String, // "Unverified" | "Pending" | "Verified"
    @SerializedName("ktmPhoto") val ktmPhoto: String?,
    @SerializedName("selfiePhoto") val selfiePhoto: String?,
    @SerializedName("avatar") val avatar: String,
    @SerializedName("authProvider") val authProvider: String, // "local" | "google"
    @SerializedName("password") val password: String?
)
