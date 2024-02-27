package com.example.quanlysinhvienlan1.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.adapter.MembersAdapter
import com.example.quanlysinhvienlan1.data.User
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ListMembersFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var btnBackClassroomManagement: ImageView
    private lateinit var rcvListMembers: RecyclerView
    private lateinit var membersAdapter: MembersAdapter

    private val membersList = mutableListOf<User>()

    private lateinit var fireStore: FirebaseFirestore

    private lateinit var prbReloadDataListMembers: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_members, container, false)
        fireStore = FirebaseFirestore.getInstance()
        mappingViews(view)

        // lấy data được gửi từ ClassroomManagementFragment
        val idClassroom = arguments?.getString("idClassroom")
        val idClass = idClassroom.toString()

        if (idClassroom != null) {
            getDataMembers(idClass)
        }

        rcvListMembers.layoutManager = LinearLayoutManager(requireContext())
        membersAdapter = MembersAdapter(membersList)
        rcvListMembers.adapter = membersAdapter


        clickEvent()
        return view
    }

    private fun mappingViews(view: View) {
        btnBackClassroomManagement = view.findViewById(R.id.btn_BackClassroomManagement)
        rcvListMembers = view.findViewById(R.id.rcv_ListMembers)
        prbReloadDataListMembers = view.findViewById(R.id.prb_ReloadDataListMembers)
    }

    private fun clickEvent() {
        btnBackClassroomManagement.setOnClickListener {
            backToCreatedClassroomManagement()
        }
    }

    private fun backToCreatedClassroomManagement() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun getDataMembers(idClassroom: String) {
        membersList.clear()
        // Lấy dữ liệu từ Firestore và cập nhật vào membersList
        val classDocRef = fireStore.collection("classes").document(idClassroom)
        classDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val memberIds = document.get("members") as? List<String> ?: emptyList()
                for (memberId in memberIds) {
                    fireStore.collection("users").document(memberId).get()
                        .addOnSuccessListener { memberDocument ->
                            if (memberDocument.exists()) {
                                val id = memberDocument.id
                                val username = memberDocument.getString("username") ?: ""
                                val email = memberDocument.getString("email") ?: ""
                                val avatarUrl = memberDocument.getString("avatar") ?: ""
                                val coverImageUrl = memberDocument.getString("coverImage") ?: ""
                                val user = User(id, username, email, avatarUrl, coverImageUrl)
                                membersList.add(user)
                                membersAdapter.notifyDataSetChanged()
                                prbReloadDataListMembers.visibility = View.GONE
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListMembersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}