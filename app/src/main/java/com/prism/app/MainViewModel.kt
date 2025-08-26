package com.prism.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prism.app.data.WifiFingerprint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app.applicationContext)

    private val _status = MutableStateFlow("idle")
    val status: StateFlow<String> = _status

    private val _currentRoom = MutableStateFlow<String?>(null)
    val currentRoom: StateFlow<String?> = _currentRoom

    fun setStatus(s: String) { _status.value = s }

    fun saveWifiFingerprint(fp: WifiFingerprint) = viewModelScope.launch {
        repo.saveWifiFingerprint(fp)
    }

    fun setCurrentRoom(roomId: String?) { _currentRoom.value = roomId }
}
