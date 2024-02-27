package com.example.quanlysinhvienlan1.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.adapter.QuestionAdapter
import com.example.quanlysinhvienlan1.data.Question
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
            .addOnFailureListener{
                Log.e("dataQuestion", it.toString())
            }
    }

    private fun clickEvents(idQuestionSet: String) {
        btnCreateNewQuestion.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("QuestionSetID", idQuestionSet)
            val fragment = CreateNewQuestion()
            fragment.arguments = bundle

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.layout_Wrapper, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        btnQuestionManagementBackClassroomManagement.setOnClickListener {
            backToProfileFragment()
        }

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