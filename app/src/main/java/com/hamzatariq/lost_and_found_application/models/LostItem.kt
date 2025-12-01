package com.hamzatariq.lost_and_found_application.models

data class LostItem(
    val lost_item_id: Int,
    val item_name: String,
    val item_description: String,
    val location_lost: String,
    val status: String,
    val created_at: String
)

data class MatchingPost(
    val post_id: Int,
    val item_name: String,
    val item_description: String,
    val location: String,
    val item_type: String,
    val image_base64: String?,
    val status: String,
    val created_at: String,
    val match_score: Int,
    val user: PostUser
)

data class PostUser(
    val user_id: Int,
    val username: String,
    val full_name: String,
    val mobile_number: String
)

