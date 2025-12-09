package com.ridermesh.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ridermesh.app.ui.screens.CreateRideScreen
import com.ridermesh.app.ui.screens.DebugScreen
import com.ridermesh.app.ui.screens.HomeScreen
import com.ridermesh.app.ui.screens.JoinRideScreen
import com.ridermesh.app.ui.screens.RideDashboardScreen
import com.ridermesh.app.ui.screens.SettingsScreen
import com.ridermesh.app.ui.viewmodel.RideViewModel

@Composable
fun RideMeshRoot(
    viewModel: RideViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") {
            HomeScreen(
                state = state,
                onCreateRide = { navController.navigate("create") },
                onJoinRide = { navController.navigate("join") },
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable("create") {
            CreateRideScreen(onCreate = { ride, host, pin ->
                viewModel.createRide(ride, host, pin)
                navController.navigate("dashboard")
            })
        }
        composable("join") {
            JoinRideScreen(
                advertisements = state.discoveredRides,
                onManualJoin = { rideId, rideCode, riderName, pin ->
                    viewModel.joinRide(rideId, rideCode, riderName, pin)
                    navController.navigate("dashboard")
                },
                onScanQr = { /* TODO: launch scanning */ }
            )
        }
        composable("dashboard") {
            val session = state.activeSession
            if (session != null) {
                RideDashboardScreen(
                    session = session,
                    peers = state.peers,
                    messages = state.messages,
                    onSendMessage = viewModel::sendTextMessage,
                    onLeave = {
                        viewModel.resetRide()
                        navController.popBackStack("home", inclusive = false)
                    },
                    onOpenDebug = { navController.navigate("debug") }
                )
            } else {
                HomeScreen(state = state, onCreateRide = { navController.navigate("create") }, onJoinRide = { navController.navigate("join") }, onOpenSettings = { navController.navigate("settings") })
            }
        }
        composable("settings") {
            SettingsScreen(
                onSaveName = { /* TODO */ },
                onToggleAutoJoin = { /* TODO */ },
                onToggleVoice = { /* TODO */ },
                onOpenLogs = { navController.navigate("debug") }
            )
        }
        composable("debug") {
            DebugScreen(peers = state.peers, messages = state.messages)
        }
    }
}
