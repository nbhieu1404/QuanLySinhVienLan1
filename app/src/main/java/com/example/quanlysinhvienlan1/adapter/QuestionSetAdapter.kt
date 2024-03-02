package com.example.quanlysinhvienlan1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.QuestionSet
import com.example.quanlysinhvienlan1.viewholder.QuestionSetViewHolder
import com.google.firebase.firestore.FirebaseFirestore

class QuestionSetAdapter(private val questionSetList: List<QuestionSet>) :
    RecyclerView.Adapter<QuestionSetViewHolder>() {
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionSetViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.custom_item_quiz, parent, false)
        return QuestionSetViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: QuestionSetViewHolder, position: Int) {
        val current = questionSetList[position]
        holder.txtNameQuestionSet.text = current.setName
        val quantityQuestions = current.questions as List<String>
        var countQuantityQuestions: Int = 0
        for (question in quantityQuestions) {
            countQuantityQuestions++
        }
        val fullCountQuantityQuestions = "$countQuantityQuestions câu"
        holder.txtQuestionCount.text = fullCountQuantityQuestions

        if (current.status == 0) {
            holder.btnStopDoHomework.visibility = View.GONE
            holder.btnGiveHomework.visibility = View.VISIBLE
        }
        else if (current.status == 1) {
            holder.btnStopDoHomework.visibility = View.VISIBLE
            holder.btnGiveHomework.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
        holder.btnGiveHomework.setOnClickListener {
            listener?.onStartQuestionSetClick(position)
        }
        holder.btnStopDoHomework.setOnClickListener {
            listener?.onStopQuestionSetClick(position)
        }
    }

    override fun getItemCount(): Int = questionSetList.size


}