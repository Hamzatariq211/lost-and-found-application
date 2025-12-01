package com.hamzatariq.lost_and_found_application.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

data class SignupRequest(
    val full_name: String,
    val username: String,
    val email: String,
    val mobile_number: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: UserData?
)

data class UserData(
    val user_id: Int,
    val username: String,
    val email: String,
    val full_name: String,
    val mobile_number: String?,
    val token: String
)

// Post related data classes
data class CreatePostRequest(
    val item_name: String,
    val item_description: String,
    val location: String,
    val item_type: String, // "lost" or "found"
    val item_image_base64: String? = null
)

data class PostResponse(
    val success: Boolean,
    val message: String,
    val data: PostData?
)

data class PostData(
    val post_id: Int,
    val item_name: String,
    val item_type: String,
    val location: String,
    val image_url: String?
)

// Get posts response
data class GetPostsResponse(
    val success: Boolean,
    val count: Int,
    val data: List<Post>
)

data class Post(
    val post_id: Int,
    val item_name: String,
    val item_description: String,
    val location: String,
    val item_type: String,
    val image_base64: String?,  // Changed from image_url to image_base64
    val status: String,
    val created_at: String,
    val user: PostUser
)

data class PostUser(
    val user_id: Int,
    val username: String,
    val full_name: String,
    val mobile_number: String
)

interface ApiService {
    
    @POST("auth/signup.php")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>
    
    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("user/get_profile.php")
    suspend fun getProfile(@Header("Authorization") token: String): Response<AuthResponse>

    @POST("posts/create_post_base64.php")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body request: CreatePostRequest
    ): Response<PostResponse>

    @GET("posts/get_posts.php")
    suspend fun getPosts(
        @Query("item_type") itemType: String? = null,
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<GetPostsResponse>
}
