package com.habittracker.ml.data.remote

import com.habittracker.ml.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== AUTH ==========

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<LoginResponse>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<ApiResponse<UserDto>>

    @PUT("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Any>>

    // ========== HABITS ==========

    @GET("api/habits")
    suspend fun getHabits(
        @Query("include_checkins") includeCheckIns: Boolean = false
    ): Response<ApiResponse<List<HabitDto>>>

    @GET("api/habits/{id}")
    suspend fun getHabit(
        @Path("id") id: Long,
        @Query("include_checkins") includeCheckIns: Boolean = false
    ): Response<ApiResponse<HabitDto>>

    @POST("api/habits")
    suspend fun createHabit(@Body request: HabitCreateRequest): Response<ApiResponse<HabitDto>>

    @PUT("api/habits/{id}")
    suspend fun updateHabit(
        @Path("id") id: Long,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ApiResponse<HabitDto>>

    @DELETE("api/habits/{id}")
    suspend fun deleteHabit(@Path("id") id: Long): Response<ApiResponse<Any>>

    // ========== CHECK-INS ==========

    @GET("api/checkins")
    suspend fun getCheckIns(
        @Query("habit_id") habitId: Long? = null,
        @Query("date") date: String? = null
    ): Response<ApiResponse<List<CheckInDto>>>

    @POST("api/checkins")
    suspend fun createCheckIn(@Body request: CheckInCreateRequest): Response<ApiResponse<CheckInDto>>

    @POST("api/checkins/bulk")
    suspend fun bulkCreateCheckIns(@Body request: BulkCheckInRequest): Response<ApiResponse<List<CheckInDto>>>

    @DELETE("api/checkins/{id}")
    suspend fun deleteCheckIn(@Path("id") id: Long): Response<ApiResponse<Any>>

    // ========== SYNC ==========

    @GET("api/sync")
    suspend fun getSyncData(): Response<ApiResponse<ExportData>>

    @POST("api/sync")
    suspend fun syncData(@Body request: SyncRequest): Response<ApiResponse<SyncResponse>>

    // ========== TEMPLATES ==========

    @GET("api/templates")
    suspend fun getTemplates(): Response<ApiResponse<List<HabitDto>>>
}
