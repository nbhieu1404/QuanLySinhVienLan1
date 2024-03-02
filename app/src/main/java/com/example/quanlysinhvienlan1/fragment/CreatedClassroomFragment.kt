package com.example.quanlysinhvienlan1.fragment

import android.app.Dialog
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
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.adapter.ClassroomAdapter
import com.example.quanlysinhvienlan1.auth
import com.example.quanlysinhvienlan1.data.Classroom
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreatedClassroomFragment : Fragment(), OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var classroomAdapter: ClassroomAdapter
    private lateinit var btnAddNewClassroom: Button
    private lateinit var prbReloadDataCreatedClassroom: RelativeLayout

    private val classList = mutableListOf<Classroom>()
    private val classroomManagementFragment = ClassroomManagementFragment()

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
        fireStore = FirebaseFirestore.getInstance()

        // Ánh xạ các view
        mapping(view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        classroomAdapter = ClassroomAdapter(classList)
        recyclerView.adapter = classroomAdapter

        // Sự kiện click
        clickEvent()

        return view
    }

    private fun mapping(view: View) {

        recyclerView = view.findViewById(R.id.rcv_AllCreatedClass)
        btnAddNewClassroom = view.findViewById(R.id.btn_AddNewClass)
        prbReloadDataCreatedClassroom = view.findViewById(R.id.prb_ReloadDataCreatedClassroom)

    }

    // Lấy dữ liệu lớp học
    private fun getClassroomList() {
        // Xóa danh sách lớp
        classList.clear()
        val idTeacher = auth.currentUser?.uid

        // So sánh user có phải người tạo lớp không
        fireStore.collection("classes")
            .whereEqualTo(
                "teacher",
                idTeacher
            )
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val idClassroom = document.id
                    val nameClass = document.getString("nameClass") ?: ""
                    val membersQuantity = document.getLong("membersQuantity") ?: 0
                    val teacher = document.getString("teacher") ?: ""
                    val members = document.get("members") as List<String>
                    val classroom =
                        Classroom(idClassroom, nameClass, teacher, membersQuantity.toInt(), members)
                    classList.add(classroom)
                }

                // Cập nhật dữ liệu
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
        // Tạo adapter và thiết lập đối tượng lắng nghe sự kiện
        classroomAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Xử lý sự kiện khi item được click
                val idClassroom = classList[position].idClassroom
                Log.d("ClickedItemID", idClassroom)
//                Toast.makeText(requireContext(), idClassroom, Toast.LENGTH_LONG).show()

                // Tạo một Bundle để chứa idClassroom
                val bundle = Bundle()
                bundle.putString("idClassroom", idClassroom)

                // Khởi tạo Fragment mới muốn chuyển đến
                val fragment = ClassroomManagementFragment()
                fragment.arguments = bundle

                // Thực hiện thay thế Fragment hiện tại bằng Fragment mới
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.layout_Wrapper, fragment)
                transaction.addToBackStack(null)
                transaction.commit()

//                val mainActivity = activity as MainActivity
//                mainActivity.makeCurrentFragment(classroomManagementFragment)
            }

            override fun onUpdateButtonClick(position: Int) {

            }

            override fun onDeleteButtonClick(position: Int) {

            }

            override fun onStartQuestionSetClick(position: Int) {

            }

            override fun onStopQuestionSetClick(position: Int) {

            }
        })
    }

    // Hiển thị/ xử lý dialog thêm lớp học
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
                        val mainActivity = activity as MainActivity
                        val userCreateDocRef = fireStore.collection("users").document(userCreateID)
                        userCreateDocRef.get().addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val dataTeacher = userCreateID.toString()
                                val idClassDocument = mainActivity.generateRandomClassCode()
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


    override fun onResume() {
        super.onResume()
        getClassroomList()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreatedClassroomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onUpdateButtonClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onDeleteButtonClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onStartQuestionSetClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onStopQuestionSetClick(position: Int) {
        TODO("Not yet implemented")
    }


}