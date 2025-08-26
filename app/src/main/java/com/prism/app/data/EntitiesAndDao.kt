package com.prism.app.data

import androidx.room.*
import java.lang.System.currentTimeMillis

@Entity(tableName = "wifi_fingerprints")
data class WifiFingerprint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: String,
    val ssid: String?,
    val bssid: String,
    val rssi: Int,
    val timestamp: Long = currentTimeMillis()
)

@Entity(tableName = "sensor_raw")
data class SensorRaw(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val accelX: Float, val accelY: Float, val accelZ: Float,
    val gyroX: Float, val gyroY: Float, val gyroZ: Float
)

@Entity(tableName = "pdr_features")
data class PdrFeature(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val stepCount: Int,
    val strideMeters: Float,
    val headingDeg: Float
)

@Entity(tableName = "room_labels")
data class RoomLabel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: String,
    val ts: Long = currentTimeMillis(),
    val userId: String? = null
)

@Dao
interface PrismDao {
    // Wi-Fi
    @Insert suspend fun insertWifi(vararg w: WifiFingerprint)
    @Query("SELECT * FROM wifi_fingerprints WHERE roomId = :roomId")
    suspend fun fingerprintsForRoom(roomId: String): List<WifiFingerprint>
    @Query("SELECT * FROM wifi_fingerprints ORDER BY timestamp DESC LIMIT 500")
    suspend fun recentFingerprints(): List<WifiFingerprint>

    // Sensors
    @Insert suspend fun insertSensor(vararg s: SensorRaw)
    @Insert suspend fun insertPdr(vararg p: PdrFeature)
    @Insert suspend fun insertRoomLabel(vararg r: RoomLabel)

    @Query("SELECT COUNT(*) FROM wifi_fingerprints WHERE roomId = :roomId")
    suspend fun countFingerprints(roomId: String): Int
}
