package com.ridermesh.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ridermesh.app.data.MeshMessage
import com.ridermesh.app.data.PeerInfo
import com.ridermesh.app.data.RideSession

@Composable
fun RideDashboardScreen(
    modifier: Modifier = Modifier,
    session: RideSession,
    peers: List<PeerInfo>,
    messages: List<MeshMessage>,
    onSendMessage: (String) -> Unit,
    onLeave: () -> Unit,
    onOpenDebug: () -> Unit
) {
    val (message, setMessage) = remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = session.rideName, style = MaterialTheme.typography.headlineSmall)
        Text("Ride code: ${session.joinCode}")
        Spacer(modifier = Modifier.height(12.dp))
        PeerList(peers = peers)
        Spacer(modifier = Modifier.height(12.dp))
        MessagesList(messages = messages, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = message,
            onValueChange = setMessage,
            label = { Text("Broadcast message") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Button(modifier = Modifier.weight(1f), enabled = message.isNotBlank(), onClick = {
                onSendMessage(message)
                setMessage("")
            }) {
                Text("Send")
            }
            Button(modifier = Modifier.weight(1f), onClick = onOpenDebug) {
                Text("Debug")
            }
        }
        Button(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), onClick = onLeave) {
            Text("Leave ride")
        }
    }
}

@Composable
private fun PeerList(peers: List<PeerInfo>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Riders (${peers.size})", style = MaterialTheme.typography.titleMedium)
            peers.forEach { peer ->
                Text("${peer.displayName} · hop ${peer.hopCount} · last ${peer.lastSeen}")
            }
        }
    }
}

@Composable
private fun MessagesList(messages: List<MeshMessage>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { message ->
                Column {
                    Text(message.senderId, style = MaterialTheme.typography.labelMedium)
                    Text(message.payload["body"] ?: "<encrypted>")
                }
            }
        }
    }
}
