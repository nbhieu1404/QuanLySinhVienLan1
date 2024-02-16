package com.example.quanlysinhvienlan1.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.User
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
    private var layoutProgressBar: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        // Ánh xạ
        mapping()
        // Bấm sự kiện
        clickEvent()

    }

    private fun clickEvent() {
        resetErrorBoxEmail()
        resetErrorTextPolicy()
        clickSignUp()
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
        layoutProgressBar = findViewById(R.id.layout_ProgressBar)
    }

    // Reset màu editText Email
    private fun resetErrorBoxEmail() {
        edtEmail?.setOnClickListener {
            edtEmail?.setTextColor(Color.BLACK)
        }
    }

    // Reset màu textView chính sách
    private fun resetErrorTextPolicy() {
        cbPolicy?.setOnClickListener {
            txtPolicy?.setTextColor(ContextCompat.getColor(this, R.color.BLUE_P_S))
            txtAccept?.setTextColor(Color.BLACK)
        }
    }

    // Bấm đăng ký
    private fun clickSignUp() {
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
                && inputPassword.isNotEmpty() && inputConfirmPassword.isNotEmpty()
                && (inputPassword == inputConfirmPassword)
                && inputPolicy
            ) {
                showProgressBar(true)
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
                    // Thêm người dùng vào FireStore
                    user?.let {
                        val newUser = User(username, email, "", "")
                        firestore.collection("users")
                            .document(it.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                verifyEmail()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Lỗi khi thêm người dùng vào Firestore: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showProgressBar(false)
                            }
                    }
//                    verifyEmail()
                } else {
                    handleSignUpError(task.exception)
                }
            }
    }

    //Lỗi khi đăng ký
    private fun handleSignUpError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                showProgressBar(true)
                // Địa chỉ email đã được sử dụng
                edtEmail?.setTextColor(Color.RED)
                Toast.makeText(
                    this, "Địa chỉ Email đã tồn tại",
                    Toast.LENGTH_SHORT
                ).show()
                showProgressBar(false)
            }

            is FirebaseAuthWeakPasswordException -> {
                // Mật khẩu yếu
                Toast.makeText(
                    this, "Mật khẩu tối thiểu gồm 6 kí tự",
                    Toast.LENGTH_SHORT
                ).show()
                showProgressBar(false)
            }

            else -> {
                // Xử lý lỗi mặc định
                Toast.makeText(
                    this, "Đăng ký thất bại. Vui lòng thử lại.",
                    Toast.LENGTH_SHORT
                ).show()
                showProgressBar(false)
            }
        }
    }

    //Xác thực Email đăng ký
    private fun verifyEmail() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnSuccessListener { _ ->
                Toast.makeText(
                    this, "Vui lòng kiểm tra email để xác thực",
                    Toast.LENGTH_SHORT
                ).show()
                showProgressBar(false)
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
            ?.addOnFailureListener {
                Toast.makeText(
                    this, "Gửi xác thực email thất bại",
                    Toast.LENGTH_SHORT
                ).show()
                showProgressBar(false)
            }
    }

    // Intent đăng nhập
    private fun intentSignIn() {
        txtSignIn?.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Trạng thái của ProgressBar
    private fun showProgressBar(show: Boolean) {
        if (show) {
            layoutProgressBar?.visibility = View.VISIBLE
            btnSignUp?.isEnabled = false
            edtUsername?.isEnabled = false
            edtEmail?.isEnabled = false
            edtPassword?.isEnabled = false
            edtConfirmPassword?.isEnabled = false
            cbPolicy?.isEnabled = false
            txtSignIn?.isEnabled = false
        } else {
            layoutProgressBar?.visibility = View.GONE
            btnSignUp?.isEnabled = true
            edtUsername?.isEnabled = true
            edtEmail?.isEnabled = true
            edtPassword?.isEnabled = true
            edtConfirmPassword?.isEnabled = true
            cbPolicy?.isEnabled = true
            txtSignIn?.isEnabled = true
        }
    }
}