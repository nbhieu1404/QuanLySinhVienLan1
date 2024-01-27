package com.example.quanlysinhvienlan1.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private var edtEmail: EditText? = null
    private var edtPassword: EditText? = null
    private var btnSignIn: Button? = null
    private var txtForgetPassword: TextView? = null
    private var layoutSignUp: LinearLayout? = null
    private var layoutProgressBar: RelativeLayout? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        // Ánh xạ
        mapping()
        // CLick sự kiện
        clickEvent()

    }

    // Ánh xạ
    private fun mapping() {
        edtEmail = findViewById(R.id.edt_Email)
        edtPassword = findViewById(R.id.edt_Password)
        btnSignIn = findViewById(R.id.btn_SignIn)
        txtForgetPassword = findViewById(R.id.txt_ForgetPassword)
        layoutSignUp = findViewById(R.id.layout_SignUp)
        layoutProgressBar = findViewById(R.id.layout_ProgressBar)
    }

    private fun clickEvent() {
        clickSignIn()
        intentSignUp()
        intentForgetPassword()
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
                showProgressBar(true)
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
                            showProgressBar(false)
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            Toast.makeText(
                                this, "Email chưa được xác thực",
                                Toast.LENGTH_SHORT
                            ).show()
                            showProgressBar(false)
                        }
                    }
                } else {
                    Toast.makeText(
                        this, "Đăng nhập thất bại. Vui lòng kiểm tra thông tin đăng nhập",
                        Toast.LENGTH_SHORT
                    ).show()
                    showProgressBar(false)
                }
            }
    }

    // Intent đăng ký
    private fun intentSignUp() {
        layoutSignUp?.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // Intent quên mật khẩu
    private fun intentForgetPassword() {
        txtForgetPassword?.setOnClickListener {
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }
    }
    // Load Progress Bar
    private fun showProgressBar(show: Boolean) {
        if (show) {
            layoutProgressBar?.visibility = View.VISIBLE
            btnSignIn?.isEnabled = false
            edtEmail?.isEnabled = false
            edtPassword?.isEnabled = false
            txtForgetPassword?.isEnabled = false
            layoutSignUp?.isEnabled = false
        } else {
            layoutProgressBar?.visibility = View.GONE
            edtEmail?.isEnabled = true
            edtPassword?.isEnabled = true
            btnSignIn?.isEnabled = true
            txtForgetPassword?.isEnabled = true
            layoutSignUp?.isEnabled = true
        }
    }

    //Thoát ứng dụng
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showExitDialog()
    }

    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Thoát ứng dụng")
            .setMessage("Bạn có chắc chắn muốn thoát khỏi ứng dụng không?")
            .setPositiveButton("Có") { dialogInterface: DialogInterface, i: Int ->
                finish()
            }
            .setNegativeButton("Không") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }
}