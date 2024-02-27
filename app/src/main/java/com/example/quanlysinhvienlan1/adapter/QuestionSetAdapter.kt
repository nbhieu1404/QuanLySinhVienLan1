package com.example.quanlysinhvienlan1.adapter

import android.view.LayoutInflater
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
        val current = questionSetList[position].setId
        fireStore.collection("QuestionSet").document(current).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    holder.txtNameQuestionSet.text = document.get("setName").toString()
                    val quantityQuestions = document.get("questions") as? List<String> ?: emptyList()
                    var countQuantityQuestions: Int = 0
                    for (question in quantityQuestions) {
                        countQuantityQuestions++
                    }
                    val fullCountQuantityQuestions = "$countQuantityQuestions câu"
                    holder.txtQuestionCount.text = fullCountQuantityQuestions
                }
            }
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int = questionSetList.size


}