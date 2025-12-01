package com.hamzatariq.lost_and_found_application.api

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import java.net.NetworkInterface

object ApiConfig {

    // Configuration options - Change this to switch environments
    enum class Environment {
        EMULATOR,           // Use 10.0.2.2 for Android Emulator
        PHYSICAL_DEVICE,    // Use your local network IP
        CUSTOM              // Use a custom IP
    }

    // **CHANGE THIS TO MATCH YOUR SETUP**
    private var currentEnvironment = Environment.EMULATOR

    // For physical devices, set your computer's local IP address here
    // Find it by running 'ipconfig' (Windows) or 'ifconfig' (Mac/Linux)
    private const val LOCAL_NETWORK_IP = "192.168.18.17" // Your PC's actual IP address

    // For custom setup
    private const val CUSTOM_IP = "192.168.18.17"

    // API paths
    private const val API_PATH = "/lost_and_found_api/"

    /**
     * Get the base URL based on current environment
     */
    fun getBaseUrl(): String {
        // Auto-detect on every call to ensure it's always correct
        autoDetectEnvironment()

        val baseUrl = when (currentEnvironment) {
            Environment.EMULATOR -> "http://10.0.2.2$API_PATH"
            Environment.PHYSICAL_DEVICE -> "http://$LOCAL_NETWORK_IP$API_PATH"
            Environment.CUSTOM -> "http://$CUSTOM_IP$API_PATH"
        }

        android.util.Log.d("ApiConfig", "Environment: $currentEnvironment, Base URL: $baseUrl")
        return baseUrl
    }

    /**
     * Auto-detect environment (emulator vs physical device)
     * Call this early in your app to set the right environment
     */
    fun autoDetectEnvironment() {
        currentEnvironment = if (isEmulator()) {
            Environment.EMULATOR
        } else {
            Environment.PHYSICAL_DEVICE
        }
    }

    /**
     * Manually set the environment
     */
    fun setEnvironment(environment: Environment) {
        currentEnvironment = environment
    }

    /**
     * Set custom IP address
     */
    fun setCustomIP(ip: String) {
        currentEnvironment = Environment.CUSTOM
        // You can store this in SharedPreferences for persistence
    }

    /**
     * Get current environment
     */
    fun getCurrentEnvironment(): Environment = currentEnvironment

    /**
     * Check if running on emulator
     */
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.contains("vbox")
                || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * Get the device's local IP address (useful for debugging)
     */
    fun getDeviceIP(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.address.size == 4) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // Full URL builders for convenience
    fun getUrl(endpoint: String): String = "${getBaseUrl()}$endpoint"

    // Specific endpoints
    object Endpoints {
        const val UPDATE_FCM_TOKEN = "user/update_fcm_token.php"
        const val GET_PROFILE = "user/get_profile.php"
        const val UPDATE_PROFILE = "user/update_profile.php"
        const val GET_MY_LOST_ITEMS = "posts/get_my_lost_items.php"
        const val GET_MATCHING_POSTS = "posts/get_matching_posts.php"
        const val GET_MY_POSTS = "posts/get_my_posts.php"
        const val UPDATE_STATUS = "posts/update_status.php"
        const val CHAT_API = "chat/chat_api.php"
        const val CREATE_LOST_ITEM = "posts/create_lost_item.php"
        const val GET_MESSAGES = "chat/chat_api.php"
        const val SEND_MESSAGE = "chat/chat_api.php"
        const val MARK_READ = "chat/chat_api.php"
        const val GET_ROOMS = "chat/chat_api.php"
        const val GET_LOST_ITEMS = "posts/get_lost_items.php"
        const val GET_NOTIFICATIONS = "notifications/get_notifications.php"
        const val BASE_URL = ""  // This will be handled by getBaseUrl()
    }
}
