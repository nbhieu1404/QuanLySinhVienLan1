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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.adapter.ClassroomAdapter
import com.example.quanlysinhvienlan1.data.Classroom
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

        getClassroomList()

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
        btnJoinClassroom.setOnClickListener {
            showAddClassDialog()
        }
        imvIconApp.setOnClickListener {
            reloadDataHome()
        }
    }

    // Lấy danh sách các lớp học và cập nhật rcv bằng Adapter
    private fun getClassroomList() {
        fireStore.collection("classes").get()
            .addOnSuccessListener { documents ->
                val classList = mutableListOf<Classroom>()
                for (document in documents) {
                    val idClassroom = document.id
                    val nameClass = document.getString("nameClass") ?: ""
                    val membersQuantity = document.getLong("membersQuantity") ?: 0
                    val teacher = document.getString("teacher") ?: ""
                    // Tạo đối tượng mới với thông tin lấy từ fireStore
                    val classroom =
                        Classroom(idClassroom, nameClass, teacher, membersQuantity.toInt())
                    classList.add(classroom)
                }
                classroomAdapter = ClassroomAdapter(classList)
                recyclerView.adapter = classroomAdapter
                classroomAdapter.notifyDataSetChanged()
                prbReloadDataHome.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    // Hiển thị dialog thêm mới lớp học
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

        btnJoinClassDialog.setOnClickListener {
            val inputIDClassroom = edtIDClassroom.text.toString()
            Toast.makeText(requireContext(), inputIDClassroom, Toast.LENGTH_SHORT).show()
        }
        btnCancelJoinClassroomDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun reloadDataHome() {
        prbReloadDataHome.visibility = View.VISIBLE
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
