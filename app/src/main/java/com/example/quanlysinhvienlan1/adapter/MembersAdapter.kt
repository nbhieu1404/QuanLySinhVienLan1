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
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_members, parent, false)
        return MembersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        val currentMember = membersLists[position]
        holder.txtNameMember.text = currentMember.username
        holder.txtEmailMember.text = currentMember.email
        val avatar = currentMember.avatar
        avatar.let {
            Picasso.get().load(it).into(holder.imgAvatarMember)
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
        holder.btnDeleteMember.setOnClickListener {
            listener?.onDeleteButtonClick(position)
        }
    }


    override fun getItemCount(): Int =
        membersLists.size

}