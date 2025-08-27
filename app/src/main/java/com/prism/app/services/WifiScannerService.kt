package com.prism.app.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.prism.app.MainApplication
import com.prism.app.R
import com.prism.app.data.WifiFingerprint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WifiScannerService : Service() {

    private lateinit var wifiManager: WifiManager
    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (ContextCompat.checkSelfPermission(
                    this@WifiScannerService,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    val results = wifiManager.scanResults
                    CoroutineScope(Dispatchers.IO).launch {
                        val repo = (application as MainApplication).repository
                        val roomId = (application as MainApplication).currentCalibrationRoomId
                        if (roomId != null) {
                            for (r in results) {
                                val fp = WifiFingerprint(
                                    roomId = roomId,
                                    ssid = r.SSID,
                                    bssid = r.BSSID,
                                    rssi = r.level
                                )
                                repo.saveWifiFingerprint(fp)
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e("WifiScannerService", "Permission denied for wifi scan results", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        registerReceiver(scanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        startForeground(1, createNotification())
        scheduleScanLoop()
    }

    private fun scheduleScanLoop() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (ContextCompat.checkSelfPermission(
                        this@WifiScannerService,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        wifiManager.startScan()
                    } catch (e: SecurityException) {
                        android.util.Log.e("WifiScannerService", "Permission denied for wifi scan", e)
                    }
                }
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    private fun createNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "prism.scan"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(channelId, "Prism scanner", NotificationManager.IMPORTANCE_LOW))
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Prism: scanning Wi-Fi")
            .setContentText("Collecting fingerprints")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onDestroy() {
        unregisterReceiver(scanReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}