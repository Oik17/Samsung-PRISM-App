package com.prism.app

import android.content.Context
import com.prism.app.data.AppDatabase
import com.prism.app.data.WifiFingerprint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(context: Context)  {
    private val dao = AppDatabase.get(context).prismDao()

    suspend fun saveWifiFingerprint(fp: WifiFingerprint) = withContext(Dispatchers.IO) {
        dao.insertWifi(fp)
    }

    suspend fun getRecentFingerprints() = withContext(Dispatchers.IO) {
        dao.recentFingerprints()
    }

    suspend fun countForRoom(roomId: String) = withContext(Dispatchers.IO) {
        dao.countFingerprints(roomId)
    }

    suspend fun fingerprintsForRoom(roomId: String) = withContext(Dispatchers.IO) {
        dao.fingerprintsForRoom(roomId)
    }
}
