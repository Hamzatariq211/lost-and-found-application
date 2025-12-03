package com.hamzatariq.lost_and_found_application

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import com.hamzatariq.lost_and_found_application.api.Post
import com.hamzatariq.lost_and_found_application.models.LostItem
import com.hamzatariq.lost_and_found_application.models.MatchingPost
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MyPostsActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: LinearLayout
    private val myPosts = mutableListOf<Post>()
    private val myLostItems = mutableListOf<LostItem>()
    private lateinit var postsAdapter: MyPostsAdapter
    private lateinit var lostItemsAdapter: LostItemsAdapter

    private var currentTab = 0 // 0 = Found Posts, 1 = Lost Items

    companion object {
        private const val TAG = "MyPostsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_posts)

        setupViews()
        loadMyPosts()
    }

    override fun onResume() {
        super.onResume()
        if (currentTab == 0) {
            loadMyPosts()
        } else {
            loadMyLostItems()
        }
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Posts"
        toolbar.setNavigationOnClickListener { finish() }

        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.myPostsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)

        // Setup TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Found Posts"))
        tabLayout.addTab(tabLayout.newTab().setText("Lost Items"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                if (currentTab == 0) {
                    loadMyPosts()
                } else {
                    loadMyLostItems()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        postsAdapter = MyPostsAdapter(myPosts) { post, action ->
            when (action) {
                "mark_resolved" -> showMarkResolvedDialog(post)
                "delete" -> showDeleteDialog(post)
                "reactivate" -> updatePostStatus(post.post_id, "active")
            }
        }

        lostItemsAdapter = LostItemsAdapter(myLostItems) { lostItem ->
            showMatchingPosts(lostItem)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadMyLostItems() {
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.GONE

        Thread {
            try {
                val sharedPref = getSharedPreferences("lost_and_found_prefs", MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", 0)

                if (userId == 0) {
                    Log.w(TAG, "User ID is 0, user not logged in")
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                    }
                    return@Thread
                }

                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.GET_MY_LOST_ITEMS) + "?user_id=$userId")
                Log.d(TAG, "Fetching lost items from URL: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                Log.d(TAG, "Response code: $responseCode")

                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Response body: $response")

                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    progressBar.visibility = View.GONE

                    if (jsonResponse.getBoolean("success")) {
                        myLostItems.clear()
                        val itemsArray = jsonResponse.getJSONArray("data")
                        Log.d(TAG, "Successfully loaded ${itemsArray.length()} lost items")

                        for (i in 0 until itemsArray.length()) {
                            val itemJson = itemsArray.getJSONObject(i)

                            myLostItems.add(
                                LostItem(
                                    lost_item_id = itemJson.getInt("lost_item_id"),
                                    item_name = itemJson.getString("item_name"),
                                    item_description = itemJson.getString("item_description"),
                                    location_lost = itemJson.getString("location_lost"),
                                    status = itemJson.getString("status"),
                                    created_at = itemJson.getString("created_at")
                                )
                            )
                        }

                        if (myLostItems.isEmpty()) {
                            Log.d(TAG, "No lost items available")
                            emptyView.visibility = View.VISIBLE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            recyclerView.adapter = lostItemsAdapter
                            lostItemsAdapter.notifyDataSetChanged()
                        }
                    } else {
                        val errorMsg = jsonResponse.optString("message", "Unknown error")
                        Log.e(TAG, "Failed to load lost items: $errorMsg")
                        emptyView.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading lost items: ${e.message}", e)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun showMatchingPosts(lostItem: LostItem) {
        // Show dialog with matching found posts
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_matching_posts, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val titleText = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val matchingRecyclerView = dialogView.findViewById<RecyclerView>(R.id.matchingPostsRecyclerView)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.matchingProgressBar)
        val emptyText = dialogView.findViewById<TextView>(R.id.emptyMatchingText)
        val closeButton = dialogView.findViewById<MaterialButton>(R.id.closeButton)

        titleText.text = "Possible Matches for: ${lostItem.item_name}"
        closeButton.setOnClickListener { dialog.dismiss() }

        matchingRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load matching posts
        loadMatchingPosts(lostItem, matchingRecyclerView, progressBar, emptyText)

        dialog.show()
    }

    private fun loadMatchingPosts(
        lostItem: LostItem,
        recyclerView: RecyclerView,
        progressBar: ProgressBar,
        emptyText: TextView
    ) {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE

        Thread {
            try {
                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.GET_MATCHING_POSTS) + "?lost_item_id=${lostItem.lost_item_id}")
                Log.d(TAG, "Fetching matching posts from URL: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Matching posts response: $response")

                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    progressBar.visibility = View.GONE

                    if (jsonResponse.getBoolean("success")) {
                        val postsArray = jsonResponse.getJSONArray("data")
                        val matchingPosts = mutableListOf<MatchingPost>()

                        for (i in 0 until postsArray.length()) {
                            val postJson = postsArray.getJSONObject(i)
                            val userJson = postJson.getJSONObject("user")

                            matchingPosts.add(
                                MatchingPost(
                                    post_id = postJson.getInt("post_id"),
                                    item_name = postJson.getString("item_name"),
                                    item_description = postJson.getString("item_description"),
                                    location = postJson.getString("location"),
                                    item_type = postJson.getString("item_type"),
                                    image_base64 = postJson.optString("image_base64"),
                                    status = postJson.getString("status"),
                                    created_at = postJson.getString("created_at"),
                                    match_score = postJson.getInt("match_score"),
                                    user = com.hamzatariq.lost_and_found_application.models.PostUser(
                                        user_id = userJson.getInt("user_id"),
                                        username = userJson.getString("username"),
                                        full_name = userJson.getString("full_name"),
                                        mobile_number = userJson.getString("mobile_number")
                                    )
                                )
                            )
                        }

                        if (matchingPosts.isEmpty()) {
                            emptyText.visibility = View.VISIBLE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            recyclerView.adapter = MatchingPostsAdapter(matchingPosts)
                        }
                    } else {
                        emptyText.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading matching posts: ${e.message}", e)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    emptyText.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun loadMyPosts() {
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.GONE

        Thread {
            try {
                val sharedPref = getSharedPreferences("lost_and_found_prefs", MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", 0)

                if (userId == 0) {
                    Log.w(TAG, "User ID is 0, user not logged in")
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                    }
                    return@Thread
                }

                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.GET_MY_POSTS) + "?user_id=$userId")
                Log.d(TAG, "Fetching posts from URL: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                Log.d(TAG, "Response code: $responseCode")

                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Response body: $response")

                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    progressBar.visibility = View.GONE

                    if (jsonResponse.getBoolean("success")) {
                        myPosts.clear()
                        val postsArray = jsonResponse.getJSONArray("data")
                        Log.d(TAG, "Successfully loaded ${postsArray.length()} posts")

                        for (i in 0 until postsArray.length()) {
                            val postJson = postsArray.getJSONObject(i)
                            val userJson = postJson.getJSONObject("user")

                            myPosts.add(
                                Post(
                                    post_id = postJson.getInt("post_id"),
                                    item_name = postJson.getString("item_name"),
                                    item_description = postJson.getString("item_description"),
                                    location = postJson.getString("location"),
                                    item_type = postJson.getString("item_type"),
                                    image_base64 = postJson.optString("image_base64"),
                                    status = postJson.getString("status"),
                                    created_at = postJson.getString("created_at"),
                                    user = com.hamzatariq.lost_and_found_application.api.PostUser(
                                        user_id = userJson.getInt("user_id"),
                                        username = userJson.getString("username"),
                                        full_name = userJson.getString("full_name"),
                                        mobile_number = userJson.getString("mobile_number")
                                    )
                                )
                            )
                        }

                        if (myPosts.isEmpty()) {
                            Log.d(TAG, "No posts available")
                            emptyView.visibility = View.VISIBLE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            recyclerView.adapter = postsAdapter
                            postsAdapter.notifyDataSetChanged()
                        }
                    } else {
                        val errorMsg = jsonResponse.optString("message", "Unknown error")
                        Log.e(TAG, "Failed to load posts: $errorMsg")
                        emptyView.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading posts: ${e.message}", e)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun showMarkResolvedDialog(post: Post) {
        AlertDialog.Builder(this)
            .setTitle("Mark as Returned")
            .setMessage("Has this ${post.item_type} item been returned to its owner?")
            .setPositiveButton("Yes, Mark as Returned") { _, _ ->
                updatePostStatus(post.post_id, "resolved")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(post: Post) {
        AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                updatePostStatus(post.post_id, "deleted")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePostStatus(postId: Int, newStatus: String) {
        progressBar.visibility = View.VISIBLE

        Thread {
            try {
                val sharedPref = getSharedPreferences("lost_and_found_prefs", MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", 0)

                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.UPDATE_STATUS))
                Log.d(TAG, "Updating post status - URL: $url, postId: $postId, status: $newStatus")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val postData = "post_id=$postId&user_id=$userId&status=$newStatus"
                connection.outputStream.write(postData.toByteArray())

                val responseCode = connection.responseCode
                Log.d(TAG, "Status update response code: $responseCode")

                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Status update response: $response")

                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    progressBar.visibility = View.GONE

                    if (jsonResponse.getBoolean("success")) {
                        val message = jsonResponse.getString("message")
                        Log.d(TAG, "Status update successful: $message")
                        loadMyPosts() // Reload the list
                    } else {
                        val message = jsonResponse.getString("message")
                        Log.e(TAG, "Status update failed: $message")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while updating post status: ${e.message}", e)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                }
            }
        }.start()
    }

    // Adapter class
    class MyPostsAdapter(
        private val posts: List<Post>,
        private val onAction: (Post, String) -> Unit
    ) : RecyclerView.Adapter<MyPostsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemImage: ImageView = view.findViewById(R.id.itemImage)
            val noImageText: TextView = view.findViewById(R.id.noImageText)
            val itemName: TextView = view.findViewById(R.id.itemName)
            val itemType: TextView = view.findViewById(R.id.itemType)
            val itemStatus: TextView = view.findViewById(R.id.itemStatus)
            val itemLocation: TextView = view.findViewById(R.id.itemLocation)
            val markResolvedBtn: MaterialButton = view.findViewById(R.id.markResolvedBtn)
            val reactivateBtn: MaterialButton = view.findViewById(R.id.reactivateBtn)
            val deleteBtn: MaterialButton = view.findViewById(R.id.deleteBtn)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_my_post, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = posts[position]

            holder.itemName.text = post.item_name
            holder.itemType.text = post.item_type.uppercase()
            holder.itemLocation.text = "Location: ${post.location}"

            // Set status
            when (post.status) {
                "active" -> {
                    holder.itemStatus.text = "Active"
                    holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_light))
                    holder.markResolvedBtn.visibility = View.VISIBLE
                    holder.reactivateBtn.visibility = View.GONE
                }
                "resolved" -> {
                    holder.itemStatus.text = "Returned to Owner ✓"
                    holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_blue_light))
                    holder.markResolvedBtn.visibility = View.GONE
                    holder.reactivateBtn.visibility = View.VISIBLE
                }
                else -> {
                    holder.itemStatus.text = "Deleted"
                    holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_light))
                    holder.markResolvedBtn.visibility = View.GONE
                    holder.reactivateBtn.visibility = View.VISIBLE
                }
            }

            // Load image
            if (!post.image_base64.isNullOrEmpty()) {
                holder.noImageText.visibility = View.GONE
                holder.itemImage.visibility = View.VISIBLE

                try {
                    val base64String = if (post.image_base64.contains("base64,")) {
                        post.image_base64.substring(post.image_base64.indexOf("base64,") + 7)
                    } else {
                        post.image_base64
                    }
                    val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    holder.itemImage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    holder.itemImage.visibility = View.GONE
                    holder.noImageText.visibility = View.VISIBLE
                }
            } else {
                holder.itemImage.visibility = View.GONE
                holder.noImageText.visibility = View.VISIBLE
            }

            // Button click listeners
            holder.markResolvedBtn.setOnClickListener {
                onAction(post, "mark_resolved")
            }

            holder.reactivateBtn.setOnClickListener {
                onAction(post, "reactivate")
            }

            holder.deleteBtn.setOnClickListener {
                onAction(post, "delete")
            }
        }

        override fun getItemCount() = posts.size
    }

    // Adapter class for Lost Items
    class LostItemsAdapter(
        private val lostItems: List<LostItem>,
        private val onItemClick: (LostItem) -> Unit
    ) : RecyclerView.Adapter<LostItemsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemName: TextView = view.findViewById(R.id.itemName)
            val itemDescription: TextView = view.findViewById(R.id.itemDescription)
            val itemLocation: TextView = view.findViewById(R.id.itemLocation)
            val itemStatus: TextView = view.findViewById(R.id.itemStatus)
            val createdAt: TextView = view.findViewById(R.id.createdAt)
            val matchingPostsBtn: MaterialButton = view.findViewById(R.id.matchingPostsBtn)
            val speakerBtn: android.widget.ImageButton = view.findViewById(R.id.speakerBtn)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lost_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val lostItem = lostItems[position]

            holder.itemName.text = lostItem.item_name
            holder.itemDescription.text = lostItem.item_description
            holder.itemLocation.text = "Location Lost: ${lostItem.location_lost}"
            holder.createdAt.text = "Reported on: ${lostItem.created_at}"

            // Set status
            when (lostItem.status) {
                "active" -> {
                    holder.itemStatus.text = "Active"
                    holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_light))
                }
                "resolved" -> {
                    holder.itemStatus.text = "Returned to Owner ✓"
                    holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_blue_light))
                }
                else -> {
                    holder.itemStatus.text = "Deleted"
                    holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_light))
                }
            }

            // Speaker button click - Launch TTS Activity
            holder.speakerBtn.setOnClickListener {
                val context = holder.itemView.context
                val intent = android.content.Intent(context, TTSMatchingPostsActivity::class.java)
                intent.putExtra(TTSMatchingPostsActivity.EXTRA_LOST_ITEM_ID, lostItem.lost_item_id)
                intent.putExtra(TTSMatchingPostsActivity.EXTRA_LOST_ITEM_NAME, lostItem.item_name)
                context.startActivity(intent)
            }

            // Matching posts button click - Show dialog (original functionality)
            holder.matchingPostsBtn.setOnClickListener {
                onItemClick(lostItem)
            }
        }

        override fun getItemCount() = lostItems.size
    }

    // Adapter class for Matching Posts (within the dialog)
    class MatchingPostsAdapter(
        private val posts: List<MatchingPost>
    ) : RecyclerView.Adapter<MatchingPostsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemName: TextView = view.findViewById(R.id.itemName)
            val itemDescription: TextView = view.findViewById(R.id.itemDescription)
            val itemLocation: TextView = view.findViewById(R.id.itemLocation)
            val itemType: TextView = view.findViewById(R.id.itemType)
            val itemStatus: TextView = view.findViewById(R.id.itemStatus)
            val matchScore: TextView = view.findViewById(R.id.matchScore)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_matching_post, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = posts[position]

            holder.itemName.text = post.item_name
            holder.itemDescription.text = post.item_description
            holder.itemLocation.text = "Location: ${post.location}"
            holder.itemType.text = "Type: ${post.item_type}"
            holder.matchScore.text = "Match Score: ${post.match_score}"

            // Set status
            when (post.status) {
                "active" -> {
                    holder.itemStatus.text = "Active"
                    holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_light))
                }
                "resolved" -> {
                    holder.itemStatus.text = "Returned to Owner ✓"
                    holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_blue_light))
                }
                else -> {
                    holder.itemStatus.text = "Deleted"
                    holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_light))
                }
            }
        }

        override fun getItemCount() = posts.size
    }
}
