package com.example.quanlysinhvienlan1.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.OnItemClickListener
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.User
import com.example.quanlysinhvienlan1.viewholder.MembersViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class MembersAdapter(private val membersLists: List<User>) :
    RecyclerView.Adapter<MembersViewHolder>() {
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var listener: OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_members, parent, false)
        return MembersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        val currentMember = membersLists[position].userID

        fireStore.collection("users").document(currentMember).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
//                    displayMemberInfo(holder,currentMember)
                    holder.txtNameMember.text = document.getString("username")
                    holder.txtEmailMember.text = document.getString("email")
                    val fireStoreAvatar = document.getString("avatar")
                    fireStoreAvatar?.let {
                        Picasso.get().load(it).into(holder.imgAvatarMember)
                    }
                }
            }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
    }

//    private fun displayMemberInfo(holder: MembersViewHolder, id: String) {
//        val memberDocRef = fireStore.collection("classes").document(idClassroom).get()
//        val memberIds = memberDocRef as? List<String> ?: emptyList()
//        for (memberId in memberIds) {
//        fireStore.collection("users").document(id).get()
//            .addOnSuccessListener { memberDocument ->
//                if (memberDocument.exists()) {
//                    holder.txtNameMember.text = memberDocument.getString("username")
//                    holder.txtEmailMember.text = memberDocument.getString("email")
//                    val fireStoreAvatar = memberDocument.getString("avatar")
//                    fireStoreAvatar?.let {
//                        Picasso.get().load(it).into(holder.imgAvatarMember)
//                    }
//                }
//            }
//    }

    override fun getItemCount(): Int =
        membersLists.size

}