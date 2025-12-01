package com.hamzatariq.lost_and_found_application

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TestConnectionActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var testButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_connection)

        statusText = findViewById(R.id.statusText)
        testButton = findViewById(R.id.testButton)

        // Show current configuration
        statusText.text = """
            Environment: ${if (ApiConfig.getCurrentEnvironment() == ApiConfig.Environment.EMULATOR) "Emulator" else "Physical Device"}
            Base URL: ${ApiConfig.getBaseUrl()}
            
            Click 'Test Connection' to verify API connectivity
        """.trimIndent()

        testButton.setOnClickListener {
            testConnection()
        }
    }

    private fun testConnection() {
        statusText.text = "Testing connection...\n\nBase URL: ${ApiConfig.getBaseUrl()}"
        testButton.isEnabled = false

        Thread {
            try {
                val url = URL("${ApiConfig.getBaseUrl()}test_connection.php")
                Log.d("TestConnection", "Testing URL: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                Log.d("TestConnection", "Response Code: $responseCode")

                val response = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    "Error: HTTP $responseCode"
                }

                Log.d("TestConnection", "Response: $response")

                runOnUiThread {
                    testButton.isEnabled = true

                    if (responseCode == 200) {
                        try {
                            val json = JSONObject(response)
                            statusText.text = """
                                ✅ CONNECTION SUCCESS!
                                
                                Environment: ${if (ApiConfig.getCurrentEnvironment() == ApiConfig.Environment.EMULATOR) "Emulator" else "Physical Device"}
                                Base URL: ${ApiConfig.getBaseUrl()}
                                
                                Server Response:
                                Message: ${json.getString("message")}
                                Server IP: ${json.getString("server_ip")}
                                Your IP: ${json.getString("client_ip")}
                                Time: ${json.getString("timestamp")}
                            """.trimIndent()
                            Toast.makeText(this, "✅ API Connected Successfully!", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            statusText.text = "✅ Connected but error parsing response:\n$response"
                        }
                    } else {
                        statusText.text = """
                            ❌ CONNECTION FAILED
                            
                            URL: $url
                            HTTP Code: $responseCode
                            Response: $response
                            
                            Make sure:
                            1. XAMPP is running
                            2. Phone and PC on same WiFi
                            3. Firewall allows connections
                        """.trimIndent()
                        Toast.makeText(this, "❌ Connection Failed", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("TestConnection", "Exception: ${e.message}", e)
                runOnUiThread {
                    testButton.isEnabled = true
                    statusText.text = """
                        ❌ CONNECTION ERROR
                        
                        URL: ${ApiConfig.getBaseUrl()}test_connection.php
                        Error: ${e.message}
                        
                        Troubleshooting:
                        1. Is XAMPP running?
                        2. Phone on WiFi: 192.168.18.x?
                        3. PC IP still: 192.168.18.17?
                        4. Check Windows Firewall
                    """.trimIndent()
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}

