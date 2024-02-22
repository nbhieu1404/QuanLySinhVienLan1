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
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.adapter.ClassroomAdapter
import com.example.quanlysinhvienlan1.auth
import com.example.quanlysinhvienlan1.data.Classroom
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var btnJoinClassroom: Button
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var classroomAdapter: ClassroomAdapter
    private lateinit var imvIconApp: ImageView
    private lateinit var prbReloadDataHome: RelativeLayout

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
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        fireStore = FirebaseFirestore.getInstance()
        // Ánh xạ các view
        mapping(view)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Lấy dữ liệu lớp học
//        getClassroomList()

        // Sự kiện click
        clickEvent()
        return view
    }

    // Ánh xạ các view
    private fun mapping(view: View) {
        recyclerView = view.findViewById(R.id.rcv_AllClass)
        btnJoinClassroom = view.findViewById(R.id.btn_JoinClassroom)
        imvIconApp = view.findViewById(R.id.imv_IconApp)
        prbReloadDataHome = view.findViewById(R.id.prb_ReloadDataHome)

    }

    // Xử lý các sự kiện click
    private fun clickEvent() {
        // Tham gia lớp học
        btnJoinClassroom.setOnClickListener {
            showAddClassDialog()
        }
        // Reload dữ liệu lớp học
        imvIconApp.setOnClickListener {
            reloadDataHome()
        }
    }

    // Lấy danh sách các lớp học và cập nhật rcv bằng Adapter
    private fun getClassroomList() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fireStore.collection("classes").get()
                .addOnSuccessListener { documents ->
                    val classList = mutableListOf<Classroom>()
                    for (document in documents) {
                        val idClassroom = document.id
                        val nameClass = document.getString("nameClass") ?: ""
                        val membersQuantity = document.getLong("membersQuantity") ?: 0
                        val teacher = document.getString("teacher") ?: ""
                        val members = document.get("members") as? List<String> ?: emptyList()

                        // Kiểm tra xem người dùng có trong danh sách thành viên của lớp học không
                        if (members.contains(currentUser.uid)) {
                            // Tạo đối tượng mới với thông tin lấy từ fireStore
                            val classroom =
                                Classroom(idClassroom, nameClass, teacher, membersQuantity.toInt())
                            classList.add(classroom)
                        }
                    }
                    classroomAdapter = ClassroomAdapter(classList)
                    recyclerView.adapter = classroomAdapter
                    classroomAdapter.notifyDataSetChanged()
                    prbReloadDataHome.visibility = View.GONE
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(requireContext(), "Người dùng không tồn tại", Toast.LENGTH_SHORT).show()
        }
    }


    // Hiển thị/ xử lý dialog thêm mới lớp học
    private fun showAddClassDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_join_classroom)
        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val edtIDClassroom = dialog.findViewById<EditText>(R.id.edt_IDClassroom)
        val btnJoinClassDialog = dialog.findViewById<Button>(R.id.btn_JoinClassDialog)
        val btnCancelJoinClassroomDialog = dialog.findViewById<Button>(R.id.btn_CancelJoinClass)
        val prbJoinClassroom = dialog.findViewById<RelativeLayout>(R.id.prb_JoinClassroom)
        val txtErrorTeacher = dialog.findViewById<TextView>(R.id.txt_ErrorTeacher)

        edtIDClassroom.setOnClickListener {
            txtErrorTeacher.visibility = View.GONE
        }
        btnJoinClassDialog.setOnClickListener {
            val inputIDClassroom = edtIDClassroom.text.toString()

            if (inputIDClassroom.isEmpty()) {
                edtIDClassroom.error = "Vui lòng nhập mã lớp học"
            } else {
                prbJoinClassroom.visibility = View.VISIBLE
                edtIDClassroom.isEnabled = false
                btnJoinClassDialog.isEnabled = false
                btnCancelJoinClassroomDialog.isEnabled = true

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val classroomDocRef = fireStore.collection("classes").document(inputIDClassroom)
                    classroomDocRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val teacher = document.getString("teacher")
                            if (teacher == currentUser.uid) {
                                // Người dùng là giáo viên của lớp học
                                txtErrorTeacher.text = "Bạn đã là giáo viên của lớp này!"
                                txtErrorTeacher.visibility = View.VISIBLE
                                edtIDClassroom.isEnabled = true
                                btnJoinClassDialog.isEnabled = true
                                btnCancelJoinClassroomDialog.isEnabled = true
                                prbJoinClassroom.visibility = View.GONE
                            } else {
                                val members =
                                    document.get("members") as? List<String> ?: emptyList()
                                if (members.contains(currentUser.uid)) {
                                    // Người dùng đã tham gia lớp học
                                    txtErrorTeacher.text = "Bạn đã tham gia lớp học này!"
                                    txtErrorTeacher.visibility = View.VISIBLE
                                    edtIDClassroom.isEnabled = true
                                    btnJoinClassDialog.isEnabled = true
                                    btnCancelJoinClassroomDialog.isEnabled = true
                                    prbJoinClassroom.visibility = View.GONE
                                } else {
                                    // Thêm người dùng hiện tại vào lớp học
                                    classroomDocRef.update(
                                        "members",
                                        FieldValue.arrayUnion(currentUser.uid)
                                    )
                                        .addOnSuccessListener {
                                            getClassroomList()
                                            Toast.makeText(
                                                requireContext(),
                                                "Tham gia lớp học thành công",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            dialog.dismiss()
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(
                                                requireContext(),
                                                "Tham gia lớp học thất bại: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        } else {
                            // Lớp học không tồn tại
                            Toast.makeText(
                                requireContext(),
                                "Lớp học không tồn tại",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Người dùng không tồn tại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        }
        btnCancelJoinClassroomDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Xử lý khi tải lại dữ liệu lớp học
    private fun reloadDataHome() {
        prbReloadDataHome.visibility = View.VISIBLE
        getClassroomList()

    }

    override fun onResume() {
        super.onResume()
        getClassroomList()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
