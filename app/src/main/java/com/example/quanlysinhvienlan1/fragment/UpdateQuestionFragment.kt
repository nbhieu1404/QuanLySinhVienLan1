package com.example.quanlysinhvienlan1.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlysinhvienlan1.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateQuestionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateQuestionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var rdgRadioGroup: RadioGroup
    private lateinit var edtInputQuestion: EditText
    private lateinit var edtInputAnswer1: EditText
    private lateinit var edtInputAnswer2: EditText
    private lateinit var edtInputAnswer3: EditText
    private lateinit var edtInputAnswer4: EditText
    private lateinit var btnCreateNewQuestion: Button
    private lateinit var fireStore: FirebaseFirestore
    private var correctAnswerIndex = -1
    private lateinit var btnCreateNewQuestionBackClassroomManagement: ImageView
    private lateinit var rdb_A1: RadioButton
    private lateinit var rdb_A2: RadioButton
    private lateinit var rdb_A3: RadioButton
    private lateinit var rdb_A4: RadioButton
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
        val view = inflater.inflate(R.layout.fragment_create_new_question, container, false)
        fireStore = FirebaseFirestore.getInstance()
        val getQuestionID = arguments?.getString("SendUpdateQuestionID")
        val getQuestionIDTemp = getQuestionID.toString()
        if (getQuestionID != null) {
            getData(getQuestionID)
        }
        mappingViews(view)
        clickEvents(getQuestionIDTemp)
        setupRadioGroupListener()
        return view
    }

    private fun mappingViews(view: View) {
        rdgRadioGroup = view.findViewById(R.id.rdg_radioGroup)
        edtInputQuestion = view.findViewById(R.id.edt_InputQuestion)
        edtInputAnswer1 = view.findViewById(R.id.edt_inputAnswer1)
        edtInputAnswer2 = view.findViewById(R.id.edt_inputAnswer2)
        edtInputAnswer3 = view.findViewById(R.id.edt_inputAnswer3)
        edtInputAnswer4 = view.findViewById(R.id.edt_inputAnswer4)
        btnCreateNewQuestion = view.findViewById(R.id.btn_SaveQuestion)
        btnCreateNewQuestionBackClassroomManagement =
            view.findViewById(R.id.btn_CreateNewQuestionBackClassroomManagement)
        rdb_A1 = view.findViewById(R.id.rdb_A1)
        rdb_A2 = view.findViewById(R.id.rdb_A2)
        rdb_A3 = view.findViewById(R.id.rdb_A3)
        rdb_A4 = view.findViewById(R.id.rdb_A4)
    }

    private fun getData(getQuestionID: String) {
        fireStore.collection("Questions").document(getQuestionID).get()
            .addOnSuccessListener { DocumentSnapshot ->
                correctAnswerIndex = DocumentSnapshot.getLong("correctAnswerIndex")?.toInt() ?: -1
                edtInputQuestion.setText(DocumentSnapshot.getString("questionName"))
                val options = DocumentSnapshot.get("options") as List<String>
                if (options.size == 4) {
                    edtInputAnswer1.setText(options[0])
                    edtInputAnswer2.setText(options[1])
                    edtInputAnswer3.setText(options[2])
                    edtInputAnswer4.setText(options[3])
                }
                when (correctAnswerIndex) {
                    0 -> rdb_A1.isChecked = true
                    1 -> rdb_A2.isChecked = true
                    2 -> rdb_A3.isChecked = true
                    3 -> rdb_A4.isChecked = true
                }
            }
    }

    private fun clickEvents(getQuestionID: String?) {
        btnCreateNewQuestionBackClassroomManagement.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận trở về")
                .setMessage("Bạn có chắc chắn muốn trở lại mà chưa lưu câu hỏi?")
                .setPositiveButton("Có") { _, _ ->
                    backToProfileFragment()
                }
                .setNegativeButton("Không") { _, _ -> }
                .show()
        }
        btnCreateNewQuestion.setOnClickListener {
            val inputInputQuestion = edtInputQuestion.text.toString().trim()
            val inputInputAnswer1 = edtInputAnswer1.text.toString().trim()
            val inputInputAnswer2 = edtInputAnswer2.text.toString().trim()
            val inputInputAnswer3 = edtInputAnswer3.text.toString().trim()
            val inputInputAnswer4 = edtInputAnswer4.text.toString().trim()

            when {
                inputInputQuestion.isEmpty() ->
                    Toast.makeText(
                        requireContext(),
                        "Nội dung câu hỏi không được thiếu!",
                        Toast.LENGTH_SHORT
                    ).show()

                inputInputAnswer1.isEmpty() ->
                    Toast.makeText(
                        requireContext(),
                        "Câu trả lời 1 không được thiếu!",
                        Toast.LENGTH_SHORT
                    ).show()

                inputInputAnswer2.isEmpty() ->
                    Toast.makeText(
                        requireContext(),
                        "Câu trả lời 2 không được thiếu!",
                        Toast.LENGTH_SHORT
                    ).show()

                inputInputAnswer3.isEmpty() ->
                    Toast.makeText(
                        requireContext(),
                        "Câu trả lời 3 không được thiếu!",
                        Toast.LENGTH_SHORT
                    ).show()

                inputInputAnswer4.isEmpty() ->
                    Toast.makeText(
                        requireContext(),
                        "Câu trả lời 4 không được thiếu!",
                        Toast.LENGTH_SHORT
                    ).show()

                correctAnswerIndex == -1 ->
                    Toast.makeText(
                        requireContext(),
                        "Bạn chưa chọn câu trả lời!",
                        Toast.LENGTH_SHORT
                    ).show()

                else -> {
                    if (getQuestionID != null) {
                        val questionName = inputInputQuestion
                        val correctAnswer = correctAnswerIndex
                        val options = mutableListOf<String>()
                        options.add(inputInputAnswer1)
                        options.add(inputInputAnswer2)
                        options.add(inputInputAnswer3)
                        options.add(inputInputAnswer4)

                        val dataToUpdate = hashMapOf<String, Any>(
                            "questionName" to questionName,
                            "correctAnswerIndex" to correctAnswer,
                            "options" to options
                        )
                        fireStore.collection("Questions").document(getQuestionID)
                            .set(dataToUpdate, SetOptions.merge()).addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Cập nhật thành công!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                backToProfileFragment()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Cập nhật thất bại!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }

                }


            }
        }
    }


    private fun backToProfileFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun setupRadioGroupListener() {
        rdgRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            correctAnswerIndex = (group.indexOfChild(group.findViewById(checkedId)) / 2)
            Log.d("correctAnswerIndex", correctAnswerIndex.toString())
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UpdateQuestionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UpdateQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}