package com.prism.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prism.app.R
import com.prism.app.ui.CalibrationFragment
import com.prism.app.ui.TrackingFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calibrate -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CalibrationFragment())
                        .commit()
                    true
                }
                R.id.nav_live -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TrackingFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        // Default selected tab
        bottom.selectedItemId = R.id.nav_calibrate
    }
}
