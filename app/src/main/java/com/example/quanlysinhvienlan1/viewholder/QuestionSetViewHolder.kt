package com.example.quanlysinhvienlan1.viewholder

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R

class QuestionSetViewHolder(item: View): RecyclerView.ViewHolder(item) {
    val txtNameQuestionSet: TextView = item.findViewById(R.id.txt_NameQuiz)
    val txtQuestionCount: TextView = item.findViewById(R.id.txt_QuizCount)
    val btnGiveHomework: Button = item.findViewById(R.id.btn_GiveHomework)
    val btnStopDoHomework: Button = item.findViewById(R.id.btn_StopDoHomework)
}