package com.example.quanlysinhvienlan1.activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class ForgetPasswordActivity : AppCompatActivity() {
    private var edtEmail: EditText? = null
    private var btnConfirm: Button? = null
    private var txtSignIn: TextView? = null
    private var auth: FirebaseAuth? = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)
        // Ánh xạ
        mapping()

        // Bấm xác nhận
        clickConfirm()

        // Bấm chuyển sang màn đăng nhập
        intentSignIn()
    }



    //Ánh xạ
    private fun mapping() {
        edtEmail = findViewById(R.id.edt_Email)
        btnConfirm = findViewById(R.id.btn_Confirm)
        txtSignIn = findViewById(R.id.txt_SignIn)
    }

    // Bấm xác nhận
    private fun clickConfirm() {
        btnConfirm?.setOnClickListener {
            val inputEmail = edtEmail?.text.toString().trim()
            if (inputEmail.isEmpty()) {
                edtEmail?.error = "Vui lòng nhập email"
            } else {
                auth?.sendPasswordResetEmail(inputEmail)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this, "Kiểm tra Email để đặt lại mật khẩu",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Gửi Email đặt lại mật khẩu thất bại. Vui lòng kiểm tra lại Email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
    // Intent đăng nhập
    private fun intentSignIn(){
        txtSignIn?.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}