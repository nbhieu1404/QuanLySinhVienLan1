package com.example.quanlysinhvienlan1.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.quanlysinhvienlan1.R

class SignUpActivity : AppCompatActivity() {
    // Kết nối Firebase
    // Intent đăng nhập
    // Dialog chính sách
    // Làm logo ẩn
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        var edtUsername = findViewById<EditText>(R.id.edt_Username)
        var edtEmail = findViewById<EditText>(R.id.edt_Email)
        var edtPassword = findViewById<EditText>(R.id.edt_Password)
        var edtConfirmPassword = findViewById<EditText>(R.id.edt_ConfirmPassword)
        var cbPolicy = findViewById<CheckBox>(R.id.cb_Policy)
        var txtPolicy = findViewById<TextView>(R.id.txt_Policy)
        var txtAccept = findViewById<TextView>(R.id.txt_accept)
        var btnSignUp = findViewById<Button>(R.id.btn_SignUp)
        var txtSignin = findViewById<TextView>(R.id.txt_Signin)

        cbPolicy.setOnClickListener {
            txtPolicy.setTextColor(ContextCompat.getColor(this, R.color.BLUE_P_S))
            txtAccept.setTextColor(Color.BLACK)
        }
        btnSignUp.setOnClickListener {
            var inputUsername = edtUsername.text.toString().trim()
            var inputEmail = edtEmail.text.toString().trim()
            var inputPassword = edtPassword.text.toString().trim()
            var inputConfirmPassword = edtConfirmPassword.text.toString().trim()
            var inputPolicy = cbPolicy.isChecked
            if (inputUsername.isEmpty()) {
                edtUsername.error = "Vui lòng nhập tên người dùng"
            } else if (inputEmail.isEmpty()) {
                edtEmail.error = "Vui lòng nhập email"
            } else if (inputPassword.isEmpty()) {
                edtPassword.error = "Vui lòng nhập mật khẩu"
            } else if (inputConfirmPassword.isEmpty()) {
                edtConfirmPassword.error = "Vui lòng nhập lại mật khẩu"
            } else if (inputPassword != inputConfirmPassword) {
                edtConfirmPassword.error = "Mật khẩu không trùng khớp"
            } else if (!inputPolicy) {
                txtPolicy.setTextColor(Color.RED)
                txtAccept.setTextColor(Color.RED)
            }
        }
    }

}