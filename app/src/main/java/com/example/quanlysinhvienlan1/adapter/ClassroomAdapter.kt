package com.example.quanlysinhvienlan1.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.OnItemLongClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.Classroom
import com.example.quanlysinhvienlan1.viewholder.ClassroomViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ClassroomAdapter(private var classRoomList: List<Classroom>) :
    RecyclerView.Adapter<ClassroomViewHolder>() {

    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var listener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.onItemLongClickListener = listener
    }

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
        holder.txtClassName.text = currentClass.nameClass
        val membersQuantity = currentClass.membersQuantity.toString()
        val members = currentClass.members.size
        holder.txtMembersQuantity.text = "$members/$membersQuantity"

        fireStore.collection("QuestionSet").whereEqualTo("classroomID", currentClass.idClassroom)
            .whereEqualTo("status", 1).get().addOnSuccessListener {
                if (it.isEmpty) {
                    holder.txtStatusQuestion.text = "Lớp học đang đóng"
                    holder.txtStatusQuestion.setTextColor(Color.parseColor("#FF0000")) // Đỏ
                }else{
                    holder.txtStatusQuestion.text = "Hiện tại bạn có thể thực hành"
                    holder.txtStatusQuestion.setTextColor(Color.parseColor("#00FF00")) // Xanh lục

                }
        }
        val teacherId = currentClass.teacher
        fireStore.collection("users").document(teacherId).get()
            .addOnSuccessListener { teacherDocument ->
                if (teacherDocument.exists()) {
                    val nameTeacher = teacherDocument.getString("username").toString()
                    holder.txtNameTeacherClass.text = "Giáo viên: $nameTeacher"
                    val fireStoreAvatar = teacherDocument.getString("avatar")
                    fireStoreAvatar?.let {
                        Picasso.get().load(it).into(holder.imgAvatarTeacher)
                    }
                }
            }
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.onItemLongClick(position)
            true
        }
    }

    // Phương thức để filter danh sách lớp học
    fun filter(text: String): List<Classroom> {
        val filteredList = mutableListOf<Classroom>()
        for (classroom in classRoomList) {
            if (classroom.nameClass.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(classroom)
            }
        }
        return filteredList
    }

    // Phương thức để cập nhật dữ liệu của adapter
    fun setData(data: List<Classroom>) {
        classRoomList = data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = classRoomList.size

}