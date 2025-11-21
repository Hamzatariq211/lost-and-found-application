package com.hamzatariq.lost_and_found_application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LoginMobileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_mobile)

        // Hide the action bar for clean look
        supportActionBar?.hide()
    }
}

