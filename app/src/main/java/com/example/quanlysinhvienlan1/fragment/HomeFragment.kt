package com.example.quanlysinhvienlan1.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.OnItemLongClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.activity.DoQuestionActivity
import com.example.quanlysinhvienlan1.adapter.ClassroomAdapter
import com.example.quanlysinhvienlan1.auth
import com.example.quanlysinhvienlan1.data.Classroom
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var boxSearchHome: EditText
    private val classList = mutableListOf<Classroom>()

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
//        classList.clear()
        getClassroomList()
        // Ánh xạ các view
        mapping(view)


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        classroomAdapter = ClassroomAdapter(classList)
        recyclerView.adapter = classroomAdapter

        // Sự kiện click
        clickEvent()
//        setupSearchEvent()
        return view
    }

    // Ánh xạ các view
    private fun mapping(view: View) {
        recyclerView = view.findViewById(R.id.rcv_AllClass)
        btnJoinClassroom = view.findViewById(R.id.btn_JoinClassroom)
        imvIconApp = view.findViewById(R.id.imv_IconApp)
        prbReloadDataHome = view.findViewById(R.id.prb_ReloadDataHome)
        boxSearchHome = view.findViewById(R.id.box_searchHome)
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
        classroomAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val questionID = classList[position].idClassroom
                fireStore.collection("QuestionSet").whereEqualTo("classroomID", questionID)
                    .whereEqualTo("status", 1).get().addOnSuccessListener { query ->
                        if (!query.isEmpty) {
                            var idQuestionSet: String = ""
                            for (document in query) {
                                idQuestionSet = document.id
//                                getData(idQuestionSet)
                                Log.d("queryS", idQuestionSet.toString())
                            }
                            showDialogConfirmStartQuestion(idQuestionSet)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Bộ câu hỏi này chưa được bắt đầu!",
                                Toast.LENGTH_SHORT
                            ).show()
                            getClassroomList()

                        }
                    }
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

        })

        classroomAdapter.setOnItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongClick(position: Int) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Rời khỏi lớp")
                    .setMessage("Bạn có chắc chắn muốn rời khỏi lớp học?")
                    .setPositiveButton("Có") { _, _ ->
                        leaveClassroom(position)
                    }
                    .setNegativeButton("Không") { _, _ -> }
                    .show()
            }

        })
    }

    private fun showDialogConfirmStartQuestion(idQuestionSet: String) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Bắt đầu làm bài")
        dialog.setMessage("Bạn có chắc chắn muốn bắt đầu làm bài?")
        dialog.setPositiveButton("Tham gia") { _, _ ->
            val bundle = Bundle()
            bundle.putString("QuestionSetID", idQuestionSet)
            val intent = Intent(requireActivity(), DoQuestionActivity::class.java)
            intent.putExtras(bundle)
            requireActivity().startActivity(intent)
            val main = activity as MainActivity
            main.finish()
        }
        dialog.setNegativeButton("Hủy") { _, _ -> }
        dialog.show()

    }


    // Lấy danh sách các lớp học và cập nhật rcv bằng Adapter
    private fun getClassroomList() {
        // Xóa toàn bộ dữ liệu cũ trước khi thêm dữ liệu mới
        classList.clear()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fireStore.collection("classes").get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // Tạo đối tượng Classroom từ dữ liệu Firestore
                        val members = document.get("members") as? List<String>
                        if (members != null) {
                            for (member in members) {
                                if (member == currentUser.uid) {
                                    val idClassroom = document.id
                                    val nameClass = document.getString("nameClass") ?: ""
                                    val membersQuantity = document.getLong("membersQuantity") ?: 0
                                    val teacher = document.getString("teacher") ?: ""
                                    val classroom = Classroom(
                                        idClassroom = idClassroom,
                                        nameClass = nameClass,
                                        teacher = teacher,
                                        membersQuantity = membersQuantity.toInt(),
                                        members = members
                                    )
                                    classList.add(classroom)
                                }
                            }
                        }
                    }
                    // Cập nhật RecyclerView sau khi đã lấy được dữ liệu mới từ Firestore
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

        var quantityMembers: Int = 0
        var membersInClassroom: Int = 0

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
                    val classroomDocRef =
                        fireStore.collection("classes").document(inputIDClassroom)
                    classroomDocRef.get().addOnSuccessListener { document ->

                        val memberQuantity = document.getLong("membersQuantity")
                        // Lấy danh sách ép sang kiểu string
                        val membersString = document.get("members").toString()
                        // Tách chuỗi
                        val membersList = membersString.split(",")
                        // Số lượng học viên cho phép
                        quantityMembers = memberQuantity?.toInt() ?: 0
                        // Lấy kích cỡ
                        membersInClassroom = membersList.size

                        if (document.exists()) {
                            val teacher = document.getString("teacher")
                            if (teacher == currentUser.uid) {
                                Toast.makeText(
                                    requireContext(),
                                    membersString,
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Người dùng là giáo viên của lớp học
                                txtErrorTeacher.text = "Bạn đã là giáo viên của lớp này!"
                                txtErrorTeacher.visibility = View.VISIBLE
                                edtIDClassroom.isEnabled = true
                                btnJoinClassDialog.isEnabled = true
                                btnCancelJoinClassroomDialog.isEnabled = true
                                prbJoinClassroom.visibility = View.GONE
                            } else if (document.exists() && membersInClassroom == quantityMembers) {
                                // Thông báo đã full chỗ
                                txtErrorTeacher.text = "Lớp học đã đầy!"
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
                            edtIDClassroom.isEnabled = true
                            btnJoinClassDialog.isEnabled = true
                            btnCancelJoinClassroomDialog.isEnabled = true
                            prbJoinClassroom.visibility = View.GONE
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

    private fun leaveClassroom(position: Int) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val classroomId = classList[position].idClassroom
            val classroomDocRef = fireStore.collection("classes").document(classroomId)

            classroomDocRef.get().addOnSuccessListener { documentSnapshot ->
                val members = documentSnapshot.get("members") as? List<String> ?: emptyList()

                if (members.contains(currentUser.uid)) {
                    val updatedMembers = members.filter { it != currentUser.uid }

                    classroomDocRef.update("members", updatedMembers)
                        .addOnSuccessListener {
                            getClassroomList()
                            Toast.makeText(
                                requireContext(),
                                "Đã rời khỏi lớp học",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                requireContext(),
                                "Lỗi: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Bạn không thuộc lớp học này",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(requireContext(), "Người dùng không tồn tại", Toast.LENGTH_SHORT).show()
        }


    }

    // Xử lý sự kiện tìm kiếm
    private fun setupSearchEvent() {
        boxSearchHome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Không cần xử lý trước khi text thay đổi
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Không cần xử lý khi text thay đổi
            }

            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                // Tìm kiếm khi ngừng nhập
                if (searchText.isEmpty()) {
                    // Nếu ô tìm kiếm trống, hiển thị lại toàn bộ danh sách lớp học
                    getClassroomList()
                } else {
                    // Nếu có văn bản trong ô tìm kiếm, thực hiện tìm kiếm và cập nhật adapter
                    val filteredList = classroomAdapter.filter(searchText)
                    classroomAdapter.setData(filteredList)
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        classList.clear()
    }

    override fun onResume() {
        super.onResume()
        classList.clear()
//        getClassroomList()
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
