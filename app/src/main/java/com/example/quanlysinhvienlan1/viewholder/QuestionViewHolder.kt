package com.example.quanlysinhvienlan1.viewholder
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R

class QuestionViewHolder(item: View) : RecyclerView.ViewHolder(item){
    val txtNumberQuestion: TextView = item.findViewById(R.id.txt_NumberQuestion)
    val txtNameQuestion: TextView = item.findViewById(R.id.txt_NameQuestion)
    val rdgRadioShowAnswers: RadioGroup = item.findViewById(R.id.rdg_RadioShowAnswers)
    val btnUpdateQuestion: Button = item.findViewById(R.id.btn_UpdateQuestion)
    val btnDeleteQuestion: Button = item.findViewById(R.id.btn_DeleteQuestion)
}