package com.ridermesh.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateRideScreen(
    modifier: Modifier = Modifier,
    onCreate: (rideName: String, hostName: String, pin: String?) -> Unit
) {
    val (rideName, setRideName) = remember { mutableStateOf("") }
    val (hostName, setHostName) = remember { mutableStateOf("") }
    val (pin, setPin) = remember { mutableStateOf("") }

    Column(modifier = modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Create Ride")
        OutlinedTextField(value = rideName, onValueChange = setRideName, label = { Text("Ride name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = hostName, onValueChange = setHostName, label = { Text("Your name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pin, onValueChange = setPin, label = { Text("PIN (optional)") }, modifier = Modifier.fillMaxWidth())
        Button(enabled = rideName.isNotBlank() && hostName.isNotBlank(), onClick = { onCreate(rideName, hostName, pin.ifBlank { null }) }) {
            Text("Start hosting")
        }
    }
}
