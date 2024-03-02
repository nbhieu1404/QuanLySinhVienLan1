package com.example.quanlysinhvienlan1.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.Score
import com.example.quanlysinhvienlan1.viewholder.AttendanceViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class AttendanceAdapter(private var attendanceList: List<Score>) :
    RecyclerView.Adapter<AttendanceViewHolder>() {
    val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_attendence, parent, false)
        return AttendanceViewHolder(itemView)
    }

    override fun getItemCount(): Int = attendanceList.size

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val currentItem = attendanceList[position]
        val memberID = currentItem.memberID

        fireStore.collection("users").document(memberID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val nameMember = documentSnapshot.getString("username").toString()
                    holder.txtNameMember.text = nameMember
                    val emailMember = documentSnapshot.getString("email").toString()
                    holder.txtEmail.text = emailMember
                    val fireStoreAvatar = documentSnapshot.getString("avatar")
                    fireStoreAvatar?.let {
                        Picasso.get().load(it).into(holder.viewAvatarMember)
                    }
                }
            }
        val inputScore = currentItem.getScoreToDay.toString()
        val finalScore = "Điểm: $inputScore"
        holder.txtScore.text = finalScore
    }
}