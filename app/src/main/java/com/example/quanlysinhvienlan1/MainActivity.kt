package com.example.quanlysinhvienlan1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.quanlysinhvienlan1.fragment.CreatedClassroomFragment
import com.example.quanlysinhvienlan1.fragment.HomeFragment
import com.example.quanlysinhvienlan1.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

val ATLEAST_TIRAMISU = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
val REQUEST_CODE = 999
val auth: FirebaseAuth = FirebaseAuth.getInstance()

class MainActivity : AppCompatActivity() {
    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val createdClassroomFragment = CreatedClassroomFragment()
    private lateinit var btmNav: BottomNavigationView
    private lateinit var currentFragment: Fragment
    private val backStackTag = "current_Fragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Ánh xạ các Views
        mappingViews()
        // Kết nối các fragment
        setUpBottomNavigation()
    }

    // Ánh xạ Views
    private fun mappingViews() {
        btmNav = findViewById(R.id.btm_nav)
        // Luôn khởi tạo fragment home đầu tiên
        makeCurrentFragment(homeFragment)
    }

    // Thay đổi fragments khi chọn icon bNav
    private fun setUpBottomNavigation() {
        btmNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> makeCurrentFragment(homeFragment)
                R.id.createdClassroom -> makeCurrentFragment(createdClassroomFragment)
                R.id.profile -> makeCurrentFragment(profileFragment)
            }
            true
        }
    }

    // Nếu ở home mà ấn back thì hỏi -> xác nhận thoát
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentFragment == homeFragment) {
                showExitDialog()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // Thay đổi fragments
    fun makeCurrentFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.layout_Wrapper, fragment)

            // Xếp các frm vào ngăn xếp
            if (currentFragment != homeFragment) {
                addToBackStack(backStackTag)
            }
            commit()
        }
    }

    // Hiển thị dialog thoát ứng dụng
    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Thoát ứng dụng")
            .setMessage("Bạn có chắc chắn muốn thoát không?")
            .setPositiveButton("Có") { _, _ -> finish() }
            .setNegativeButton("Không", null)
            .show()
    }

    // Kiểm tra phiên bản Android
    fun checkNeedsPermission(): Boolean {
        val permission =
            if (ATLEAST_TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    // Yêu cầu cấp quyền đọc hình ảnh
    fun requestNeedsPermission() {
        val permission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        requestPermissions(arrayOf(permission), REQUEST_CODE)
    }

    // Không cho phép cấp quyền -> vào cài đặt cấp quyên
    fun goToSettings(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Chưa cấp quyền truy cập bộ nhớ")
            .setMessage(R.string.request_permissions)
            .setPositiveButton("Ok") { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    // Xử lý code cấp quyền truy cập hình ảnh
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền truy cập", Toast.LENGTH_SHORT).show()
            } else {
                goToSettings(this)
            }
        }
    }

    // Khởi tạo random id
    fun generateRandomClassCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}