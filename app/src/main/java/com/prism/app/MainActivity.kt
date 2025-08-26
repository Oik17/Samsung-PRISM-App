package com.prism.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prism.app.ui.CalibrationFragment
import com.prism.app.ui.TrackingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_main)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_calibrate -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, CalibrationFragment()).commit()
                    true
                }
                R.id.nav_live -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, TrackingFragment()).commit()
                    true
                }
                else -> false
            }
        }
        // default
        bottom.selectedItemId = R.id.nav_calibrate
    }
}
