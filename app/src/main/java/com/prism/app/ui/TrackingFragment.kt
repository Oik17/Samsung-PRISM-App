package com.prism.app.ui

import android.Manifest
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.prism.app.MainApplication
import com.prism.app.MainViewModel
import com.prism.app.R
import com.prism.app.ml.SimpleKnn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackingFragment : Fragment() {
    private val vm: MainViewModel by viewModels({ requireActivity() })
    private lateinit var tvRoom: TextView
    private val requestPerm = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_tracking, container, false)
        tvRoom = v.findViewById(R.id.tvRoom)
        v.findViewById<View>(R.id.btnStartLive).setOnClickListener { startLive() }
        return v
    }

    private fun startLive() {
        requestPerm.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        // start service even when not calibrating — it will scan but won't save to DB
        requireActivity().startService(Intent(requireContext(), com.prism.app.services.WifiScannerService::class.java))
        // simple live inference loop: read recent fingerprints and last scan (this demo assumes scanner saved results)
        val app = requireActivity().application as MainApplication
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val snaps = app.repository.getRecentFingerprints()
                // build a fake "current scan" from newest timestamp: group latest scan by bssid
                val latest = snaps.groupBy { it.bssid }.mapValues { it.value.maxByOrNull { it.timestamp }!!.rssi }
                val prediction = SimpleKnn.predictRoom(latest, snaps)
                launch(Dispatchers.Main) {
                    tvRoom.text = "You are likely in: ${prediction ?: "Unknown"}"
                }
                kotlinx.coroutines.delay(3000)
            }
        }
    }
}
