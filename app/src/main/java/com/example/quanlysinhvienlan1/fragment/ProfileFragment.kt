package com.example.quanlysinhvienlan1.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.activity.SignInActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {
    private var layoutUser: LinearLayout? = null
    private var layoutAccountSettings: LinearLayout? = null
    private var layoutChangeUsername: LinearLayout? = null
    private var layoutChangePassword: LinearLayout? = null
    private var btnLogout: Button? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var isSettingsVisible = true

    private var param1: String? = null
    private var param2: String? = null

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
        // Ánh xạ
        mapping(view)
        // CLick sự kiện
        clickEvent()
        return view
    }

    //Ánh xạ
    private fun mapping(view: View) {
        layoutUser = view.findViewById(R.id.layout_User)
        layoutAccountSettings = view.findViewById(R.id.layout_AccountSettings)
        layoutChangeUsername = view.findViewById(R.id.layout_ChangeUsername)
        layoutChangePassword = view.findViewById(R.id.layout_ChangePassword)
        btnLogout = view.findViewById(R.id.btn_Logout)
    }

    //CLick sự kiện
    private fun clickEvent() {
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

    // Đổi mật khẩu
    private fun changePassword() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.custom_dialog_change_password, null)
        builder.setView(view)

        val dialog = builder.create()

        val edtOldPassword = view.findViewById<EditText>(R.id.edt_OldPassword)
        val edtNewPassword = view.findViewById<EditText>(R.id.edt_NewPassword)
        val edtConfirmPassword = view.findViewById<EditText>(R.id.edt_ConfirmNewPassword)
        val btnChangePassword = view.findViewById<Button>(R.id.btn_ConfirmChangePassword)
        val btnCancelChangePassword = view.findViewById<Button>(R.id.btn_CancelChangePassword)
        val prbChangePassword = view.findViewById<RelativeLayout>(R.id.prb_ChangePassword)

        btnChangePassword.setOnClickListener {
            val inputOldPassword = edtOldPassword.text.toString().trim()
            val inputNewPassword = edtNewPassword.text.toString().trim()
            val inputConfirmPassword = edtConfirmPassword.text.toString().trim()

            when {
                inputOldPassword.isNullOrEmpty() -> edtOldPassword.error = "Không được bỏ trống"
                inputNewPassword.isNullOrEmpty() -> edtNewPassword.error = "Không được để trống"
                inputConfirmPassword.isNullOrEmpty() -> edtConfirmPassword.error =
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