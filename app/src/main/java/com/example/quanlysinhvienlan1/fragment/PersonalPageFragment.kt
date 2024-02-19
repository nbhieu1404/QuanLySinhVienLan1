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
import com.example.quanlysinhvienlan1.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PersonalPageFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var btnBackToProfileFragment: View
    private lateinit var imvCoverImage: ImageView
    private lateinit var imgAvatar: ImageView
    private lateinit var txtUsername: TextView
    private lateinit var txtEmailDetails: TextView
    private lateinit var txtUsernameDetails: TextView
    private lateinit var btnChangeCoverImage: ImageButton
    private lateinit var btnChangeAvatar: ImageButton
    private lateinit var prbAvatar: ProgressBar
    private lateinit var prbCoverImage: ProgressBar

    private lateinit var fireStore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage

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
        fireStore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        // Anh xạ các view
        mappingViews(view)
        // Load data trang
        loadData()
        // Sự kiện click
        clickEvent()
        return view
    }

    //Ánh xạ
    private fun mappingViews(view: View) {
        btnBackToProfileFragment = view.findViewById(R.id.btn_BackToProfileFragment)
        imvCoverImage = view.findViewById(R.id.imv_CoverImage)
        imgAvatar = view.findViewById(R.id.img_Avatar)
        txtUsername = view.findViewById(R.id.txt_Username)
        txtEmailDetails = view.findViewById(R.id.txt_EmailDetails)
        txtUsernameDetails = view.findViewById(R.id.txt_UsernameDetails)
        btnChangeCoverImage = view.findViewById(R.id.btn_ChangeCoverImage)
        btnChangeAvatar = view.findViewById(R.id.btn_ChangeAvatar)
        prbAvatar = view.findViewById(R.id.layout_AvatarProgressBar)
        prbCoverImage = view.findViewById(R.id.layout_CoverImageProgressBar)

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

                        txtUsername.text = fireStoreUsername
                        txtEmailDetails.text = fireStoreEmail
                        txtUsernameDetails.text = fireStoreUsername

                        fireStoreAvatar?.let {
                            Picasso.get().load(it).into(imgAvatar)
                        }
                        fireStoreCoverImage?.let {
                            Picasso.get().load(it).into(imvCoverImage)
                        }
                        prbAvatar.visibility = View.GONE
                        prbCoverImage.visibility = View.GONE
                    } else {
                        txtUsername.text = "..."
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FalseLoadDataPersonalPageFragment", "${exception.message}")
                }
        }
    }

    // Sự kiện click
    private fun clickEvent() {
        btnBackToProfileFragment.setOnClickListener {
            backToProfileFragment()
        }
        btnChangeAvatar.setOnClickListener {
            val mainActivity = activity as? MainActivity
            if (mainActivity?.checkNeedsPermission() == true) {
                startImageAvatarSelection()
            } else {
                mainActivity?.requestNeedsPermission()
            }
        }
        btnChangeCoverImage.setOnClickListener {
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
        prbAvatar.visibility = View.VISIBLE
        val userID = auth.currentUser?.uid
        userID?.let {
            val storageRef = firebaseStorage.reference
            val imageRef = storageRef.child("avatars/${UUID.randomUUID()}")
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
                    Picasso.get().load(imageURL).into(imgAvatar)
                    prbAvatar.visibility = View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật ảnh đại diện thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                    prbAvatar.visibility = View.GONE
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
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to update avatar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun startCoverImageSelection() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickCoverImageLauncher.launch(galleryIntent)
    }

    private fun uploadCoverImage(imageUri: Uri) {
        prbCoverImage.visibility = View.VISIBLE
        val userID = auth.currentUser?.uid
        userID?.let {
            val storageRef = firebaseStorage.reference
            val imageRef = storageRef.child("coverImages/${UUID.randomUUID()}")
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
                    Picasso.get().load(imageURL).into(imvCoverImage)
                    prbCoverImage.visibility = View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật ảnh bìa thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                    prbCoverImage.visibility = View.GONE
                }
            }
        }
    }

    private fun updateCoverImageUrl(userID: String, coverImageUrl: String) {
        val userDocRef = fireStore.collection("users").document(userID)
        userDocRef.update("coverImage", coverImageUrl)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Cập nhật ảnh bìa thành công",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to update cover image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // popback fragment
    private fun backToProfileFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    companion object {
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