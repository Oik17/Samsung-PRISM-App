package com.prism.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.prism.app.MainApplication
import com.prism.app.R
import com.prism.app.data.WifiFingerprint
import com.prism.app.ml.SimpleKnn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetectionFragment : Fragment() {

    private lateinit var statusTv: TextView
    private lateinit var detectBtn: Button
    private lateinit var wifiManager: WifiManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_detection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusTv = view.findViewById(R.id.statusTv)
        detectBtn = view.findViewById(R.id.detectBtn)
        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        detectBtn.setOnClickListener {
            lifecycleScope.launch {
                val app = requireActivity().application as MainApplication
                val repo = app.repository

                // Get all training fingerprints
                val training = withContext(Dispatchers.IO) { repo.getAll() }

                if (training.isEmpty()) {
                    statusTv.text = "No calibration data yet!"
                    return@launch
                }

                // Check permissions first
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    statusTv.text = "Location permission required to scan Wi-Fi"
                    return@launch
                }

                // Do a fresh wifi scan
                wifiManager.startScan()
                val results = wifiManager.scanResults

                // Build test fingerprints (roomId = "unknown")
                val test = results.map { r ->
                    WifiFingerprint(
                        roomId = "unknown",
                        ssid = r.SSID,
                        bssid = r.BSSID,
                        rssi = r.level,
                        timestamp = System.currentTimeMillis()
                    )
                }

                // Run classifier using SimpleKnn object
                val predicted = SimpleKnn.predictRoom(
                    currentScan = test.associate { it.bssid to it.rssi },
                    fingerprints = training
                )

                statusTv.text = "Predicted Room: ${predicted ?: "Unknown"}"
            }
        }
    }
}
