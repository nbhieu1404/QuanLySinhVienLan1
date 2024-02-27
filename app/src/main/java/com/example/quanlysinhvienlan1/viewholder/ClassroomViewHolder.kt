package com.example.quanlysinhvienlan1.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R

class ClassroomViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imgAvatarTeacher: ImageView = itemView.findViewById(R.id.view_avatarTeacher)
    val txtClassName: TextView = itemView.findViewById(R.id.txt_NameClassroom)
    val txtNameTeacherClass: TextView = itemView.findViewById(R.id.txt_NameTeacherClass)
    val txtMembersQuantity: TextView = itemView.findViewById(R.id.txt_MembersQuantity)
}