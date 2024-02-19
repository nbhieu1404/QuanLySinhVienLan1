package com.example.quanlysinhvienlan1.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var txtForgetPassword: TextView
    private lateinit var layoutSignUp: LinearLayout
    private lateinit var prbSignIn: RelativeLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()
        // Ánh xạ
        mappingViews()
        // Bấm sự kiện
        setUpClickEvents()
    }

    // Ánh xạ các view
    private fun mappingViews() {
        edtEmail = findViewById(R.id.edt_Email)
        edtPassword = findViewById(R.id.edt_Password)
        btnSignIn = findViewById(R.id.btn_SignIn)
        txtForgetPassword = findViewById(R.id.txt_ForgetPassword)
        layoutSignUp = findViewById(R.id.layout_SignUp)
        prbSignIn = findViewById(R.id.layout_ProgressBar)
    }

    // Thiết lập sự kiện click
    private fun setUpClickEvents() {
        btnSignIn.setOnClickListener {
            handleSignInButtonClick()
        }
        layoutSignUp.setOnClickListener {
            navigateToSignUpActivity()
        }
        txtForgetPassword.setOnClickListener {
            navigateToForgetPasswordActivity()
        }
    }

    // Xử lý sự kiện khi nút đăng nhập được nhấn
    private fun handleSignInButtonClick() {
        val inputEmail = edtEmail.text.toString()
        val inputPassword = edtPassword.text.toString()

        if (inputEmail.isEmpty()) {
            edtEmail.error = "Vui lòng nhập email"
        } else if (inputPassword.isEmpty()) {
            edtPassword.error = "Vui lòng nhập mật khẩu"
        } else {
            showProgressBar(true)
            signInUser(inputEmail, inputPassword)
        }
    }

    // Xử lý đăng nhập người dùng
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

    // Chuyển đến màn hình đăng ký
    private fun navigateToSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    // Chuyển đến màn hình quên mật khẩu
    private fun navigateToForgetPasswordActivity() {
        val intent = Intent(this, ForgetPasswordActivity::class.java)
        startActivity(intent)
    }

    // Hiển thị hoặc ẩn thanh tiến trình
    private fun showProgressBar(show: Boolean) {
        prbSignIn.visibility = if (show) View.VISIBLE else View.GONE
        btnSignIn.isEnabled = !show
        edtEmail.isEnabled = !show
        edtPassword.isEnabled = !show
        txtForgetPassword.isEnabled = !show
        layoutSignUp.isEnabled = !show
    }

    // Xử lý khi nhấn nút back
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showExitDialog()
    }

    // Hiển thị hộp thoại xác nhận thoát
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
