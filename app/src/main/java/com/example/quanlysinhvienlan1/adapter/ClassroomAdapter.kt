package com.example.quanlysinhvienlan1.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.Classroom
import com.example.quanlysinhvienlan1.viewholder.ClassroomViewHolder
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ClassroomAdapter(private val classRoomList: List<Classroom>) :
    RecyclerView.Adapter<ClassroomViewHolder>() {
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassroomViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_classroom, parent, false)
        return ClassroomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ClassroomViewHolder, position: Int) {
        val currentClass = classRoomList[position]

        fireStore.collection("classes").document(currentClass.idClassroom).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    displayClassroomInfo(document, holder)
                }
            }
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
    }

    private fun displayClassroomInfo(document: DocumentSnapshot, holder: ClassroomViewHolder) {
        holder.txtClassName.text = document.getString("nameClass")
        holder.txtMembersQuantity.text = document.getLong("membersQuantity").toString()

        val teacherId = document.getString("teacher")
        if (teacherId != null) {
            fireStore.collection("users").document(teacherId).get()
                .addOnSuccessListener { teacherDocument ->
                    if (teacherDocument.exists()) {
                        holder.txtNameTeacherClass.text = teacherDocument.getString("username")
                        val fireStoreAvatar = teacherDocument.getString("avatar")
                        fireStoreAvatar?.let {
                            Picasso.get().load(it).into(holder.imgAvatarTeacher)
                        }
                    }
                }
        }
    }

    override fun getItemCount(): Int = classRoomList.size

}