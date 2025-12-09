package com.ridermesh.app

import com.ridermesh.app.data.MeshMessage
import com.ridermesh.app.data.MessageStore
import com.ridermesh.app.data.MessageType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MeshMessageTest {
    @Test
    fun serializationRoundTrip() {
        val message = MeshMessage(
            rideId = "ride",
            senderId = "alice",
            type = MessageType.TEXT_MESSAGE,
            payload = mapOf("body" to "hello")
        )
        val json = Json.encodeToString(message)
        val decoded = Json.decodeFromString<MeshMessage>(json)
        assertEquals(message.payload["body"], decoded.payload["body"])
    }

    @Test
    fun messageStoreDropsDuplicates() {
        val store = MessageStore(capacity = 2)
        assertTrue(store.shouldProcess("a"))
        assertFalse(store.shouldProcess("a"))
        assertTrue(store.shouldProcess("b"))
        assertTrue(store.shouldProcess("c"))
        // "a" should be evicted because of capacity.
        assertTrue(store.shouldProcess("a"))
    }
}
