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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var edtUsername: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var cbPolicy: CheckBox
    private lateinit var txtAccept: TextView
    private lateinit var txtPolicy: TextView
    private lateinit var btnSignUp: Button
    private lateinit var txtSignIn: TextView
    private lateinit var prbSignUp: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        // Ánh xạ
        mappingViews()
        // Bấm sự kiện
        clickEvent()

    }

    // Ánh xạ
    private fun mappingViews() {
        edtUsername = findViewById(R.id.edt_Username)
        edtEmail = findViewById(R.id.edt_Email)
        edtPassword = findViewById(R.id.edt_Password)
        edtConfirmPassword = findViewById(R.id.edt_ConfirmPassword)
        cbPolicy = findViewById(R.id.cb_Policy)
        txtPolicy = findViewById(R.id.txt_Policy)
        txtAccept = findViewById(R.id.txt_accept)
        btnSignUp = findViewById(R.id.btn_SignUp)
        txtSignIn = findViewById(R.id.txt_Signin)
        prbSignUp = findViewById(R.id.layout_ProgressBar)
    }

    private fun clickEvent() {
        edtEmail.setOnClickListener {
            resetErrorBoxEmail()
        }
        cbPolicy.setOnClickListener {
            resetErrorTextPolicy()
        }
        txtSignIn.setOnClickListener {
            intentSignIn()
        }

        btnSignUp.setOnClickListener {
            signUpUser()
        }
    }


    // Reset màu editText Email
    private fun resetErrorBoxEmail() {
        edtEmail.setTextColor(Color.BLACK)
    }

    // Reset màu textView chính sách
    private fun resetErrorTextPolicy() {
        txtPolicy.setTextColor(ContextCompat.getColor(this, R.color.BLUE_P_S))
        txtAccept.setTextColor(Color.BLACK)

    }

    // Intent đăng nhập
    private fun intentSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()

    }

    // Bấm đăng ký
    private fun signUpUser() {
        val inputUsername = edtUsername.text.toString().trim()
        val inputEmail = edtEmail.text.toString().trim()
        val inputPassword = edtPassword.text.toString().trim()
        val inputConfirmPassword = edtConfirmPassword.text.toString().trim()
        val inputPolicy = cbPolicy.isChecked

        when {
            inputUsername.isNullOrEmpty() -> edtUsername.error = "Vui lòng nhập tên người dùng"
            inputEmail.isNullOrEmpty() -> edtEmail.error = "Vui lòng nhập email"
            inputPassword.isNullOrEmpty() -> edtPassword.error = "Vui lòng nhập mật khẩu"
            inputConfirmPassword.isNullOrEmpty() -> edtConfirmPassword.error =
                "Vui lòng nhập lại mật khẩu"

            inputPassword != inputConfirmPassword -> edtConfirmPassword.error =
                "Mật khẩu không trùng khớp"

            !inputPolicy -> {
                txtPolicy.setTextColor(Color.RED)
                txtAccept.setTextColor(Color.RED)
            }

            inputUsername.isNotEmpty() && inputEmail.isNotEmpty()
                    && inputPassword.isNotEmpty() && inputConfirmPassword.isNotEmpty()
                    && inputPassword == inputConfirmPassword
                    && inputPolicy -> {
                showProgressBar(true)
                signUp(inputUsername, inputEmail, inputPassword)
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
                    val storageRef = firebaseStorage.reference
                    val defaultImageAvatar =
                        "https://firebasestorage.googleapis.com/v0/b/quanlysinhvienlan1.appspot.com/o/avatars%2FDefault%20image%20avatar.jpeg?alt=media&token=ab835105-98b2-46ba-8836-5304bc717878"
                    val defaultCoverImage =
                        "https://firebasestorage.googleapis.com/v0/b/quanlysinhvienlan1.appspot.com/o/coverImages%2FDefault%20cover%20image.png?alt=media&token=7d4b5a93-247d-485a-ab04-10951e967010"
                    // Thêm người dùng vào fireStore
                    user?.let {
                        val newUser = User(username, email, defaultImageAvatar, defaultCoverImage)
                        fireStore.collection("users")
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
                edtEmail.setTextColor(Color.RED)
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



    // Trạng thái của ProgressBar
    private fun showProgressBar(show: Boolean) {
        if (show) {
            prbSignUp.visibility = View.VISIBLE
            btnSignUp.isEnabled = false
            edtUsername.isEnabled = false
            edtEmail.isEnabled = false
            edtPassword.isEnabled = false
            edtConfirmPassword.isEnabled = false
            cbPolicy.isEnabled = false
            txtSignIn.isEnabled = false
        } else {
            prbSignUp.visibility = View.GONE
            btnSignUp.isEnabled = true
            edtUsername.isEnabled = true
            edtEmail.isEnabled = true
            edtPassword.isEnabled = true
            edtConfirmPassword.isEnabled = true
            cbPolicy.isEnabled = true
            txtSignIn.isEnabled = true
        }
    }
}