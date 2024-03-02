package com.example.quanlysinhvienlan1.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.Question
import com.example.quanlysinhvienlan1.viewholder.QuestionViewHolder
import com.google.firebase.firestore.FirebaseFirestore

class QuestionAdapter(private var questionList: List<Question>) :
    RecyclerView.Adapter<QuestionViewHolder>() {
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    fun setData(newList: List<Question>) {
        questionList = newList
        notifyDataSetChanged()
    }
    private var index: Int = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_questions, parent, false)
        return QuestionViewHolder(itemView)
    }

    override fun getItemCount(): Int =
        questionList.size


    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val currentQuestion = questionList[position]
        holder.txtNameQuestion.text = currentQuestion.questionName
        val fullQuestion = "Câu ${position + 1}"
        holder.txtNumberQuestion.text = fullQuestion
        val correctAnswer: Int = currentQuestion.correctAnswerIndex
        var temp: Int = 0
        val questionLists = currentQuestion.options
        for (document in questionLists) {
            val radioButton: RadioButton =
                holder.rdgRadioShowAnswers.getChildAt(temp) as RadioButton
            radioButton.text = document
            if (temp == correctAnswer) {
                radioButton.isChecked = true
            }
            temp++
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)

        }
        holder.btnUpdateQuestion.setOnClickListener {
            listener?.onUpdateButtonClick(position)
        }


        holder.btnDeleteQuestion.setOnClickListener {
            listener?.onDeleteButtonClick(position)
        }
    }

}