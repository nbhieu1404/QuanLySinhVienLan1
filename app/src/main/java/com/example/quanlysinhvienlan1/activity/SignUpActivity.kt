package com.example.quanlysinhvienlan1.activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    // Tạo FireStore
    // Dialog chính sách
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var edtUsername: EditText? = null
    private var edtEmail: EditText? = null
    private var edtPassword: EditText? = null
    private var edtConfirmPassword: EditText? = null
    private var cbPolicy: CheckBox? = null
    private var txtAccept: TextView? = null
    private var txtPolicy: TextView? = null
    private var btnSignUp: Button? = null
    private var txtSignIn: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        // Ánh xạ
        mapping()
        // Bấm nhập email
        resetErrorBoxEmail()
        // Bấm Chấp thuận chính sách
        resetErrorTextPolicy()
        // Bấm đăng ký
        clickSignUp()
        // Bấm intent đăng nhập
        intentSignIn()
    }

    // Ánh xạ
    private fun mapping() {
        edtUsername = findViewById(R.id.edt_Username)
        edtEmail = findViewById(R.id.edt_Email)
        edtPassword = findViewById(R.id.edt_Password)
        edtConfirmPassword = findViewById(R.id.edt_ConfirmPassword)
        cbPolicy = findViewById(R.id.cb_Policy)
        txtPolicy = findViewById(R.id.txt_Policy)
        txtAccept = findViewById(R.id.txt_accept)
        btnSignUp = findViewById(R.id.btn_SignUp)
        txtSignIn = findViewById(R.id.txt_Signin)
    }

    // Reset màu editText Email
    private fun resetErrorBoxEmail(){
        edtEmail?.setOnClickListener {
            edtEmail?.setTextColor(Color.BLACK)
        }
    }

    // Reset màu textView chính sách
    private fun resetErrorTextPolicy(){
        cbPolicy?.setOnClickListener {
            txtPolicy?.setTextColor(ContextCompat.getColor(this, R.color.BLUE_P_S))
            txtAccept?.setTextColor(Color.BLACK)
        }
    }

    // Bấm đăng ký
    private fun clickSignUp(){
        btnSignUp?.setOnClickListener {
            var inputUsername = edtUsername?.text.toString().trim()
            var inputEmail = edtEmail?.text.toString().trim()
            var inputPassword = edtPassword?.text.toString().trim()
            var inputConfirmPassword = edtConfirmPassword?.text.toString().trim()
            var inputPolicy = cbPolicy?.isChecked

            if (inputUsername.isNullOrEmpty()) {
                edtUsername?.error = "Vui lòng nhập tên người dùng"
            } else if (inputEmail.isNullOrEmpty()) {
                edtEmail?.error = "Vui lòng nhập email"
            } else if (inputPassword.isNullOrEmpty()) {
                edtPassword?.error = "Vui lòng nhập mật khẩu"
            } else if (inputConfirmPassword.isNullOrEmpty()) {
                edtConfirmPassword?.error = "Vui lòng nhập lại mật khẩu"
            } else if (inputPassword != inputConfirmPassword) {
                edtConfirmPassword?.error = "Mật khẩu không trùng khớp"
            } else if (inputPolicy != true) {
                txtPolicy?.setTextColor(Color.RED)
                txtAccept?.setTextColor(Color.RED)
            } else if (inputUsername.isNotEmpty() && inputEmail.isNotEmpty()
                && inputPassword.isNotEmpty () && inputConfirmPassword.isNotEmpty()
                && (inputPassword == inputConfirmPassword)
                && inputPolicy) {
                signUp(inputUsername, inputEmail, inputPassword)
            }
        }
    }
    // Kiểm tra Email đã tồn tại
    private fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    val emailExists = signInMethods?.isNotEmpty() == true
                    callback(emailExists)
                } else {
                    // Xử lý lỗi nếu cần thiết
                    callback(false)
                }
            }
    }

    // Đăng ký
    private fun signUp(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Đăng ký thành công, lấy thông tin người dùng
                    val user = auth.currentUser

                    // Lưu thông tin vào Realtime Database hoặc Firestore
//                    saveUserInfoToDatabase(username, email)
//                    Toast.makeText(
//                        this, "Đăng ký thành công",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    verifyEmail()
                } else {
                    handleSignUpError(task.exception)
                }
            }
    }

    //Lỗi khi đăng ký
    private fun handleSignUpError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                // Địa chỉ email đã được sử dụng
                edtEmail?.setTextColor(Color.RED)
                Toast.makeText(
                    this, "Địa chỉ Email đã tồn tại",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is FirebaseAuthWeakPasswordException -> {
                // Mật khẩu yếu
                Toast.makeText(
                    this, "Mật khẩu tối thiểu gồm 6 kí tự",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                // Xử lý lỗi mặc định
                Toast.makeText(
                    this, "Đăng ký thất bại. Vui lòng thử lại.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //Xác thực Email đăng ký
    private fun verifyEmail(){
        auth.currentUser?.sendEmailVerification()
            ?.addOnSuccessListener { _ ->
                Toast.makeText(
                    this, "Vui lòng kiểm tra email để xác thực",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ?.addOnFailureListener {
                Toast.makeText(
                    this, "Gửi xác thực email thất bại",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Intent đăng nhập
    private fun intentSignIn(){
        txtSignIn?.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}