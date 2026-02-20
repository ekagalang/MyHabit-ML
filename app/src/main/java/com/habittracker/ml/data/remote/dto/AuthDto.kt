package com.habittracker.ml.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class LoginResponse(
    val token: String,
    val user: UserDto
)

data class ChangePasswordRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    @SerializedName("profile_photo")
    val profilePhoto: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)
