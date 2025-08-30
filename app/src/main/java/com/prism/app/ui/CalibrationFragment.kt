package com.prism.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.prism.app.MainApplication
import com.prism.app.R
import com.prism.app.data.AppDatabase
import com.prism.app.data.WifiFingerprint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.System.currentTimeMillis

class CalibrationFragment : Fragment() {
    private lateinit var startBtn: Button
    private lateinit var roomName: EditText
    private lateinit var statusTv: TextView
    private lateinit var wifiManager: WifiManager

    private val requestPerm = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        // no-op; handle inside toggleCalibration
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_calibration, container, false)
        startBtn = v.findViewById(R.id.btnStart)
        roomName = v.findViewById(R.id.etRoom)
        statusTv = v.findViewById(R.id.tvStatus)

        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

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
            if (room.isEmpty()) {
                Toast.makeText(requireContext(), "Enter room id", Toast.LENGTH_SHORT).show()
                return
            }

            // ask permission
            requestPerm.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))

            app.currentCalibrationRoomId = room
            statusTv.text = "Calibrating: $room — collecting scans..."
            startBtn.text = "Stop"

            startWifiScanning(room)

        } else {
            app.currentCalibrationRoomId = null
            statusTv.text = "Calibration stopped"
            startBtn.text = "Start"
        }
    }

    private fun startWifiScanning(roomId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.get(requireContext()).prismDao()

            while ((requireActivity().application as MainApplication).currentCalibrationRoomId == roomId) {
                // trigger wifi scan
                wifiManager.startScan()
                val results: List<ScanResult> = wifiManager.scanResults

                results.forEach { result ->
                    val fingerprint = WifiFingerprint(
                        roomId = roomId,
                        ssid = result.SSID,
                        bssid = result.BSSID,
                        rssi = result.level,
                        timestamp = currentTimeMillis()
                    )
                    dao.insertWifi(fingerprint)
                }

                // update UI with progress
                val count = dao.countFingerprints(roomId)
                withContext(Dispatchers.Main) {
                    statusTv.text = "Calibrating: $roomId — $count samples collected"
                }

                // sleep before next scan
                kotlinx.coroutines.delay(5000) // every 5 sec
            }
        }
    }
}
