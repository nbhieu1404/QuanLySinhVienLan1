package com.example.quanlysinhvienlan1.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val currentUser = auth.currentUser
        Handler().postDelayed({
            val intent = when {
                currentUser != null && currentUser.isEmailVerified -> Intent(
                    this,
                    MainActivity::class.java
                )

                else -> Intent(this, SignInActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_DELAY)
    }

    companion object {
        private const val SPLASH_DELAY = 1000L
    }
}
