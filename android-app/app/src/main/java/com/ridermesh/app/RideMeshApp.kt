package com.ridermesh.app

import android.app.Application
import android.content.Context
import com.ridermesh.app.encryption.CryptoManager
import com.ridermesh.app.network.DefaultMeshManager
import com.ridermesh.app.network.MeshManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class RideMeshApp : Application() {
    lateinit var serviceLocator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        serviceLocator = ServiceLocator(this)
    }
}

class ServiceLocator(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob())

    val meshManager: MeshManager by lazy { DefaultMeshManager(context, appScope) }
    val cryptoManager: CryptoManager by lazy { CryptoManager(context) }
}
