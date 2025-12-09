package com.ridermesh.app.data

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class MessageStore(private val capacity: Int = 256) {
    private val seenIds = ConcurrentHashMap.newKeySet<String>()
    private val queue = ConcurrentLinkedQueue<String>()

    fun shouldProcess(messageId: String): Boolean {
        if (seenIds.contains(messageId)) return false
        seenIds += messageId
        queue += messageId
        trim()
        return true
    }

    private fun trim() {
        while (queue.size > capacity) {
            val removed = queue.poll() ?: break
            seenIds.remove(removed)
        }
    }
}
