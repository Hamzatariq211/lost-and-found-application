package com.hamzatariq.lost_and_found_application

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import com.hamzatariq.lost_and_found_application.firebase.DataSyncService
import com.hamzatariq.lost_and_found_application.viewmodel.PostViewModel
import com.hamzatariq.lost_and_found_application.viewmodel.PostViewModelFactory
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class AddPostActivity : AppCompatActivity() {

    private var itemType = "lost" // Default to "lost"
    private lateinit var lostItemButton: LinearLayout
    private lateinit var foundItemButton: LinearLayout
    private lateinit var itemNameInput: TextInputEditText
    private lateinit var itemDescriptionInput: TextInputEditText
    private lateinit var locationInput: TextInputEditText
    private lateinit var uploadedImagePreview: ImageView
    private lateinit var postButton: MaterialButton
    private lateinit var postViewModel: PostViewModel
    
    private var selectedImageUri: Uri? = null
    private var imageBase64: String? = null

    companion object {
        private const val TAG = "AddPostActivity"
    }

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            uploadedImagePreview.setImageURI(it)
            // Convert to base64
            convertImageToBase64(it)
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied to read storage", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Initialize ViewModel
        postViewModel = ViewModelProvider(this, PostViewModelFactory(this))
            .get(PostViewModel::class.java)

        // Initialize views
        lostItemButton = findViewById(R.id.lostItemButton)
        foundItemButton = findViewById(R.id.foundItemButton)
        itemNameInput = findViewById(R.id.itemNameInput)
        itemDescriptionInput = findViewById(R.id.itemDescriptionInput)
        locationInput = findViewById(R.id.locationInput)
        uploadedImagePreview = findViewById(R.id.uploadedImagePreview)
        postButton = findViewById(R.id.postButton)

        // Observe post state
        postViewModel.postState.observe(this) { state ->
            when (state) {
                is PostViewModel.PostState.Loading -> {
                    postButton.isEnabled = false
                    postButton.text = "Creating Post..."
                }
                is PostViewModel.PostState.Success -> {
                    postButton.isEnabled = true
                    postButton.text = "Post"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    // Navigate back to home
                    finish()
                }
                is PostViewModel.PostState.Error -> {
                    postButton.isEnabled = true
                    postButton.text = "Post"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                PostViewModel.PostState.Idle -> {
                    postButton.isEnabled = true
                    postButton.text = "Post"
                }
            }
        }

        // Handle back button click
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Handle Lost Item Button click
        lostItemButton.setOnClickListener {
            itemType = "lost"
            updateItemTypeSelection()
        }

        // Handle Found Item Button click
        foundItemButton.setOnClickListener {
            itemType = "found"
            updateItemTypeSelection()
        }

        // Handle image upload container click
        val imageUploadContainer = findViewById<android.widget.FrameLayout>(R.id.imageUploadContainer)
        imageUploadContainer.setOnClickListener {
            checkPermissionAndPickImage()
        }

        // Handle Notify Me button click
        val notifyMeButton = findViewById<LinearLayout>(R.id.notifyMeButton)
        notifyMeButton.setOnClickListener {
            Toast.makeText(this, "Notification feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Handle post button click
        postButton.setOnClickListener {
            submitPost()
        }

        // Set default selection to Lost Item
        updateItemTypeSelection()
    }

    private fun checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openImagePicker()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Below Android 13
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openImagePicker()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun convertImageToBase64(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Compress and resize image
            val resizedBitmap = resizeBitmap(bitmap, 1024, 1024)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()
            
            // Convert to base64
            imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            
            Toast.makeText(this, "Image loaded successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        
        if (width > height) {
            newWidth = maxWidth
            newHeight = (maxWidth / aspectRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun submitPost() {
        val itemName = itemNameInput.text.toString().trim()
        val itemDescription = itemDescriptionInput.text.toString().trim()
        val location = locationInput.text.toString().trim()

        // Validation
        when {
            itemName.isEmpty() -> {
                itemNameInput.error = "Item name is required"
                itemNameInput.requestFocus()
                return
            }
            itemDescription.isEmpty() -> {
                itemDescriptionInput.error = "Item description is required"
                itemDescriptionInput.requestFocus()
                return
            }
            location.isEmpty() -> {
                locationInput.error = "Location is required"
                locationInput.requestFocus()
                return
            }
        }

        // If it's a lost item, create a lost item report
        if (itemType == "lost") {
            createLostItemReport(itemName, itemDescription, location)
        } else {
            // Create found post via ViewModel
            postViewModel.createPost(
                itemName = itemName,
                itemDescription = itemDescription,
                location = location,
                itemType = itemType,
                imageBase64 = imageBase64
            )
        }
    }

    private fun createLostItemReport(itemName: String, itemDescription: String, location: String) {
        postButton.isEnabled = false
        postButton.text = "Creating Report..."

        Thread {
            try {
                val sharedPref = getSharedPreferences("lost_and_found_prefs", MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", 0)

                if (userId == 0) {
                    runOnUiThread {
                        postButton.isEnabled = true
                        postButton.text = "Post"
                        Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.CREATE_LOST_ITEM))
                Log.d(TAG, "Creating lost item report - URL: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val postData = "user_id=$userId&item_name=$itemName&item_description=$itemDescription&location_lost=$location"
                connection.outputStream.write(postData.toByteArray())

                val responseCode = connection.responseCode
                Log.d(TAG, "Lost item report response code: $responseCode")

                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Lost item report response: $response")

                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    postButton.isEnabled = true
                    postButton.text = "Post"

                    if (jsonResponse.getBoolean("success")) {
                        // Sync to Firebase after successful creation
                        val dataSyncService = DataSyncService(this)
                        dataSyncService.syncPostsToFirebase(userId)
                        Log.d(TAG, "Lost item synced to Firebase")

                        Toast.makeText(this, "Lost item reported successfully! We'll notify you of matches.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while creating lost item report: ${e.message}", e)
                runOnUiThread {
                    postButton.isEnabled = true
                    postButton.text = "Post"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updateItemTypeSelection() {
        if (itemType == "lost") {
            // Lost item is selected - white background
            lostItemButton.setBackgroundResource(R.drawable.item_type_button_selected_bg)
            foundItemButton.setBackgroundResource(R.drawable.item_type_button_bg)
        } else {
            // Found item is selected - white background
            foundItemButton.setBackgroundResource(R.drawable.item_type_button_selected_bg)
            lostItemButton.setBackgroundResource(R.drawable.item_type_button_bg)
        }
    }
}
