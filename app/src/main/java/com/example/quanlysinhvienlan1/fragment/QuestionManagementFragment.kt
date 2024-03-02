package com.example.quanlysinhvienlan1.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.adapter.QuestionAdapter
import com.example.quanlysinhvienlan1.data.Question
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [QuestionManagementFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuestionManagementFragment : Fragment(), DialogInterface.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var btnCreateNewQuestion: Button
    private lateinit var rcvAllQuestions: RecyclerView
    private lateinit var btnQuestionManagementBackClassroomManagement: ImageView
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var questionAdapter: QuestionAdapter
    private val questionList = mutableListOf<Question>()
    private lateinit var prbReloadQuestionManagementB: RelativeLayout
    private lateinit var txtQuestionQuantity: TextView
    private lateinit var boxSearchQuestion: EditText
    private lateinit var btnSearchQuestion: Button


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
        val view = inflater.inflate(R.layout.fragment_question_management, container, false)
        fireStore = FirebaseFirestore.getInstance()

        val setId = arguments?.getString("idQuestionSet")
        val idQuestionSet = setId.toString()
        if (setId != null) {
            getData(idQuestionSet)
        }

        mappingViews(view)

        rcvAllQuestions.layoutManager = LinearLayoutManager(requireContext())
        questionAdapter = QuestionAdapter(questionList)
        rcvAllQuestions.adapter = questionAdapter
        clickEvents(idQuestionSet)
        return view
    }

    private fun mappingViews(view: View) {
        btnCreateNewQuestion = view.findViewById(R.id.btn_CreateNewQuestion)
        rcvAllQuestions = view.findViewById(R.id.rcv_AllQuestions)
        btnQuestionManagementBackClassroomManagement =
            view.findViewById(R.id.btn_QuestionManagementBackClassroomManagement)
        prbReloadQuestionManagementB = view.findViewById(R.id.prb_ReloadQuestionManagementB)
        txtQuestionQuantity = view.findViewById(R.id.txt_QuestionQuantity)
        boxSearchQuestion = view.findViewById(R.id.box_SearchQuestion)
        btnSearchQuestion = view.findViewById(R.id.btn_SearchQuestion)
    }

    private fun getData(idQuestionSet: String) {
        questionList.clear()
        fireStore.collection("Questions").whereEqualTo("questionSetID", idQuestionSet).get()
            .addOnSuccessListener { query ->
                for (document in query) {
                    val questionID = document.getString("questionID").toString()
                    val questionSetID = idQuestionSet
                    val questionName = document.getString("questionName").toString()
                    val options = document.get("options") as List<String>
                    val correctAnswerIndex: Int =
                        document.get("correctAnswerIndex").toString().toIntOrNull() ?: 0
                    val question = Question(
                        questionID,
                        questionSetID,
                        questionName,
                        options,
                        correctAnswerIndex
                    )
                    questionList.add(question)
                    Log.d("dataQuestionD", question.toString())
                }
                val stringQuestionQuantity = questionList.size.toString() + " câu"
                txtQuestionQuantity.text = stringQuestionQuantity
                questionAdapter.notifyDataSetChanged()
                prbReloadQuestionManagementB.visibility = View.GONE
            }
            .addOnFailureListener {
                Log.e("dataQuestion", it.toString())
            }
    }

    private fun clickEvents(idQuestionSet: String) {
        btnCreateNewQuestion.setOnClickListener {
            fireStore.collection("QuestionSet").document(idQuestionSet).get()
                .addOnSuccessListener {
                    val status = it.getLong("status")?.toInt()
                    if (status == 1) {
                        Toast.makeText(
                            requireContext(),
                            "Bộ câu hỏi này đang được diễn ra!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val bundle = Bundle()
                        bundle.putString("QuestionSetID", idQuestionSet)
                        val fragment = CreateNewQuestion()
                        fragment.arguments = bundle

                        val transaction =
                            requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.layout_Wrapper, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }

                }
        }
        btnQuestionManagementBackClassroomManagement.setOnClickListener {
            backToProfileFragment()
        }
        questionAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {

            }

            override fun onUpdateButtonClick(position: Int) {
                val questionId = questionList[position].questionID
                val questionSetId = questionList[position].questionSetID
                fireStore.collection("QuestionSet").document(questionSetId).get()
                    .addOnSuccessListener {
                        val status = it.getLong("status")?.toInt()
                        if (status == 1) {
                            Toast.makeText(
                                requireContext(),
                                "Bộ câu hỏi này đang được diễn ra!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val fragment = UpdateQuestionFragment()
                            val bundle = Bundle()
                            bundle.putString(
                                "SendUpdateQuestionID",
                                questionId
                            )
                            fragment.arguments = bundle
                            val transaction =
                                requireActivity().supportFragmentManager.beginTransaction()
                            transaction.replace(R.id.layout_Wrapper, fragment)
                            transaction.addToBackStack(null)
                            transaction.commit()
                        }

                    }
            }

            override fun onDeleteButtonClick(position: Int) {
                val questionId = questionList[position].questionID
                val questionSetId = questionList[position].questionSetID
                fireStore.collection("QuestionSet").document(questionSetId).get()
                    .addOnSuccessListener {
                        val status = it.getLong("status")?.toInt()
                        if (status == 1) {
                            Toast.makeText(
                                requireContext(),
                                "Bộ câu hỏi này đang được diễn ra!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Xóa câu hỏi")
                                .setMessage("Bạn có chắc chắn muốn xóa câu hỏi?")
                                .setPositiveButton("Có") { _, _ ->
                                    fireStore.collection("Questions").document(questionId)
                                        .delete()
                                        .addOnSuccessListener {
                                            fireStore.collection("QuestionSet")
                                                .document(questionSetId).get()
                                                .addOnSuccessListener { documentSnapshot ->
                                                    if (documentSnapshot.exists()) {
                                                        val questions =
                                                            documentSnapshot.get("questions") as? List<String>
                                                                ?: emptyList()
                                                        // Loại bỏ giá trị questionId khỏi mảng questions
                                                        val updatedQuestions =
                                                            questions.filter { it != questionId }
                                                        // Cập nhật lại mảng questions vào tài liệu
                                                        fireStore.collection("QuestionSet")
                                                            .document(questionSetId)
                                                            .update(
                                                                "questions",
                                                                updatedQuestions
                                                            )
                                                            .addOnSuccessListener {
                                                                prbReloadQuestionManagementB.visibility =
                                                                    View.VISIBLE
                                                                getData(idQuestionSet)
                                                                Toast.makeText(
                                                                    requireContext(),
                                                                    "Xóa câu hỏi thành công",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                    }
                                                }
                                        }
                                }
                                .setNegativeButton("Không") { _, _ -> }
                                .show()
                        }
                    }

            }

            override fun onStartQuestionSetClick(position: Int) {

            }

            override fun onStopQuestionSetClick(position: Int) {

            }

        })
        btnSearchQuestion.setOnClickListener {
            val keyword = boxSearchQuestion.text.toString()
            searchQuestions(keyword)
        }
    }

    private fun searchQuestions(keyword: String) {
        val filteredList = questionList.filter { it.questionName.contains(keyword, true) }
        questionAdapter.setData(filteredList)
        val stringQuestionQuantity = filteredList.size.toString() + " câu"
        txtQuestionQuantity.text = stringQuestionQuantity
    }

    private fun backToProfileFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QuestionManagementFragment().apply {
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