package com.example.quanlysinhvienlan1.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.Question
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CreateNewQuestion : Fragment() {
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
        val setId = arguments?.getString("QuestionSetID")
        val idQuestionSet = setId.toString()
        if (setId != null) {
            Toast.makeText(requireContext(), idQuestionSet, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "null", Toast.LENGTH_SHORT).show()
        }
        fireStore = FirebaseFirestore.getInstance()
        mappingViews(view)
        setupRadioGroupListener()
        clickEvents(idQuestionSet)

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
        btnCreateNewQuestionBackClassroomManagement = view.findViewById(R.id.btn_CreateNewQuestionBackClassroomManagement)
    }

    private fun clickEvents(questionSetID: String) {
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

                    val mainActivity = activity as MainActivity
                    val questionID = mainActivity.generateQuestionsCode()
                    val questionName = inputInputQuestion

                    val correctAnswer = correctAnswerIndex
                    val options = mutableListOf<String>()
                    options.add(inputInputAnswer1)
                    options.add(inputInputAnswer2)
                    options.add(inputInputAnswer3)
                    options.add(inputInputAnswer4)
                    val question = Question(
                        questionID,
                        questionSetID,
                        questionName,
                        options,
                        correctAnswer
                    )
                    fireStore.collection("Questions").document(questionID).set(question)
                        .addOnSuccessListener {
                            val questionSetRef =
                                fireStore.collection("QuestionSet").document(questionSetID)
                            questionSetRef.get()
                                .addOnSuccessListener { DocumentSnapshot ->
                                    if (DocumentSnapshot.exists()) {
                                        val questionsList =
                                            DocumentSnapshot.get("questions") as? MutableList<String>
                                        if (questionsList != null) {
                                            questionsList.add(questionID)

                                            // Tạo một hashmap chứa dữ liệu cập nhật
                                            val updates =
                                                hashMapOf<String, Any>("questions" to questionsList)
                                            questionSetRef.update(updates)
                                            Toast.makeText(
                                                requireContext(),
                                                "Thêm câu hỏi thành công",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            backToProfileFragment()
                                        }
                                    }


                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Thêm câu hỏi thất bại_!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }


                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Thêm câu hỏi thất bại",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }
            }
        }

        btnCreateNewQuestionBackClassroomManagement.setOnClickListener {
            backToProfileFragment()
        }
    }

    private fun setupRadioGroupListener() {
        rdgRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            correctAnswerIndex = (group.indexOfChild(group.findViewById(checkedId)) / 2)
            Log.d("AnswerCorrect", correctAnswerIndex.toString())
        }
    }

    private fun backToProfileFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateNewQuestion().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}