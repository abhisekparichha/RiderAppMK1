package com.ridermesh.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ridermesh.app.data.MeshMessage
import com.ridermesh.app.data.PeerInfo

@Composable
fun DebugScreen(
    peers: List<PeerInfo>,
    messages: List<MeshMessage>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text("Peer Graph")
                peers.forEach { peer ->
                    Text("• ${peer.displayName} (${peer.role}) hop=${peer.hopCount}")
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(messages) { message ->
                    Text("${message.type} from ${message.senderId}")
                }
            }
        }
    }
}
