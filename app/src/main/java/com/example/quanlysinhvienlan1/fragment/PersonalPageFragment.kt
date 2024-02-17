package com.example.quanlysinhvienlan1.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PersonalPageFragment : Fragment() {
    // Lắng nghe kết quả chọn hình ảnh avatar
    private val pickImageAvatarLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    uploadImageAvatar(it)
                }
            }
        }

    // Lắng nghe kết quả chọn hình ảnh bìa
    private val pickCoverImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    uploadCoverImage(it)
                }
            }
        }
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
    private var AvatarProgressBar: ProgressBar? = null
    private var CoverImageProgressBar: ProgressBar? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

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
        AvatarProgressBar = view.findViewById(R.id.layout_AvatarProgressBar)
        CoverImageProgressBar = view.findViewById(R.id.layout_CoverImageProgressBar)
    }

    // Sự kiện click
    private fun clickEvent() {
        btnBackToProfileFragment?.setOnClickListener {
            backToProfileFragment()
        }
        btnChangeAvatar?.setOnClickListener {
            val mainActivity = activity as? MainActivity
            if (mainActivity?.checkNeedsPermission() == true) {
                startImageAvatarSelection()
            } else {
                mainActivity?.requestNeedsPermission()
            }
        }
        btnChangeCoverImage?.setOnClickListener {
            val mainActivity = activity as? MainActivity
            if (mainActivity?.checkNeedsPermission() == true) {
                startCoverImageSelection()
            } else {
                mainActivity?.requestNeedsPermission()
            }
        }
    }

    // Mở thư viện và chọn ảnh
    private fun startImageAvatarSelection() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageAvatarLauncher.launch(galleryIntent)
    }

    // Tải hình ảnh lên Storage và cập nhật URL của hình ảnh sang FireStorage
    private fun uploadImageAvatar(imageUri: Uri) {
        AvatarProgressBar?.visibility = View.VISIBLE
        val userID = auth.currentUser?.uid
        userID?.let {
            val storageRef = firebaseStorage.reference
            val imageRef = storageRef.child("avatars/${UUID.randomUUID()}")
            // Tải hình ảnh lên Storage
            val uploadTask = imageRef.putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    updateAvatarUrl(userID, downloadUri.toString())
                    val imageURL = downloadUri.toString()
                    // Hiển thị hình ảnh trên ImageView
                    Picasso.get().load(imageURL).into(imgAvatar)
                    AvatarProgressBar?.visibility = View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật ảnh đại diện thất bại",
                        Toast.LENGTH_SHORT

                    )
                        .show()
                    AvatarProgressBar?.visibility = View.GONE
                }
            }
        }
    }

    // Cập nhật URL của avatar trong firestorage
    private fun updateAvatarUrl(userID: String, avatarUrl: String) {
        val userDocRef = fireStore.collection("users").document(userID)
        userDocRef.update("avatar", avatarUrl)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Cập nhật ảnh đại diện thành công",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to update avatar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                        val fireStoreEmail = documentSnapshot.getString("email")
                        val fireStoreAvatar = documentSnapshot.getString("avatar")
                        val fireStoreCoverImage = documentSnapshot.getString("coverImage")

                        txtUsername?.text = fireStoreUsername
                        txtEmailDetails?.text = fireStoreEmail
                        txtUsernameDetails?.text = fireStoreUsername

                        fireStoreAvatar?.let {
                            Picasso.get().load(it).into(imgAvatar)
                        }
                        fireStoreCoverImage?.let {
                            Picasso.get().load(it).into(imvCoverImage)
                        }

                    } else {
                        txtUsername?.text = "Loading..."
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FalseLoadDataPersonalPageFragment", "${exception.message}")
                }
        }
    }


    private fun startCoverImageSelection() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickCoverImageLauncher.launch(galleryIntent)
    }

    private fun uploadCoverImage(imageUri: Uri) {
        CoverImageProgressBar?.visibility = View.VISIBLE
        val userID = auth.currentUser?.uid
        userID?.let {
            val storageRef = firebaseStorage.reference
            val imageRef = storageRef.child("coverImages/${UUID.randomUUID()}")
            // Tải hình ảnh lên Storage
            val uploadTask = imageRef.putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    updateCoverImageUrl(userID, downloadUri.toString())
                    val imageURL = downloadUri.toString()
                    // Hiển thị hình ảnh trên ImageView
                    Picasso.get().load(imageURL).into(imvCoverImage)
                    CoverImageProgressBar?.visibility = View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật ảnh bìa thất bại",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    CoverImageProgressBar?.visibility = View.GONE
                }
            }
        }
    }

    private fun updateCoverImageUrl(userID: String, coverImageUrl: String) {
        val userDocRef = fireStore.collection("users").document(userID)
        userDocRef.update("coverImage", coverImageUrl)
            .addOnSuccessListener {
                // Handle successful update
                Toast.makeText(requireContext(), "Cập nhật ảnh bìa thành công", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                // Handle failed update
                Toast.makeText(
                    requireContext(),
                    "Failed to update cover image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

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