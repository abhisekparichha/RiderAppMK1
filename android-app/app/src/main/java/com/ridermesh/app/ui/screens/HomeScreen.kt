package com.ridermesh.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ridermesh.app.ui.viewmodel.RideUiState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: RideUiState,
    onCreateRide: () -> Unit,
    onJoinRide: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Rider Mesh", style = MaterialTheme.typography.headlineMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current ride")
                val ride = state.activeSession
                if (ride == null) {
                    Text("Not connected")
                } else {
                    Text("${ride.rideName} (${ride.joinCode})")
                }
            }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateRide) {
            Text("Create Ride")
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onJoinRide) {
            Text("Join Ride")
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onOpenSettings) {
            Text("Settings & Debug")
        }
    }
}
