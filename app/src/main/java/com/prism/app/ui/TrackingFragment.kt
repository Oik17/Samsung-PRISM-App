package com.prism.app.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.prism.app.MainApplication
import com.prism.app.MainViewModel
import com.prism.app.R
import com.prism.app.ml.SimpleKnn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackingFragment : Fragment() {

    private val vm: MainViewModel by activityViewModels()
    private lateinit var tvRoom: TextView

    // Be explicit so the compiler doesn’t have to guess.
    private val requestPerm =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                _: Map<String, Boolean> -> /* no-op */
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_tracking, container, false)
        tvRoom = v.findViewById(R.id.tvRoom)
        v.findViewById<Button>(R.id.btnStartLive).setOnClickListener { startLive() }
        return v
    }

    private fun startLive() {
        // Ask for location permission (pre-Android 13 Wi-Fi scanning needs this).
        requestPerm.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))

        // Kick off your Wi-Fi scan service.
        requireActivity().startService(
            Intent(requireContext(), com.prism.app.services.WifiScannerService::class.java)
        )

        val app = requireActivity().application as MainApplication

        // Lifecycle-aware collection to avoid leaking/looping off-screen.
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    // If your repo type is too generic, help the compiler with an explicit type.
                    val snaps = app.repository.getRecentFingerprints()

                    // latest: Map<BSSID, RSSI of most recent reading>
                    val latest: Map<String, Int> = snaps
                        .groupBy { snap -> snap.bssid }
                        .mapValues { entry ->
                            val list = entry.value
                            val newest = list.maxByOrNull { snap -> snap.timestamp }
                            // Avoid !! tantrums. If somehow empty, treat as very weak signal.
                            newest?.rssi ?: Int.MIN_VALUE
                        }

                    val prediction = SimpleKnn.predictRoom(latest, snaps)

                    launch(Dispatchers.Main) {
                        tvRoom.text = getString(
                            R.string.tracking_result,
                            prediction ?: "Unknown"
                        )
                    }

                    delay(3000)
                }
            }
        }
    }
}
