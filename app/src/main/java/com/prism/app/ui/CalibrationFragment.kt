package com.prism.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.prism.app.MainApplication
import com.prism.app.MainViewModel
import com.prism.app.R
import com.prism.app.services.WifiScannerService

class CalibrationFragment : Fragment() {
    private val vm: MainViewModel by viewModels({ requireActivity() })
    private lateinit var startBtn: Button
    private lateinit var roomName: EditText
    private lateinit var statusTv: TextView

    private val requestPerm = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        // no-op; user may deny -> handle
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_calibration, container, false)
        startBtn = v.findViewById(R.id.btnStart)
        roomName = v.findViewById(R.id.etRoom)
        statusTv = v.findViewById(R.id.tvStatus)
        startBtn.setOnClickListener { toggleCalibration() }
        v.findViewById<Button>(R.id.btnHotspotGuide).setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
        return v
    }

    private fun toggleCalibration() {
        val app = requireActivity().application as MainApplication
        val room = roomName.text.toString().trim()
        if (app.currentCalibrationRoomId == null) {
            if (room.isEmpty()) { Toast.makeText(requireContext(), "Enter room id", Toast.LENGTH_SHORT).show(); return }
            // request perms
            requestPerm.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            app.currentCalibrationRoomId = room
            statusTv.text = "Calibrating: $room — collecting scans"
            startBtn.text = "Stop"
            requireActivity().startService(Intent(requireContext(), WifiScannerService::class.java))
        } else {
            app.currentCalibrationRoomId = null
            statusTv.text = "Calibration stopped"
            startBtn.text = "Start"
            requireActivity().stopService(Intent(requireContext(), WifiScannerService::class.java))
        }
    }
}
