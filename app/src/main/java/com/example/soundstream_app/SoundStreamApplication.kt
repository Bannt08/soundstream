package com.example.soundstream_app

import android.app.Application
import com.example.soundstream_app.data.AppDatabase
import com.example.soundstream_app.data.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SoundStreamApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppDatabase.initialize(this, applicationScope)
        PlaybackManager.initialize(this)
    }
}
