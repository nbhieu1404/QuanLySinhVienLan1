package com.example.quanlysinhvienlan1.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.adapter.ClassroomAdapter
import com.example.quanlysinhvienlan1.auth
import com.example.quanlysinhvienlan1.data.Classroom
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreatedClassroomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreatedClassroomFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var classroomAdapter: ClassroomAdapter
    private lateinit var btnAddNewClassroom: Button
    private lateinit var prbReloadDataCreatedClassroom: RelativeLayout
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
        val view = inflater.inflate(R.layout.fragment_created_classroom, container, false)
        // Ánh xạ các view
        mapping(view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Sự kiện click
        clickEvent()

        getClassroomList()
        return view
    }
    private fun mapping(view : View){
        fireStore = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.rcv_AllCreatedClass)
        btnAddNewClassroom = view.findViewById(R.id.btn_AddNewClass)
        prbReloadDataCreatedClassroom = view.findViewById(R.id.prb_ReloadDataCreatedClassroom)

    }
    private fun getClassroomList() {
        fireStore.collection("classes")
            .whereEqualTo("teacher", auth.currentUser?.uid) // Chỉ lấy các lớp học do người dùng hiện tại tạo
            .get()
            .addOnSuccessListener { documents ->
                val classList = mutableListOf<Classroom>()
                for (document in documents) {
                    val idClassroom = document.id
                    val nameClass = document.getString("nameClass") ?: ""
                    val membersQuantity = document.getLong("membersQuantity") ?: 0
                    val teacher = document.getString("teacher") ?: ""
                    val classroom = Classroom(idClassroom, nameClass, teacher, membersQuantity.toInt())
                    classList.add(classroom)
                }
                classroomAdapter = ClassroomAdapter(classList)
                recyclerView.adapter = classroomAdapter
                classroomAdapter.notifyDataSetChanged()
                prbReloadDataCreatedClassroom.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    // Xử lý các sự kiện click
    private fun clickEvent() {
        btnAddNewClassroom.setOnClickListener {
            showAddClassDialog()
        }
    }
    private fun showAddClassDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_add_class)
        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val edtClassName = dialog.findViewById<EditText>(R.id.edt_ClassName)
        val edtMembersQuantity = dialog.findViewById<EditText>(R.id.edt_MembersQuantity)
        val btnAddClassDialog = dialog.findViewById<Button>(R.id.btn_AddClassDialog)
        val btnCancelAddClass = dialog.findViewById<Button>(R.id.btn_CancelAddClass)
        val prbAddClass = dialog.findViewById<RelativeLayout>(R.id.prb_AddClass)

        btnAddClassDialog.setOnClickListener {
            val inputClassName = edtClassName.text.toString()
            val inputMembersQuantity = edtMembersQuantity.text.toString().toIntOrNull()

            when {
                inputClassName.isEmpty() -> edtClassName.error = "Vui lòng nhập tên lớp"
                inputMembersQuantity == null -> edtMembersQuantity.error =
                    "Vui lòng nhập số lượng sinh viên"

                inputMembersQuantity < 1 -> edtMembersQuantity.error =
                    "Số lượng sinh viên phải lớn hơn 0"

                inputMembersQuantity > 100 -> edtMembersQuantity.error =
                    "Số lượng sinh viên tối đa = 100"

                else -> {
                    prbAddClass.visibility = View.VISIBLE
                    edtClassName.isEnabled = false
                    edtMembersQuantity.isEnabled = false
                    btnAddClassDialog.isEnabled = false
                    btnCancelAddClass.isEnabled = true

                    val userCreate = auth.currentUser
                    userCreate?.let {
                        val userCreateID = userCreate.uid
                        val userCreateDocRef = fireStore.collection("users").document(userCreateID)
                        userCreateDocRef.get().addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val dataTeacher = userCreateID.toString()
                                val idClassDocument = generateRandomClassCode()
                                val classroom = Classroom(
                                    idClassroom = idClassDocument,
                                    nameClass = inputClassName,
                                    teacher = dataTeacher,
                                    membersQuantity = inputMembersQuantity
                                )
                                fireStore.collection("classes")
                                    .document(idClassDocument)
                                    .set(classroom)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Thêm lớp thành công",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        dialog.dismiss()
                                        getClassroomList()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Thêm lớp thất bại",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        prbAddClass.visibility = View.GONE
                                        edtClassName.isEnabled = true
                                        edtMembersQuantity.isEnabled = true
                                        btnAddClassDialog.isEnabled = true
                                        btnCancelAddClass.isEnabled = true
                                    }

                            }
                        }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Fail",
                                    Toast.LENGTH_SHORT
                                ).show()
                                prbAddClass.visibility = View.GONE
                                edtClassName.isEnabled = true
                                edtMembersQuantity.isEnabled = true
                                btnAddClassDialog.isEnabled = true
                                btnCancelAddClass.isEnabled = true
                            }

                    }
                }
            }
        }
        btnCancelAddClass.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    // tạo random mã cho id lớp
    private fun generateRandomClassCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreatedClassroomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}