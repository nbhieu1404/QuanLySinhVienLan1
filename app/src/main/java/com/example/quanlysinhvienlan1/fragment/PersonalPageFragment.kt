package com.example.quanlysinhvienlan1.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PersonalPageFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var btnBackToProfileFragment: View? = null
    private var imvCoverImage: ImageView? = null
    private var imgAvatar: ImageView? = null
    private var txtUsername: TextView? = null
    private var txtEmailDetails: TextView? = null
    private var txtUsernameDetails: TextView? = null
    private var btnChangeCoverImage: ImageButton? = null
    private var btnChangeAvatar: ImageButton? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
//    private lateinit var activity: AppCompatActivity


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

        val view = inflater.inflate(R.layout.fragment_personal_page, container, false)
        mapping(view)
        loadData()
        clickEvent()
        return view
    }

    //Ánh xạ
    private fun mapping(view: View) {
        btnBackToProfileFragment = view.findViewById(R.id.btn_BackToProfileFragment)
        imvCoverImage = view.findViewById(R.id.imv_CoverImage)
        imgAvatar = view.findViewById(R.id.img_Avatar)
        txtUsername = view.findViewById(R.id.txt_Username)
        txtEmailDetails = view.findViewById(R.id.txt_EmailDetails)
        txtUsernameDetails = view.findViewById(R.id.txt_UsernameDetails)
        btnChangeCoverImage = view.findViewById(R.id.btn_ChangeCoverImage)
        btnChangeAvatar = view.findViewById(R.id.btn_ChangeAvatar)
    }

    // Sự kiện click
    private fun clickEvent() {
        btnBackToProfileFragment?.setOnClickListener {
            backToProfileFragment()
        }
        btnChangeAvatar?.setOnClickListener {
            val mainActivity = activity as? MainActivity
            if (mainActivity?.checkNeedsPermission() == true) {
                Toast.makeText(context, "Cấp quyền thành công", Toast.LENGTH_SHORT).show()
            } else {
                mainActivity?.requestNeedsPermission()
            }
        }
    }

    // popback fragment
    private fun backToProfileFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    // Load data user
    private fun loadData() {
        if (auth.currentUser != null) {
            val userID = auth.currentUser!!.uid
            val userDocRef = fireStore.collection("users").document(userID)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val fireStoreUsername = documentSnapshot.getString("username")
                        txtUsername?.text = fireStoreUsername
                        val fireStoreEmail = documentSnapshot.getString("email")
                        txtEmailDetails?.text = fireStoreEmail
                        txtUsernameDetails?.text = fireStoreUsername
//                        val fireStoreAvatar = documentSnapshot.getString("avatar")
//                        if(fireStoreAvatar!=null){
//                            imgAvatar?.loadImage(fireStoreAvatar)
//                        }
                    } else {
                        txtUsername?.text = "Loading..."
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FalseLoadDataPersonalPageFragment", "${exception.message}")
                }
        }
    }


//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        activity = context as AppCompatActivity
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PersonalPageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PersonalPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}