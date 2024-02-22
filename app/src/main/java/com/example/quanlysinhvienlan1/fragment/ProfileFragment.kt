package com.example.quanlysinhvienlan1.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.activity.SignInActivity
import com.example.quanlysinhvienlan1.auth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {
    private var layoutUser: LinearLayout? = null
    private var viewAvatar: ImageView? = null
    private var txtUsername: TextView? = null
    private var layoutAccountSettings: LinearLayout? = null
    private var layoutChangeUsername: LinearLayout? = null
    private var layoutChangePassword: LinearLayout? = null
    private var btnLogout: Button? = null

    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var isSettingsVisible = true

    private var param1: String? = null
    private var param2: String? = null

    private val personalPageFragment = PersonalPageFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        // Ánh xạ các views
        mappingViews(view)
        // Lấy dữ liệu người dùng
        getUserData()
        // CLick sự kiện
        clickEvent()
        return view
    }

    //Ánh xạ
    private fun mappingViews(view: View) {
        layoutUser = view.findViewById(R.id.layout_User)
        txtUsername = view.findViewById(R.id.txt_Username)
        layoutAccountSettings = view.findViewById(R.id.layout_AccountSettings)
        layoutChangeUsername = view.findViewById(R.id.layout_ChangeUsername)
        layoutChangePassword = view.findViewById(R.id.layout_ChangePassword)
        btnLogout = view.findViewById(R.id.btn_Logout)
        viewAvatar = view.findViewById(R.id.view_avt)
    }
    // Lấy dữ liệu người dùng
    private fun getUserData(){
        if(auth.currentUser!=null){
            val userID = auth.currentUser!!.uid
            val userDocRef = fireStore.collection("users").document(userID)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot != null && documentSnapshot.exists()){
                        val fireStoreUsername = documentSnapshot.getString("username")
                        txtUsername?.text = fireStoreUsername
                        val fireStoreAvatar = documentSnapshot.getString("avatar")
                        fireStoreAvatar?.let {
                            Picasso.get().load(it).into(viewAvatar)
                        }
                    }else{
                        txtUsername?.text = "Loading..."
                    }
                }
                .addOnFailureListener{ exception ->
                    Log.e("FalseUsernameProfileFragment", "${exception.message}")
                }
        }

    }

    //CLick sự kiện
    private fun clickEvent() {
        // chuyển sang trang nội dung người dùng
        layoutUser?.setOnClickListener {
//            navigateToPersonalPageFragment()
            val mainActivity = activity as? MainActivity
            mainActivity?.makeCurrentFragment(personalPageFragment)
        }
        // Hiển thị/ ẩn settings
        layoutAccountSettings?.setOnClickListener {
            if (isSettingsVisible) {
                showSettings()
            } else {
                hideSettings()
            }
        }
        // Đăng xuất
        btnLogout?.setOnClickListener {
            logout()
        }
        // Đổi mật khẩu
        layoutChangePassword?.setOnClickListener {
            changePassword()
        }
        // Đổi tên người dùng
        layoutChangeUsername?.setOnClickListener{
            changeUsername()
        }
    }

    // Hiển thị settings details
    private fun showSettings() {
        layoutChangeUsername?.visibility = View.VISIBLE
        layoutChangePassword?.visibility = View.VISIBLE
        isSettingsVisible = false
    }

    // Ẩn settings details
    private fun hideSettings() {
        layoutChangeUsername?.visibility = View.GONE
        layoutChangePassword?.visibility = View.GONE
        isSettingsVisible = true
    }

    // Đăng xuất
    private fun logout() {
        if (auth.currentUser != null) {
            val builder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater
            val view: View = inflater.inflate(R.layout.custom_dialog_log_out, null)
            builder.setView(view)

            val dialog = builder.create()

            val txtLogout = view.findViewById<TextView>(R.id.txt_Logout)
            val txtCancel = view.findViewById<TextView>(R.id.txt_Cancel)

            txtLogout.setOnClickListener {
                auth.signOut()
                val intent = Intent(context, SignInActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            txtCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        } else {
            Toast.makeText(context, "Lỗi", Toast.LENGTH_SHORT).show()
        }
    }

    // Đổi tên người dùng
    private fun changeUsername(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_change_username)

        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val prbChangeUsername = dialog.findViewById<RelativeLayout>(R.id.prb_ChangeUsername)
        val edtNewFirstName = dialog.findViewById<EditText>(R.id.edt_NewFirstName)
        val edtNewLastName = dialog.findViewById<EditText>(R.id.edt_NewLastName)
        val btnConfirmChangeUsername = dialog.findViewById<Button>(R.id.btn_ConfirmChangeUsername)
        val btnCancelChangeUsername = dialog.findViewById<Button>(R.id.btn_CancelChangeUsername)

        val userID = auth.currentUser?.uid

        btnConfirmChangeUsername.setOnClickListener {
            val inputNewFirstName = edtNewFirstName.text.toString().trim()
            val inputNewLastName = edtNewLastName.text.toString().trim()
            val inputFullName = "$inputNewFirstName $inputNewLastName"
            if(inputNewFirstName.isNullOrEmpty()){
                edtNewFirstName.error = "Vui lòng nhập họ"
            }else if(inputNewLastName.isNullOrEmpty()){
                edtNewLastName.error = "Vui lòng nhập tên"
            }else if(inputNewFirstName.isNotEmpty() && inputNewLastName.isNotEmpty() && userID!=null) {
                prbChangeUsername.visibility = View.VISIBLE
                edtNewFirstName.isEnabled = false
                edtNewLastName.isEnabled = false
                btnConfirmChangeUsername.isEnabled = false
                val useRef = fireStore.collection("users").document(userID)
                val updateName = hashMapOf<String, Any>(
                    "username" to inputFullName
                )
                useRef.update(updateName)
                    .addOnSuccessListener {
                        txtUsername?.text = inputFullName
                        Toast.makeText(context,"Thay đổi tên thành công", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context,"Thay đổi tên thất bại", Toast.LENGTH_SHORT).show()
                        prbChangeUsername.visibility = View.GONE
                        edtNewFirstName.isEnabled = true
                        edtNewLastName.isEnabled = true
                        btnConfirmChangeUsername.isEnabled = true
                    }
            }
        }
        btnCancelChangeUsername.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    // Đổi mật khẩu
    private fun changePassword() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_change_password)

        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val edtOldPassword = dialog.findViewById<EditText>(R.id.edt_OldPassword)
        val edtNewPassword = dialog.findViewById<EditText>(R.id.edt_NewPassword)
        val edtConfirmPassword = dialog.findViewById<EditText>(R.id.edt_ConfirmNewPassword)
        val btnChangePassword = dialog.findViewById<Button>(R.id.btn_ConfirmChangePassword)
        val btnCancelChangePassword = dialog.findViewById<Button>(R.id.btn_CancelChangePassword)
        val prbChangePassword = dialog.findViewById<RelativeLayout>(R.id.prb_ChangePassword)

        btnChangePassword.setOnClickListener {
            val inputOldPassword = edtOldPassword.text.toString().trim()
            val inputNewPassword = edtNewPassword.text.toString().trim()
            val inputConfirmPassword = edtConfirmPassword.text.toString().trim()

            when {
                inputOldPassword.isEmpty() -> edtOldPassword.error = "Không được bỏ trống"
                inputNewPassword.isEmpty() -> edtNewPassword.error = "Không được để trống"
                inputConfirmPassword.isEmpty() -> edtConfirmPassword.error =
                    "Không được để trống"

                inputNewPassword.length < 6 -> edtNewPassword.error =
                    "Mật khẩu phải có ít nhất 6 ký tự"

                inputNewPassword != inputConfirmPassword -> edtConfirmPassword.error =
                    "Mật khẩu nhập lại không đúng"

                else -> {
                    prbChangePassword.visibility = View.VISIBLE
                    btnChangePassword.isEnabled = false
                    btnCancelChangePassword.isEnabled = false
                    edtOldPassword.isEnabled = false
                    edtNewPassword.isEnabled = false
                    edtConfirmPassword.isEnabled = false
                    val user = auth.currentUser
                    val checkOldPassword =
                        EmailAuthProvider.getCredential(user?.email!!, inputOldPassword)
                    user.reauthenticate(checkOldPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user.let {
                                it.updatePassword(inputNewPassword)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Đổi mật khẩu thành công",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            dialog.dismiss()
                                        } else {
                                            prbChangePassword.visibility = View.GONE
                                            btnChangePassword.isEnabled = true
                                            btnCancelChangePassword.isEnabled = true
                                            edtOldPassword.isEnabled = true
                                            edtNewPassword.isEnabled = true
                                            edtConfirmPassword.isEnabled = true
                                            Toast.makeText(
                                                context,
                                                "Đổi mật khẩu thất bại",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        } else {
                            prbChangePassword.visibility = View.GONE
                            btnChangePassword.isEnabled = true
                            btnCancelChangePassword.isEnabled = true
                            edtOldPassword.isEnabled = true
                            edtNewPassword.isEnabled = true
                            edtConfirmPassword.isEnabled = true
                            edtOldPassword.error = "Mật khẩu không chính xác"
                        }
                    }
                }
            }
        }

        btnCancelChangePassword.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }



    // Điều hướng sang trang cá nhân
    private fun navigateToPersonalPageFragment(){
        val fragmentPersonalPage = PersonalPageFragment()
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_Profile, fragmentPersonalPage)
        transaction.addToBackStack(null)
        transaction.commit()
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}