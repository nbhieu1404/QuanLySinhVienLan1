package com.example.quanlysinhvienlan1.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth
import java.util.logging.Handler

class SplashActivity : AppCompatActivity() {
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val currentUser = auth.currentUser
        android.os.Handler().postDelayed({
            if(currentUser != null){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }, 1000)

    }
}