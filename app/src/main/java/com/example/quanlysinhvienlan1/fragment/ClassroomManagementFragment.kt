package com.example.quanlysinhvienlan1.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.auth
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ClassroomManagementFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var txtNameClassroomManagement: TextView
    private lateinit var txtMemberQuantityManagementActionBar: TextView
    private lateinit var btnAddNewQuiz: Button
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var btnBackToCreatedClassroomManagement: ImageView
    private lateinit var imvOptionManagementActionBar: ImageView


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
        val view = inflater.inflate(R.layout.fragment_classroom_management, container, false)

        mappingViews(view)
        // lấy data được gửi từ createdClassroomFragment
        val idClassroom = arguments?.getString("idClassroom")
        val idClass = idClassroom.toString()
        if (idClassroom != null) {
            getData(idClassroom)
        }

        // Tạo một PopupMenu
        val popupMenu = PopupMenu(requireContext(), imvOptionManagementActionBar)

        // Gắn menu_classroom_options.xml vào PopupMenu
        popupMenu.menuInflater.inflate(R.menu.menu_created_classroom, popupMenu.menu)

        // Sự kiện khi bấm vào imvOptionManagementActionBar
        imvOptionManagementActionBar.setOnClickListener {
            // Hiển thị PopupMenu
            popupMenu.show()
        }

        popupMenuClick(popupMenu, idClass)
        clickEvents()
        return view
    }


    private fun mappingViews(view: View) {
        txtNameClassroomManagement = view.findViewById(R.id.txt_NameClassroomManagement)
        txtMemberQuantityManagementActionBar =
            view.findViewById(R.id.txt_MemberQuantityManagementActionBar)
        btnAddNewQuiz = view.findViewById(R.id.btn_AddNewQuiz)
        btnBackToCreatedClassroomManagement =
            view.findViewById(R.id.btn_BackToCreatedClassroomManagement)
        imvOptionManagementActionBar = view.findViewById(R.id.imv_OptionManagementActionBar)
    }

    private fun getData(classroomId: String) {
        val classDocRef = fireStore.collection("classes").document(classroomId)
        classDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nameClass = document.getString("nameClass")
                    val memberQuantity = document.getLong("membersQuantity")

                    // Gán thông tin lớp học cho các TextView
                    txtNameClassroomManagement.text = nameClass
                    txtMemberQuantityManagementActionBar.text = memberQuantity?.toString() ?: ""
                }
            }
            .addOnFailureListener { exception ->
                // Xử lý khi có lỗi xảy ra
            }
    }


    private fun clickEvents() {
        btnBackToCreatedClassroomManagement.setOnClickListener {
            backToCreatedClassroomManagement()
        }
    }

    private fun backToCreatedClassroomManagement() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun popupMenuClick(popupMenu: PopupMenu, idClassroom: String) {
        // Thiết lập sự kiện click cho các item trong menu
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                // Bấm cập nhật lớp học
                R.id.updateInformationClassroom -> {
                    showDialogUpdateClassroom(idClassroom)
                    true
                }

                R.id.listMembersJoinedClassroom -> {
                    // Xử lý khi chọn "Danh sách thành viên"
                    true
                }

                R.id.deleteClassroom -> {
                    showDialogDeleteClassroom(idClassroom)
                    true
                }

                else -> false
            }
        }
    }

    // Hiển thị/ xử lý cập nhập lớp học
    private fun showDialogUpdateClassroom(idClassroom: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_update_created_classroom)
        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val edtUpdateClassName = dialog.findViewById<EditText>(R.id.edt_UpdateClassName)
        val edtUpdateMembersQuantity = dialog.findViewById<EditText>(R.id.edt_UpdateMembersQuantity)
        val btnUpdateClassDialog = dialog.findViewById<Button>(R.id.btn_UpdateClassDialog)
        val btnCancelUpdateAddClass = dialog.findViewById<Button>(R.id.btn_CancelUpdateAddClass)
        val prbUpdateClass = dialog.findViewById<RelativeLayout>(R.id.prb_UpdateClass)

        val userCreateDocRef = fireStore.collection("classes").document(idClassroom)
        userCreateDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val nameClass = document.getString("nameClass")
                val memberQuantity = document.getLong("membersQuantity")

                edtUpdateClassName.setText(nameClass)
                edtUpdateMembersQuantity.setText(memberQuantity?.toString() ?: "")
            }
        }

        btnUpdateClassDialog.setOnClickListener {
            val inputUpdateClassName = edtUpdateClassName.text.toString()
            val inputUpdateMembersQuantity = edtUpdateMembersQuantity.text.toString().toIntOrNull()

            when {
                inputUpdateClassName.isEmpty() -> edtUpdateClassName.error =
                    "Vui lòng nhập tên lớp học"

                inputUpdateMembersQuantity == null -> edtUpdateMembersQuantity.error =
                    "Vui lòng nhập số lượng sinh viên"

                inputUpdateMembersQuantity < 1 -> edtUpdateMembersQuantity.error =
                    "Số lượng sinh viên phải lớn hơn 0"

                else -> {
                    prbUpdateClass.visibility = View.VISIBLE
                    edtUpdateClassName.isEnabled = false
                    edtUpdateMembersQuantity.isEnabled = false
                    btnUpdateClassDialog.isEnabled = false
                    btnCancelUpdateAddClass.isEnabled = false

                    val classUpdateDocRef = fireStore.collection("classes").document(idClassroom)
                    classUpdateDocRef.update(
                        mapOf<String, Any?>(
                            "nameClass" to inputUpdateClassName,
                            "membersQuantity" to inputUpdateMembersQuantity
                        ).filterValues { it != null }
                    ).addOnSuccessListener {
                        // Update UI here if needed
                        txtNameClassroomManagement.text = inputUpdateClassName
                        txtMemberQuantityManagementActionBar.text =
                            inputUpdateMembersQuantity.toString()
                        Toast.makeText(
                            requireContext(),
                            "Cập nhật thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }.addOnFailureListener {
                        // Handle failure
                        Toast.makeText(
                            requireContext(),
                            "Cập nhật thất bại",
                            Toast.LENGTH_SHORT
                        ).show()
                        prbUpdateClass.visibility = View.GONE
                        edtUpdateClassName.isEnabled = true
                        edtUpdateMembersQuantity.isEnabled = true
                        btnUpdateClassDialog.isEnabled = true
                        btnCancelUpdateAddClass.isEnabled = true
                    }
                }
            }
        }

        btnCancelUpdateAddClass.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    // Hiển thị/ xử lý xóa lớp học
    private fun showDialogDeleteClassroom(idClassroom: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_delete_classroom)
        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val edtConfirmDeleteClassroom =
            dialog.findViewById<EditText>(R.id.edt_ConfirmDeleteClassroom)
        val btnConfirmDeleteClassroom = dialog.findViewById<Button>(R.id.btn_ConfirmDeleteClassroom)
        val btnCancelDeleteClassroom = dialog.findViewById<Button>(R.id.btn_CancelDeleteClassroom)
        val prbDeleteClassroom = dialog.findViewById<RelativeLayout>(R.id.prb_DeleteClassroom)


        btnConfirmDeleteClassroom.setOnClickListener {
            val inputConfirmDeleteClassroom = edtConfirmDeleteClassroom.text.toString()

            when {
                inputConfirmDeleteClassroom.isEmpty() -> edtConfirmDeleteClassroom.error =
                    "Vui lòng nhập tên mật khẩu"

                else -> {
                    prbDeleteClassroom.visibility = View.VISIBLE
                    edtConfirmDeleteClassroom.isEnabled = false
                    btnConfirmDeleteClassroom.isEnabled = false
                    btnCancelDeleteClassroom.isEnabled = false

                    val user = auth.currentUser
                    val checkPassword =
                        EmailAuthProvider.getCredential(user?.email!!, inputConfirmDeleteClassroom)
                    user.reauthenticate(checkPassword).addOnCompleteListener {
                        if (it.isSuccessful) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Xóa lớp học")
                                .setMessage("Bạn có chắc chắn muốn xóa lớp học?")
                                .setPositiveButton("Có") { _, _ ->
                                    val classDeleteDocRef =
                                        fireStore.collection("classes").document(idClassroom)
                                    classDeleteDocRef.delete().addOnSuccessListener {
                                        // Update UI here if needed
                                        Toast.makeText(
                                            requireContext(),
                                            "Xóa thành công",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        dialog.dismiss()
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }.addOnFailureListener {
                                        // Handle failure
                                        Toast.makeText(
                                            requireContext(),
                                            "Xóa thất bại",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .setNegativeButton("Không") { _, _ -> dialog.dismiss() }
                                .show()


                        } else {
                            prbDeleteClassroom.visibility = View.GONE
                            edtConfirmDeleteClassroom.isEnabled = true
                            btnConfirmDeleteClassroom.isEnabled = true
                            btnCancelDeleteClassroom.isEnabled = true
                            edtConfirmDeleteClassroom.error = "Mật khẩu không chính xác"
                        }
                    }


                }
            }
        }

        btnCancelDeleteClassroom.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ClassroomManagementFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}