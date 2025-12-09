package com.ridermesh.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onSaveName: (String) -> Unit,
    onToggleAutoJoin: (Boolean) -> Unit,
    onToggleVoice: (Boolean) -> Unit,
    onOpenLogs: () -> Unit
) {
    val (name, setName) = remember { mutableStateOf("") }
    val (autoJoin, setAutoJoin) = remember { mutableStateOf(false) }
    val (voiceEnabled, setVoiceEnabled) = remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Profile")
        OutlinedTextField(value = name, onValueChange = setName, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onSaveName(name) }) { Text("Save") }
        Text("Auto-join host rides")
        Switch(checked = autoJoin, onCheckedChange = {
            setAutoJoin(it)
            onToggleAutoJoin(it)
        })
        Text("Push-to-talk (Wi-Fi Direct)")
        Switch(checked = voiceEnabled, onCheckedChange = {
            setVoiceEnabled(it)
            onToggleVoice(it)
        })
        VerticalDivider()
        Button(onClick = onOpenLogs) { Text("Open debug logs") }
    }
}
