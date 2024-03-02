package com.example.quanlysinhvienlan1.fragment

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
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
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.adapter.QuestionSetAdapter
import com.example.quanlysinhvienlan1.auth
import com.example.quanlysinhvienlan1.data.Question
import com.example.quanlysinhvienlan1.data.QuestionSet
import com.example.quanlysinhvienlan1.data.Score
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ClassroomManagementFragment : Fragment(), DialogInterface.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var txtNameClassroomManagement: TextView
    private lateinit var txtMemberQuantityManagementActionBar: TextView
    private lateinit var btnAddNewQuizSet: Button
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var btnBackToCreatedClassroomManagement: ImageView
    private lateinit var imvOptionManagementActionBar: ImageView
    private lateinit var rcvAllQuestionSet: RecyclerView
    private lateinit var questionSetAdapter: QuestionSetAdapter
    private val questionSetList = mutableListOf<QuestionSet>()
    private lateinit var prbReloadDataClassroomManagement: RelativeLayout


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

        rcvAllQuestionSet.layoutManager = LinearLayoutManager(requireContext())
        questionSetAdapter = QuestionSetAdapter(questionSetList)
        rcvAllQuestionSet.adapter = questionSetAdapter

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
        clickEvents(idClass)
        return view
    }


    private fun mappingViews(view: View) {
        txtNameClassroomManagement = view.findViewById(R.id.txt_NameClassroomManagement)
        txtMemberQuantityManagementActionBar =
            view.findViewById(R.id.txt_MemberQuantityManagementActionBar)
        btnAddNewQuizSet = view.findViewById(R.id.btn_AddNewQuiz)
        btnBackToCreatedClassroomManagement =
            view.findViewById(R.id.btn_BackToCreatedClassroomManagement)
        imvOptionManagementActionBar = view.findViewById(R.id.imv_OptionManagementActionBar)
        rcvAllQuestionSet = view.findViewById(R.id.rcv_AllQuizSet)
        prbReloadDataClassroomManagement = view.findViewById(R.id.prb_ReloadDataClassroomManagement)
    }

    private fun getData(classroomId: String) {
        questionSetList.clear()
        val classDocRef = fireStore.collection("classes").document(classroomId)
        classDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nameClass = document.getString("nameClass")
                    val memberQuantity = document.getLong("membersQuantity").toString()
                    val dataMembersList = document.get("members") as List<String>
                    val data = dataMembersList.size
                    val quantityMembers = "$data/$memberQuantity học viên"
                    // Gán thông tin lớp học cho các TextView
                    txtNameClassroomManagement.text = nameClass
                    txtMemberQuantityManagementActionBar.text = quantityMembers


                    fireStore.collection("QuestionSet")
                        .whereEqualTo("classroomID", document.get("idClassroom"))
                        .get().addOnSuccessListener { querySnapshot ->
                            for (documents in querySnapshot) {
                                val questionSetID = documents.getString("setId") ?: ""
                                val idClassroom = classroomId
                                val nameQuestionSet = documents.getString("setName") ?: ""
                                val questions =
                                    documents.get("questions") as List<Question>
                                val status = documents.getLong("status")
                                var finalStatus: Int = 0
                                if (status != null) {
                                    finalStatus = status.toInt()
                                }
                                Log.d(
                                    "statusInt",
                                    "$questionSetID/$idClassroom/$nameQuestionSet/$status/$questions"
                                )

                                val questionSet = QuestionSet(
                                    questionSetID,
                                    idClassroom,
                                    nameQuestionSet,
                                    questions,
                                    finalStatus
                                )
                                questionSetList.add(questionSet)
                            }
                            questionSetAdapter.notifyDataSetChanged()
                            prbReloadDataClassroomManagement.visibility = View.GONE
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Xử lý khi có lỗi xảy ra
            }
    }


    private fun clickEvents(idClassroom: String) {
        btnBackToCreatedClassroomManagement.setOnClickListener {
            backToCreatedClassroomManagement()
        }
        btnAddNewQuizSet.setOnClickListener {
            showDialogCreateNewQuizSet(idClassroom)
        }

        questionSetAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val idQuestionSet = questionSetList[position].setId
                Log.d("idQuestionSet", idQuestionSet)
                val bundle = Bundle()
                bundle.putString("idQuestionSet", idQuestionSet)
                val fragment = QuestionManagementFragment()
                fragment.arguments = bundle

                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.layout_Wrapper, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            override fun onUpdateButtonClick(position: Int) {

            }

            override fun onDeleteButtonClick(position: Int) {

            }

            override fun onStartQuestionSetClick(position: Int) {
                val selectedQuestionSet = questionSetList[position] // Lấy ra bộ câu hỏi được chọn

                val questionSetRef = fireStore.collection("QuestionSet")

                var check: Int = 0
                // Kiểm tra nếu có bộ câu hỏi nào có trạng thái là 1
                questionSetRef.whereEqualTo("classroomID", selectedQuestionSet.classroomID).get()
                    .addOnSuccessListener { querySnapshot ->
                        for (documents in querySnapshot) {
                            val getStatus = documents.getLong("status")
                            if (getStatus != null) {
                                check = getStatus.toInt()
                                Log.d("selectedQuestionSet", "${check}")
                            }
                            if (check == 1) {
                                break
                            }
                        }
                        if (check == 1) {
                            Toast.makeText(
                                requireContext(),
                                "Có bài tập đang được diễn ra",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (check == 0) {
                            var temp: Int = 0
                            questionSetRef.document(selectedQuestionSet.setId).get()
                                .addOnSuccessListener {
                                    val quantityQuestion = it.get("questions") as List<String>
                                    temp = quantityQuestion.size
                                    if (temp == 0) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Không có bài tập nào",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (temp >= 1) {
                                        val dialog = Dialog(requireContext())
                                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                        dialog.setContentView(R.layout.custom_dialog_set_time_countdown)
                                        val window = dialog.window ?: return@addOnSuccessListener
                                        window.setLayout(
                                            WindowManager.LayoutParams.MATCH_PARENT,
                                            WindowManager.LayoutParams.WRAP_CONTENT
                                        )
                                        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                        val edtSetCountdownTime =
                                            dialog.findViewById<TextView>(R.id.edt_SetCountdownTime)
                                        val btnConfirmSetTimeCountdown =
                                            dialog.findViewById<Button>(R.id.btn_ConfirmSetTimeCountdown)
                                        val btnCancelSetCountdownTime =
                                            dialog.findViewById<Button>(R.id.btn_CancelSetCountdownTime)
                                        val txtErrorCountdownTime =
                                            dialog.findViewById<TextView>(R.id.txt_ErrorCountdownTime)
                                        edtSetCountdownTime.setOnClickListener {
                                            txtErrorCountdownTime.visibility = View.GONE
                                        }
                                        btnConfirmSetTimeCountdown.setOnClickListener {
                                            val inputCountDownTime =
                                                edtSetCountdownTime.text.toString().trim()
                                            when {
                                                inputCountDownTime.toInt() < 5 -> {
                                                    txtErrorCountdownTime.text =
                                                        "Thời gian làm bài phải nhiều hơn 5 phút!"
                                                    txtErrorCountdownTime.visibility = View.VISIBLE
                                                }

                                                inputCountDownTime.toInt() > 60 -> {
                                                    txtErrorCountdownTime.text =
                                                        "Thời gian làm bài tối da là 60 phút!"
                                                    txtErrorCountdownTime.visibility = View.VISIBLE
                                                }

                                                else -> {
                                                    val update = hashMapOf<String, Any>(
                                                        "status" to 1,
                                                        "countDownTime" to inputCountDownTime.toInt()
                                                    )
                                                    // Nếu không, cập nhật trạng thái của bộ câu hỏi được chọn sang 1
                                                    questionSetRef.document(selectedQuestionSet.setId)
                                                        .update(update)
                                                        .addOnSuccessListener {
                                                            registerAttendance(idClassroom)
                                                            dialog.dismiss()
                                                            getData(idClassroom)
                                                        }
                                                        .addOnFailureListener { exception ->
                                                            // Xảy ra lỗi khi cập nhật
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "Lỗi: ${exception.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }
                                            }
                                        }

                                        btnCancelSetCountdownTime.setOnClickListener {
                                            dialog.dismiss()
                                        }
                                        dialog.show()

                                    }
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Xảy ra lỗi khi truy vấn
                        Toast.makeText(
                            requireContext(),
                            "Lỗi: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            override fun onStopQuestionSetClick(position: Int) {
                Log.d("StartStop", "onStopQuestionSetClick")
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xác nhận kết thúc")
                    .setMessage("Bạn có chắc chắn muốn muốn kết thúc tắt làm bài?")
                    .setPositiveButton("Kết thúc") { _, _ ->
                        val selectedQuestionSet = questionSetList[position]
                        val update = hashMapOf<String, Any>(
                            "status" to 0,
                            "countDownTime" to 0
                        )
                        fireStore.collection("QuestionSet").document(selectedQuestionSet.setId)
                            .update(update)
                            .addOnSuccessListener {
                                fireStore.collection("Attendance").document(idClassroom).get()
                                    .addOnSuccessListener {
                                        val getUserScore = it.get("getScoreToDay") as? List<Score>
                                        if (getUserScore != null) {
                                            Log.d("getUserScore", getUserScore.toString())
                                        } else {
                                            Log.d("getUserScore", null.toString())
                                        }

                                    }
//                                // Cập nhật thành công
                                Toast.makeText(
                                    requireContext(),
                                    "Đã kết thúc bài tập!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                getData(idClassroom)
                            }
                    }
                    .setNegativeButton("Hủy") { _, _ -> }
                    .show()
            }

        })
    }

    private fun backToCreatedClassroomManagement() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun popupMenuClick(popupMenu: PopupMenu, idClassroom: String) {
        // Thiết lập sự kiện click cho các item trong menu
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.shareCodeClassroom -> {
                    showShareCodeClassroom(idClassroom)
                    true
                }
                // Bấm cập nhật lớp học
                R.id.updateInformationClassroom -> {
                    showDialogUpdateClassroom(idClassroom)
                    true
                }

                R.id.Attendance -> {
                    val bundle = Bundle()
                    bundle.putString("getIdClassroom", idClassroom)
                    val fragment = AttendanceFragment()
                    fragment.arguments = bundle
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.layout_Wrapper, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    true
                }

                R.id.listMembersJoinedClassroom -> {
                    // Xử lý khi chọn "Danh sách thành viên"

                    // Tạo một Bundle để chứa idClassroom
                    val bundle = Bundle()
                    bundle.putString("idClassroom", idClassroom)

                    // Khởi tạo Fragment mới muốn chuyển đến
                    val fragment = ListMembersFragment()
                    fragment.arguments = bundle
                    // Thực hiện thay thế Fragment hiện tại bằng Fragment mới
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.layout_Wrapper, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
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


        var membersInClassroom: Int = 0

        val userCreateDocRef = fireStore.collection("classes").document(idClassroom)
        userCreateDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val nameClass = document.getString("nameClass")
                val memberQuantity = document.getLong("membersQuantity")


                // Lấy danh sách ép sang kiểu string
                val membersString = document.get("members").toString()
                // Tách chuỗi
                val membersList = membersString.split(",")

                edtUpdateClassName.setText(nameClass)
                edtUpdateMembersQuantity.setText(memberQuantity?.toString() ?: "")

                // Lấy kích cỡ
                membersInClassroom = membersList.size

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

                inputUpdateMembersQuantity < membersInClassroom -> {
                    edtUpdateMembersQuantity.error =
                        "Số lượng nhỏ hơn số lượng sinh viên đang có trong lớp"
                }

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

    private fun showShareCodeClassroom(idClassroom: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_share_code)
        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtCodeClassroom = dialog.findViewById<TextView>(R.id.txt_CodeClassroom)
        val btnShareCodeClassroom = dialog.findViewById<Button>(R.id.btn_CopyCode)
        val btnOk = dialog.findViewById<Button>(R.id.btn_Ok)

        txtCodeClassroom.text = idClassroom

        btnShareCodeClassroom.setOnClickListener {
            val clipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("CodeClassroom", idClassroom)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(
                requireContext(),
                "Mã lớp học đã được sao chép vào clipboard!",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        btnOk.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDialogCreateNewQuizSet(idClassroom: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_create_new_question_set)
        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val edtNewNameQuestionSet = dialog.findViewById<EditText>(R.id.edt_NewNameQuestionSet)
        val btnCreateNewNameQuestionSet =
            dialog.findViewById<Button>(R.id.btn_CreateNewNameQuestionSet)
        val btnCancelCreateNewNameQuestionSet =
            dialog.findViewById<Button>(R.id.btn_CancelCreateNewNameQuestionSet)
        val txtErrorNewNameQuestionSet =
            dialog.findViewById<TextView>(R.id.txt_ErrorNewNameQuestionSet)

        edtNewNameQuestionSet.setOnClickListener {
            txtErrorNewNameQuestionSet.visibility = View.GONE
        }

        btnCreateNewNameQuestionSet.setOnClickListener {
            val inputNewNameQuestionSet = edtNewNameQuestionSet.text.toString().trim()
            when {
                inputNewNameQuestionSet.isEmpty() -> {
                    txtErrorNewNameQuestionSet.text = "Vui lòng nhập tên bộ câu hỏi!"
                    txtErrorNewNameQuestionSet.visibility = View.VISIBLE
                }

                else -> {
                    val classroomId = idClassroom
                    val mainActivity = activity as MainActivity
                    val setID = mainActivity.generateQuestionSetCode()
                    val setNameQuestionSet = inputNewNameQuestionSet
                    val questionList = emptyList<Question>()

//                    Toast.makeText(requireContext(), setID, Toast.LENGTH_SHORT).show()
                    val newQuestionSet =
                        QuestionSet(setID, classroomId, setNameQuestionSet, questionList, 0)

                    fireStore.collection("QuestionSet").document(setID).set(newQuestionSet)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Tạo bộ câu hỏi thành công",
                                Toast.LENGTH_SHORT

                            ).show()
                            getData(classroomId)
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Tạo bộ câu hỏi thất bại",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }

        btnCancelCreateNewNameQuestionSet.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun registerAttendance(idClassroom: String) {
        val mainActivity = activity as MainActivity
        val today = mainActivity.generateDays()
        fireStore.collection("Attendance")
            .whereEqualTo("idClassroom", idClassroom)
            .whereEqualTo("date", today).get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    val userScore = mutableListOf<Score>()

                    fireStore.collection("classes")
                        .document(idClassroom)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val members =
                                    documentSnapshot.get(
                                        "members"
                                    ) as? List<String>
                                if (members != null) {
                                    for (member in members) {
                                        val getMember = Score(
                                            memberID = member,
                                            getScoreToDay = 0.0
                                        )
                                        userScore.add(getMember)
                                    }
                                    val update =
                                        hashMapOf<String, Any>(
                                            "classroomId" to idClassroom,
                                            "date" to today,
                                            "getScoreToDay" to userScore
                                        )
                                    val dcm = "$idClassroom${mainActivity.generateDays()}"
                                    fireStore.collection("Attendance")
                                        .document(dcm)
                                        .set(update)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Bộ câu hỏi đã được triển khai!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e(
                                                "registerAttendance",
                                                "Error: ${exception.message}"
                                            )
                                            Toast.makeText(
                                                requireContext(),
                                                "Đã xảy ra lỗi khi triển khai!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Không có thành viên nào!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Bộ câu hỏi đã được triển khai!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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

    override fun onClick(dialog: DialogInterface?, which: Int) {
        TODO("Not yet implemented")
    }
}