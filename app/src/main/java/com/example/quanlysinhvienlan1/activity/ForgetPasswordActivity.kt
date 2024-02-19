package com.example.quanlysinhvienlan1.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {
    private lateinit var edtEmail: EditText
    private lateinit var btnConfirm: Button
    private lateinit var txtSignIn: TextView
    private lateinit var prbForgetPassword: RelativeLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)
        auth = FirebaseAuth.getInstance()
        // Ánh xạ
        mappingViews()

        // Thiết lập sự kiện click
        setUpClickEvents()
    }

    // Ánh xạ các view
    private fun mappingViews() {
        edtEmail = findViewById(R.id.edt_Email)
        btnConfirm = findViewById(R.id.btn_Confirm)
        txtSignIn = findViewById(R.id.txt_SignIn)
        prbForgetPassword = findViewById(R.id.layout_ProgressBar)
    }

    // Thiết lập sự kiện click
    private fun setUpClickEvents() {
        btnConfirm.setOnClickListener {
            handleConfirmButtonClick()
        }
        txtSignIn.setOnClickListener {
            navigateToSignInActivity()
        }
    }

    // Xử lý sự kiện khi nút xác nhận được nhấn
    private fun handleConfirmButtonClick() {
        val inputEmail = edtEmail.text.toString().trim()
        if (inputEmail.isEmpty()) {
            edtEmail.error = "Vui lòng nhập email"
        } else {
            showProgressBar(true)
            auth.sendPasswordResetEmail(inputEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this, "Kiểm tra Email để đặt lại mật khẩu",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, SignInActivity::class.java)
                        showProgressBar(false)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Gửi Email đặt lại mật khẩu thất bại. Vui lòng kiểm tra lại Email",
                            Toast.LENGTH_SHORT
                        ).show()
                        showProgressBar(false)
                    }
                }
        }
    }


    // Chuyển đến màn hình đăng nhập
    private fun navigateToSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Hiển thị hoặc ẩn thanh tiến trình
    private fun showProgressBar(show: Boolean) {
        prbForgetPassword.visibility = if (show) View.VISIBLE else View.GONE
        btnConfirm.isEnabled = !show
        edtEmail.isEnabled = !show
    }
}
