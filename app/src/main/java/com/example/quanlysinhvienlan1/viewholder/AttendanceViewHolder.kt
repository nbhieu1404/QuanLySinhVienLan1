package com.example.quanlysinhvienlan1.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R

class AttendanceViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
    val txtNameMember: TextView = itemView.findViewById(R.id.txt_NameMember)
    val txtEmail: TextView = itemView.findViewById(R.id.txt_Email)
    val viewAvatarMember: ImageView = itemView.findViewById(R.id.view_avatarMemberAttendance)
    val txtScore: TextView = itemView.findViewById(R.id.txt_Score)
}