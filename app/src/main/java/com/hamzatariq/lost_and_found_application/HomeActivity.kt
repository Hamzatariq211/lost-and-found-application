package com.hamzatariq.lost_and_found_application

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import com.hamzatariq.lost_and_found_application.api.Post
import com.hamzatariq.lost_and_found_application.viewmodel.PostViewModel
import com.hamzatariq.lost_and_found_application.viewmodel.PostViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var postViewModel: PostViewModel
    private lateinit var postsContainer: LinearLayout
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var searchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Initialize ViewModel
        postViewModel = ViewModelProvider(this, PostViewModelFactory(this))
            .get(PostViewModel::class.java)

        // Initialize views
        postsContainer = findViewById(R.id.postsContainer)
        searchInput = findViewById(R.id.searchInput)

        // Create loading indicator and empty state programmatically
        loadingIndicator = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 40, 0, 40)
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }
        }

        emptyStateText = TextView(this).apply {
            text = "No posts found"
            textSize = 16f
            setTextColor(getColor(R.color.white))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 40, 0, 40)
            }
        }

        // Observe posts list state
        postViewModel.postsListState.observe(this) { state ->
            when (state) {
                is PostViewModel.PostsListState.Loading -> {
                    showLoading()
                }
                is PostViewModel.PostsListState.Success -> {
                    hideLoading()
                    displayPosts(state.posts)
                }
                is PostViewModel.PostsListState.Error -> {
                    hideLoading()
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    showEmptyState()
                }
                PostViewModel.PostsListState.Idle -> {
                    hideLoading()
                }
            }
        }

        // Handle profile icon click to navigate to settings
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Handle chat icon click to navigate to Chats
        val chatIcon = findViewById<ImageView>(R.id.chatIcon)
        chatIcon.setOnClickListener {
            startActivity(Intent(this, ChatsActivity::class.java))
        }

        // Handle My Posts icon click to navigate to My Posts
        val myPostsIcon = findViewById<ImageView>(R.id.myPostsIcon)
        myPostsIcon.setOnClickListener {
            startActivity(Intent(this, MyPostsActivity::class.java))
        }

        // Handle add button click to navigate to Add Post
        val addButton = findViewById<FrameLayout>(R.id.addButton)
        addButton.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }

        // Handle search functionality
        val searchIcon = findViewById<ImageView>(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                postViewModel.getPosts(searchQuery = query)
            } else {
                postViewModel.getPosts()
            }
        }

        // Load posts
        postViewModel.getPosts()
    }

    override fun onResume() {
        super.onResume()
        // Refresh posts when returning to this activity
        postViewModel.getPosts()
    }

    private fun showLoading() {
        postsContainer.removeAllViews()
        postsContainer.addView(loadingIndicator)
    }

    private fun hideLoading() {
        postsContainer.removeView(loadingIndicator)
    }

    private fun showEmptyState() {
        postsContainer.removeAllViews()
        postsContainer.addView(emptyStateText)
    }

    private fun displayPosts(posts: List<Post>) {
        postsContainer.removeAllViews()

        if (posts.isEmpty()) {
            showEmptyState()
            return
        }

        posts.forEach { post ->
            val postView = createPostView(post)
            postsContainer.addView(postView)
        }
    }

    private fun createPostView(post: Post): View {
        val inflater = LayoutInflater.from(this)
        val postView = inflater.inflate(R.layout.item_post, postsContainer, false)

        val itemName = postView.findViewById<TextView>(R.id.itemName)
        val itemLocation = postView.findViewById<TextView>(R.id.itemLocation)
        val itemType = postView.findViewById<TextView>(R.id.itemType)
        val itemImage = postView.findViewById<ImageView>(R.id.itemImage)
        val noImageText = postView.findViewById<TextView>(R.id.noImageText)

        // Set data
        itemName.text = post.item_name
        itemLocation.text = "Found in: ${post.location}"
        itemType.text = post.item_type.uppercase()

        // Set item type color
        if (post.item_type == "lost") {
            itemType.setTextColor(getColor(android.R.color.holo_red_light))
        } else {
            itemType.setTextColor(getColor(android.R.color.holo_green_light))
        }

        // Load base64 image directly
        if (!post.image_base64.isNullOrEmpty()) {
            noImageText.visibility = View.GONE
            itemImage.visibility = View.VISIBLE

            try {
                // Decode base64 to bitmap
                val base64String = if (post.image_base64.contains("base64,")) {
                    post.image_base64.substring(post.image_base64.indexOf("base64,") + 7)
                } else {
                    post.image_base64
                }

                val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                itemImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // If base64 decoding fails, show no image
                itemImage.visibility = View.GONE
                noImageText.visibility = View.VISIBLE
                android.util.Log.e("HomeActivity", "Failed to decode base64 image", e)
            }
        } else {
            itemImage.visibility = View.GONE
            noImageText.visibility = View.VISIBLE
        }

        // Handle click to show details
        postView.setOnClickListener {
            showPostDetailsDialog(post)
        }

        return postView
    }

    private fun showPostDetailsDialog(post: Post) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product_details, null)

        val productNameView = dialogView.findViewById<TextView>(R.id.dialogProductName)
        val productLocationView = dialogView.findViewById<TextView>(R.id.dialogProductLocation)
        val productDescriptionView = dialogView.findViewById<TextView>(R.id.dialogProductDescription)
        val productImageView = dialogView.findViewById<ImageView>(R.id.dialogProductImage)
        val closeButton = dialogView.findViewById<MaterialButton>(R.id.dialogCloseButton)
        val contactButton = dialogView.findViewById<MaterialButton>(R.id.dialogContactButton)

        productNameView.text = post.item_name
        productLocationView.text = "Found in: ${post.location}"
        productDescriptionView.text = post.item_description

        // Load base64 image in dialog
        if (!post.image_base64.isNullOrEmpty()) {
            productImageView?.let {
                it.visibility = View.VISIBLE

                try {
                    // Decode base64 to bitmap
                    val base64String = if (post.image_base64.contains("base64,")) {
                        post.image_base64.substring(post.image_base64.indexOf("base64,") + 7)
                    } else {
                        post.image_base64
                    }

                    val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    it.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    it.visibility = View.GONE
                    android.util.Log.e("HomeActivity", "Failed to decode base64 image in dialog", e)
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        contactButton.setOnClickListener {
            // Create chat room and navigate to chat screen
            dialog.dismiss()
            createChatRoomAndNavigate(post)
        }

        dialog.show()
    }

    private fun createChatRoomAndNavigate(post: Post) {
        val sharedPref = getSharedPreferences("lost_and_found_prefs", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("user_id", 0)

        android.util.Log.d("ChatDebug", "Current User ID: $currentUserId")
        android.util.Log.d("ChatDebug", "Post User ID: ${post.user.user_id}")
        android.util.Log.d("ChatDebug", "Post ID: ${post.post_id}")

        // Don't allow user to chat with themselves
        if (currentUserId == 0) {
            Toast.makeText(this, "Please log in again to use chat", Toast.LENGTH_LONG).show()
            return
        }

        if (currentUserId == post.user.user_id) {
            Toast.makeText(this, "You cannot contact your own post", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        Toast.makeText(this, "Creating chat...", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                val url = java.net.URL(ApiConfig.getUrl(ApiConfig.Endpoints.CHAT_API))
                android.util.Log.d("ChatDebug", "Connecting to: ${url.toString()}")

                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "action=create_room&post_id=${post.post_id}&sender_id=$currentUserId&receiver_id=${post.user.user_id}"
                android.util.Log.d("ChatDebug", "Post Data: $postData")

                connection.outputStream.write(postData.toByteArray())

                val responseCode = connection.responseCode
                android.util.Log.d("ChatDebug", "Response Code: $responseCode")

                val response = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream?.bufferedReader()?.readText() ?: "No error message"
                }

                android.util.Log.d("ChatDebug", "Response: $response")

                val jsonResponse = org.json.JSONObject(response)

                runOnUiThread {
                    if (jsonResponse.getBoolean("success")) {
                        val roomId = jsonResponse.getInt("room_id")
                        android.util.Log.d("ChatDebug", "Chat room created/opened: $roomId")

                        // Navigate to chat screen
                        val intent = Intent(this, ChatScreenActivity::class.java).apply {
                            putExtra("room_id", roomId)
                            putExtra("other_user_name", post.user.full_name)
                            putExtra("item_name", post.item_name)
                        }
                        startActivity(intent)
                    } else {
                        val errorMsg = jsonResponse.optString("message", "Unknown error")
                        android.util.Log.e("ChatDebug", "API Error: $errorMsg")
                        Toast.makeText(this, "Failed to create chat: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatDebug", "Exception occurred", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }.start()
    }
}
