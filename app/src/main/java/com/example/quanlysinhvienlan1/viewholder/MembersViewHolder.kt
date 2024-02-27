package com.example.quanlysinhvienlan1.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R

class MembersViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
    val imgAvatarMember: ImageView = itemView.findViewById(R.id.view_avatarMember)
    val txtNameMember: TextView = itemView.findViewById(R.id.txt_NameMember)
    val txtEmailMember: TextView = itemView.findViewById(R.id.txt_EmailMember)
}