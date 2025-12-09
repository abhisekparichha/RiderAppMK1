package com.ridermesh.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ridermesh.app.ui.RideMeshRoot
import com.ridermesh.app.ui.theme.RideMeshTheme
import com.ridermesh.app.ui.viewmodel.RideViewModel

class MainActivity : ComponentActivity() {
    private val rideViewModel: RideViewModel by viewModels {
        val locator = (application as RideMeshApp).serviceLocator
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RideViewModel(locator.meshManager, locator.cryptoManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RideMeshTheme {
                RideMeshRoot(viewModel = rideViewModel)
            }
        }
    }
}
