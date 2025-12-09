package com.ridermesh.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ridermesh.app.network.RideAdvertisement

@Composable
fun JoinRideScreen(
    modifier: Modifier = Modifier,
    advertisements: List<RideAdvertisement>,
    onManualJoin: (rideId: String, rideCode: String, riderName: String, pin: String?) -> Unit,
    onScanQr: () -> Unit
) {
    var rideId by remember { mutableStateOf("") }
    var rideCode by remember { mutableStateOf("") }
    var riderName by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Nearby rides")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
            items(advertisements) { ad ->
                ListItem(
                    headlineContent = { Text(ad.rideName) },
                    supportingContent = { Text("Host: ${ad.hostName} · RSSI ${ad.signalStrength}") },
                    leadingContent = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingContent = {
                        Button(onClick = {
                            rideId = ad.rideId
                            rideCode = ad.rideName // Placeholder until Wi-Fi Direct credential exchange is wired.
                        }) {
                            Text("Fill")
                        }
                    }
                )
            }
        }
        Button(onClick = onScanQr) { Text("Scan QR") }
        OutlinedTextField(value = rideId, onValueChange = { rideId = it }, label = { Text("Ride ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = rideCode, onValueChange = { rideCode = it }, label = { Text("Join code") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = riderName, onValueChange = { riderName = it }, label = { Text("Your name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("PIN (optional)") }, modifier = Modifier.fillMaxWidth())
        Button(enabled = rideId.isNotBlank() && rideCode.isNotBlank() && riderName.isNotBlank(), onClick = {
            onManualJoin(rideId, rideCode, riderName, pin.ifBlank { null })
        }) {
            Text("Join ride")
        }
    }
}
