package com.example.quanlysinhvienlan1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.quanlysinhvienlan1.fragment.ExFragment
import com.example.quanlysinhvienlan1.fragment.HomeFragment
import com.example.quanlysinhvienlan1.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val exFragment = ExFragment()
        val profileFragment = ProfileFragment()
        var btmNav = findViewById<BottomNavigationView>(R.id.btm_nav)
        makeCurrentFragment(homeFragment)

        btmNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> makeCurrentFragment(homeFragment)
                R.id.ex -> makeCurrentFragment(exFragment)
                R.id.profile -> makeCurrentFragment(profileFragment)
            }
            true
        }


    }
    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.layout_Wrapper, fragment)
            commit()
        }
}