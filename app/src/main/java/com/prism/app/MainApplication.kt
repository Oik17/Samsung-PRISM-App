package com.prism.app

import android.app.Application

class MainApplication : Application() {
    lateinit var repository: Repository
    var currentCalibrationRoomId: String? = null

    override fun onCreate() {
        super.onCreate()
        repository = Repository(applicationContext)
    }
}
