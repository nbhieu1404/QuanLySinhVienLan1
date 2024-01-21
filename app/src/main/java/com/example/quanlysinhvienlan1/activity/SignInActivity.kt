package com.example.quanlysinhvienlan1.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private var edtEmail: EditText? = null
    private var edtPassword: EditText? = null
    private var btnSignIn: Button? = null
    private var txtForgetPassword: TextView? = null
    private var layoutSignUp: LinearLayout? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        // Ánh xạ
        mapping()
        // Bấm đăng nhập
        clickSignIn()
        // Bấm chuyển sang màn đăng ký
        intentSignUp()
    }
    // Ánh xạ
    private fun mapping() {
        edtEmail = findViewById(R.id.edt_Email)
        edtPassword = findViewById(R.id.edt_Password)
        btnSignIn = findViewById(R.id.btn_SignIn)
        txtForgetPassword = findViewById(R.id.txt_ForgetPassword)
        layoutSignUp = findViewById(R.id.layout_SignUp)
    }

    // Bấm đăng nhập
    private fun clickSignIn() {
        btnSignIn?.setOnClickListener {
            val inputEmail = edtEmail?.text.toString()
            val inputPassword = edtPassword?.text.toString()

            if (inputEmail.isEmpty()) {
                edtEmail?.error = "Vui lòng nhập email"
            } else if (inputPassword.isEmpty()) {
                edtPassword?.error = "Vui lòng nhập mật khẩu"
            } else {
                signInUser(inputEmail, inputPassword)
            }
        }
    }

    // Kiểm tra tài khoản trước khi đăng nhập/ Xác thực
    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val isEmailVerified = user.isEmailVerified
                        if (isEmailVerified) {
                            Toast.makeText(
                                this, "Đăng nhập thành công",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this, "Email chưa được xác thực",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this, "Đăng nhập thất bại. Vui lòng kiểm tra thông tin đăng nhập",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Intent đăng ký
    private fun intentSignUp(){
        layoutSignUp?.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}