package com.example.quanlysinhvienlan1.activity

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
    // Intent đăng nhập
    // Dialog chính sách
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var edtEmail: EditText? = null
//    private var edtPassword = findViewById<EditText>(R.id.edt_Password)
//    private var edtConfirmPassword = findViewById<EditText>(R.id.edt_ConfirmPassword)
//    private var cbPolicy = findViewById<CheckBox>(R.id.cb_Policy)
//    private var txtPolicy = findViewById<TextView>(R.id.txt_Policy)
//    private var txtAccept = findViewById<TextView>(R.id.txt_accept)
//    private var btnSignUp = findViewById<Button>(R.id.btn_SignUp)
//    private var txtSignin = findViewById<TextView>(R.id.txt_Signin)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        var edtUsername = findViewById<EditText>(R.id.edt_Username)
        edtEmail = findViewById(R.id.edt_Email)
        var edtPassword = findViewById<EditText>(R.id.edt_Password)
        var edtConfirmPassword = findViewById<EditText>(R.id.edt_ConfirmPassword)
        var cbPolicy = findViewById<CheckBox>(R.id.cb_Policy)
        var txtPolicy = findViewById<TextView>(R.id.txt_Policy)
        var txtAccept = findViewById<TextView>(R.id.txt_accept)
        var btnSignUp = findViewById<Button>(R.id.btn_SignUp)
        var txtSignin = findViewById<TextView>(R.id.txt_Signin)

        // Bấm nhập email
        edtEmail?.setOnClickListener {
            edtEmail?.setTextColor(Color.BLACK)
        }

        // Bấm Chấp thuận chính sách
        cbPolicy.setOnClickListener {
            txtPolicy.setTextColor(ContextCompat.getColor(this, R.color.BLUE_P_S))
            txtAccept.setTextColor(Color.BLACK)
        }

        // Bấm đăng ký
        btnSignUp.setOnClickListener {
            var inputUsername = edtUsername.text.toString().trim()
            var inputEmail = edtEmail?.text.toString().trim()
            var inputPassword = edtPassword.text.toString().trim()
            var inputConfirmPassword = edtConfirmPassword.text.toString().trim()
            var inputPolicy = cbPolicy.isChecked
            if (inputUsername.isEmpty()) {
                edtUsername.error = "Vui lòng nhập tên người dùng"
            }else if (inputEmail.isEmpty()) {
                edtEmail?.error = "Vui lòng nhập email"
            } else if (inputPassword.isEmpty()) {
                edtPassword.error = "Vui lòng nhập mật khẩu"
            } else if (inputConfirmPassword.isEmpty()) {
                edtConfirmPassword.error = "Vui lòng nhập lại mật khẩu"
            } else if (inputPassword != inputConfirmPassword) {
                edtConfirmPassword.error = "Mật khẩu không trùng khớp"
            } else if (!inputPolicy) {
                txtPolicy.setTextColor(Color.RED)
                txtAccept.setTextColor(Color.RED)
            } else if (inputUsername.isNotEmpty() && inputEmail.isNotEmpty()
                    && inputPassword.isNotEmpty () && inputConfirmPassword.isNotEmpty()
                    && (inputPassword == inputConfirmPassword)
                    && inputPolicy) {
                signUp(inputUsername, inputEmail, inputPassword)
            }
//            }else{
//                checkEmailExists(inputEmail){emailExists ->
//                    if(emailExists) {
//                        Toast.makeText(this, "Địa chỉ Email đã tồn tại",
//                                        Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
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

}