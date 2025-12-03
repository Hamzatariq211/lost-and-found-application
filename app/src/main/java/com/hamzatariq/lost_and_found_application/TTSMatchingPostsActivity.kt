package com.hamzatariq.lost_and_found_application

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.hamzatariq.lost_and_found_application.adapters.TTSMatchingPostsAdapter
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import com.hamzatariq.lost_and_found_application.models.MatchingPost
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class TTSMatchingPostsActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var ttsControlBtn: ImageButton
    private lateinit var adapter: TTSMatchingPostsAdapter

    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var isSpeaking = false
    private val matchingPosts = mutableListOf<MatchingPost>()
    private var currentSpeakingIndex = 0

    private var lostItemId: Int = 0
    private var lostItemName: String = ""

    companion object {
        private const val TAG = "TTSMatchingPosts"
        const val EXTRA_LOST_ITEM_ID = "lost_item_id"
        const val EXTRA_LOST_ITEM_NAME = "lost_item_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tts_matching_posts)

        lostItemId = intent.getIntExtra(EXTRA_LOST_ITEM_ID, 0)
        lostItemName = intent.getStringExtra(EXTRA_LOST_ITEM_NAME) ?: ""

        setupViews()
        initializeTTS()
        loadMatchingPosts()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Matches for: $lostItemName"
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.matchingPostsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyText = findViewById(R.id.emptyText)
        ttsControlBtn = findViewById(R.id.ttsControlBtn)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TTSMatchingPostsAdapter(matchingPosts) { position ->
            highlightAndSpeakPost(position)
        }
        recyclerView.adapter = adapter

        ttsControlBtn.setOnClickListener {
            toggleSpeaking()
        }
    }

    private fun initializeTTS() {
        textToSpeech = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported")
                Toast.makeText(this, "TTS Language not supported", Toast.LENGTH_SHORT).show()
                isTtsInitialized = false
            } else {
                isTtsInitialized = true
                Log.d(TAG, "TTS initialized successfully")
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
            Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show()
            isTtsInitialized = false
        }
    }

    private fun loadMatchingPosts() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE

        Thread {
            try {
                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.GET_MATCHING_POSTS) + "?lost_item_id=$lostItemId")
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
                        matchingPosts.clear()

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
                            emptyText.text = "No matching posts found"
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            adapter.notifyDataSetChanged()

                            // Auto-start speaking when matches are loaded
                            if (isTtsInitialized) {
                                startSpeakingAllMatches()
                            }
                        }
                    } else {
                        emptyText.visibility = View.VISIBLE
                        emptyText.text = "No matches found"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading matching posts: ${e.message}", e)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    emptyText.visibility = View.VISIBLE
                    emptyText.text = "Error loading matches: ${e.message}"
                }
            }
        }.start()
    }

    private fun toggleSpeaking() {
        if (isSpeaking) {
            stopSpeaking()
        } else {
            startSpeakingAllMatches()
        }
    }

    private fun startSpeakingAllMatches() {
        if (!isTtsInitialized) {
            Toast.makeText(this, "Text-to-Speech not ready", Toast.LENGTH_SHORT).show()
            return
        }

        if (matchingPosts.isEmpty()) {
            Toast.makeText(this, "No matches to speak", Toast.LENGTH_SHORT).show()
            return
        }

        isSpeaking = true
        currentSpeakingIndex = 0
        updateTTSButton()

        val introText = "Found ${matchingPosts.size} matching posts for $lostItemName. Starting from the highest match."
        speakText(introText) {
            speakNextMatch()
        }
    }

    private fun speakNextMatch() {
        if (!isSpeaking || currentSpeakingIndex >= matchingPosts.size) {
            stopSpeaking()
            return
        }

        val post = matchingPosts[currentSpeakingIndex]

        // Highlight current post
        adapter.setHighlightedPosition(currentSpeakingIndex)

        // Scroll to current post
        recyclerView.smoothScrollToPosition(currentSpeakingIndex)

        val matchText = buildMatchText(post, currentSpeakingIndex + 1)

        speakText(matchText) {
            currentSpeakingIndex++
            speakNextMatch()
        }
    }

    private fun highlightAndSpeakPost(position: Int) {
        if (!isTtsInitialized) {
            Toast.makeText(this, "Text-to-Speech not ready", Toast.LENGTH_SHORT).show()
            return
        }

        stopSpeaking()
        isSpeaking = true
        currentSpeakingIndex = position
        updateTTSButton()

        adapter.setHighlightedPosition(position)

        val post = matchingPosts[position]
        val matchText = buildMatchText(post, position + 1)

        speakText(matchText) {
            isSpeaking = false
            updateTTSButton()
            adapter.setHighlightedPosition(-1)
        }
    }

    private fun buildMatchText(post: MatchingPost, matchNumber: Int): String {
        return """
            Match number $matchNumber. 
            Match score: ${post.match_score} percent.
            Item name: ${post.item_name}.
            Description: ${post.item_description}.
            Location: ${post.location}.
            Posted by: ${post.user.full_name}.
            Contact number: ${formatPhoneNumber(post.user.mobile_number)}.
            Status: ${post.status}.
        """.trimIndent()
    }

    private fun formatPhoneNumber(number: String): String {
        // Format phone number for better TTS pronunciation
        return number.replace("", " ").trim()
    }

    private fun speakText(text: String, onComplete: (() -> Unit)? = null) {
        textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "Started speaking: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "Finished speaking: $utteranceId")
                runOnUiThread {
                    onComplete?.invoke()
                }
            }

            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Error speaking: $utteranceId")
                runOnUiThread {
                    onComplete?.invoke()
                }
            }
        })

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "matchPost_${System.currentTimeMillis()}")
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
    }

    private fun stopSpeaking() {
        textToSpeech?.stop()
        isSpeaking = false
        currentSpeakingIndex = 0
        adapter.setHighlightedPosition(-1)
        updateTTSButton()
    }

    private fun updateTTSButton() {
        if (isSpeaking) {
            ttsControlBtn.setImageResource(android.R.drawable.ic_media_pause)
            ttsControlBtn.contentDescription = "Pause speaking"
        } else {
            ttsControlBtn.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            ttsControlBtn.contentDescription = "Start speaking"
        }
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (isSpeaking) {
            textToSpeech?.stop()
        }
    }
}

