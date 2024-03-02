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
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.adapter.AttendanceAdapter
import com.example.quanlysinhvienlan1.data.Score
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AttendanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AttendanceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var btnChangeDay: Button
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var txtDidQuestion: TextView
    private lateinit var rcvListAttendance: RecyclerView
    private lateinit var attendanceAdapter: AttendanceAdapter
    private val attendanceList = mutableListOf<Score>()
    var dayOfMonth: String = ""
    var month: String = ""
    var year: String = ""
    var today = "$dayOfMonth/$month/$year"
    private lateinit var btnBackToAttendance: ImageView
    private lateinit var prbReloadData: RelativeLayout

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
        val view = inflater.inflate(R.layout.fragment_attendance, container, false)
        val mainActivity = activity as MainActivity
        today = mainActivity.generateDays()
        fireStore = FirebaseFirestore.getInstance()
        mappingViews(view)


        val idClassroom = arguments?.getString("getIdClassroom")
        var cloneIdClassroom: String = ""
        if (idClassroom != null) {
            Log.d("getID", idClassroom)
            getData(idClassroom, today)
            cloneIdClassroom = idClassroom
        } else {
            Log.d("getID", null.toString())
        }

        rcvListAttendance.layoutManager = LinearLayoutManager(requireContext())
        attendanceAdapter = AttendanceAdapter(attendanceList)
        rcvListAttendance.adapter = attendanceAdapter

        clickEvents(cloneIdClassroom)
        return view
    }

    private fun mappingViews(view: View) {
        btnChangeDay = view.findViewById(R.id.btn_ChangeDay)
        txtDidQuestion = view.findViewById(R.id.txt_DidQuestion)
        rcvListAttendance = view.findViewById(R.id.rcv_Attendance)
        btnBackToAttendance = view.findViewById(R.id.btn_BackToAttendance)
        prbReloadData = view.findViewById(R.id.prb_ReloadData)
        btnChangeDay.setText("$today")
    }

    private fun clickEvents(idClassroom: String) {
        btnChangeDay.setOnClickListener {
            changeDateAttendance(idClassroom)
        }
        btnBackToAttendance.setOnClickListener {
            back()
        }
    }

    private fun changeDateAttendance(idClassroom: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_select_date)
        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dpkChangeDate = dialog.findViewById<DatePicker>(R.id.dpk_ChangeDate)
        val btnCancelSelectDate = dialog.findViewById<Button>(R.id.btn_CancelSelectDate)
        val btnUpdateSelectDate = dialog.findViewById<Button>(R.id.btn_UpdateSelectDate)

        btnUpdateSelectDate.setOnClickListener {
            dayOfMonth = dpkChangeDate.dayOfMonth.toString()
            val inputMonth = dpkChangeDate.month + 1
            month = inputMonth.toString()
            year = dpkChangeDate.year.toString()
            today = "$dayOfMonth-$month-$year"
            btnChangeDay.text = today
            getData(idClassroom, today)
            dialog.dismiss()
        }
        btnCancelSelectDate.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getData(idClassroom: String, today: String) {
        attendanceList.clear()
        val documentAttendance = "$idClassroom$today"
        var count: Int = 0
        fireStore.collection("Attendance").document(documentAttendance).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userScores =
                        document.get("getScoreToDay") as? ArrayList<HashMap<String, Any>>

                    if (userScores != null) {
                        for (scoreMap in userScores) {
                            val memberID = scoreMap["memberID"] as? String
                            val getScoreToDay = scoreMap["getScoreToDay"] as? Double

                            Log.d("documentAttendance", memberID.toString())
                            Log.d("documentAttendance", getScoreToDay.toString())

                            if (memberID != null && getScoreToDay != null) {
                                val score = Score(memberID, getScoreToDay)
                                attendanceList.add(score)
                                if (getScoreToDay > 0.0) {
                                    count++
                                }
                            }
                        }
                    }

                    fireStore.collection("classes").document(idClassroom).get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                var quantityMembers: Int = 0
                                val getQuantityMembers = it.get("members") as List<String>
                                Log.d("quantityMembers", quantityMembers.toString())
                                for (member in getQuantityMembers) {
                                    quantityMembers++
                                }
                                val finalCount = "Số lượng bài làm: ${count}/${quantityMembers}"
                                txtDidQuestion.text = finalCount
                            }
                        }
                    attendanceAdapter.notifyDataSetChanged()
                    prbReloadData.visibility = View.GONE
                }
            }.addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Không có dữ liệu điểm danh ngày $today",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun back() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AttendanceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}