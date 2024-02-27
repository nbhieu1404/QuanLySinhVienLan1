package com.example.quanlysinhvienlan1.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.Question
import com.example.quanlysinhvienlan1.viewholder.QuestionViewHolder
import com.google.firebase.firestore.FirebaseFirestore

class QuestionAdapter(private val questionList: List<Question>) :
    RecyclerView.Adapter<QuestionViewHolder>() {
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var listener: OnItemClickListener? = null
    private var index: Int = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_questions, parent, false)
        return QuestionViewHolder(itemView)
    }

    override fun getItemCount(): Int =
        questionList.size


    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val currentQuestion = questionList[position].questionID
        Log.d("in4", currentQuestion)
        fireStore.collection("Questions").document(currentQuestion).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
//                    Log.d("dataAdapter", "succses")
                    val inputQuestionName = documentSnapshot.getString("questionName").toString()
                    val fullQuestion = "Câu ${position+1}"
                    holder.txtNameQuestion.text = documentSnapshot.getString("questionName").toString()
                    holder.txtNumberQuestion.text = fullQuestion
                    val correctAnswer: Int =
                        documentSnapshot.get("correctAnswerIndex").toString().toIntOrNull() ?: 0
                    var temp: Int = 0
                    val questionLists =
                        documentSnapshot.get("options") as? List<String> ?: emptyList()
                    for (document in questionLists) {
                        val radioButton: RadioButton =
                            holder.rdgRadioShowAnswers.getChildAt(temp) as RadioButton
                        radioButton.text = document
                        if (temp == correctAnswer) {
                            radioButton.isChecked = true
                        }
                        temp++
                    }

                }
                else{
                    Log.e("errorAdapter", "Lỗi")
                }
            }
            .addOnFailureListener{
                Log.e("errorAdapter", it.toString())
            }
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
    }

}