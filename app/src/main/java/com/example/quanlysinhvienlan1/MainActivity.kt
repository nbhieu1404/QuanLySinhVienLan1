package com.example.quanlysinhvienlan1

import android.Manifest
import android.content.Context
import android.content.DialogInterface
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
import com.example.quanlysinhvienlan1.fragment.ExFragment
import com.example.quanlysinhvienlan1.fragment.HomeFragment
import com.example.quanlysinhvienlan1.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
val ATLEAST_TIRAMISU = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
val REQUEST_CODE = 999
class MainActivity : AppCompatActivity() {
    private val homeFragment = HomeFragment()
    private val exFragment = ExFragment()
    private val profileFragment = ProfileFragment()
    private var btmNav: BottomNavigationView? = null
    private var currentFragment: Fragment? = null
    private val backStackTag = "current_Fragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btmNav = findViewById(R.id.btm_nav)
        makeCurrentFragment(homeFragment)

        btmNav?.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> makeCurrentFragment(homeFragment)
                R.id.ex -> makeCurrentFragment(exFragment)
                R.id.profile -> makeCurrentFragment(profileFragment)
            }
            true
        }


    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentFragment == homeFragment) {
                showExitDialog()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.layout_Wrapper, fragment)
//            addToBackStack(null)  // Thêm vào ngăn xếp back
            if (currentFragment != homeFragment) {
                addToBackStack(backStackTag)
            }
            commit()
        }
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Thoát ứng dụng")
            .setMessage("Bạn có chắc chắn muốn thoát không?")
            .setPositiveButton("Có") { _, _ ->
                finish()
            }
            .setNegativeButton("Không", null)
            .show()
    }
    // Kiểm tra thiết bị đã được cấp quyền hay chưa
    fun checkNeedsPermission(): Boolean {
        val result: Int
        if (!ATLEAST_TIRAMISU) {
            result = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            result = checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
        }
        return result == PackageManager.PERMISSION_GRANTED
    }

    // Yêu cầu quyền truy cập
    fun requestNeedsPermission() {
        if (!checkNeedsPermission()) {
            val permission: String
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission = Manifest.permission.READ_MEDIA_IMAGES
            } else {
                permission = Manifest.permission.READ_EXTERNAL_STORAGE
            }
            requestPermissions(
                arrayOf(permission),
                REQUEST_CODE
            )
        }
    }
    // Đưa người dùng đến cài đặt tự quyết quyền truy cập
    fun goToSettings(context: Context) {
        // Tạo dialog
        val dialog = AlertDialog.Builder(context)
            .setTitle("Chưa cấp quyền truy cập bộ nhớ")
            .setMessage(R.string.request_permissions)
            .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, _ ->
                // Mở cài đặt
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
                dialog.dismiss()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            })
            .create()

        // Hiển thị dialog
        dialog.show()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 999) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                    "Đã cấp quyền truy cập overide",
                    Toast.LENGTH_SHORT)
                    .show()
            } else {
                goToSettings(this)
            }
        }
    }
}