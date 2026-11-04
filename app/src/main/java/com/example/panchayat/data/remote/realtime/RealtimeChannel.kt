package com.example.panchayat.data.remote.realtime

import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Represents a subscribed Realtime channel (topic) on Supabase.
 * Allows client code to listen to broadcast, presence, and postgres change events.
 */
class RealtimeChannel(
    val topic: String,
    private val client: SupabaseRealtimeClient
) {
    private val listeners = CopyOnWriteArrayList<Pair<String, (RealtimeMessage) -> Unit>>()

    @Volatile
    var isSubscribed: Boolean = false
        internal set

    /**
     * Registers a callback listener for a specific event type on this channel.
     * Use event "*" to listen to all events on this channel.
     */
    fun on(event: String, callback: (RealtimeMessage) -> Unit): RealtimeChannel {
        listeners.add(Pair(event, callback))
        return this
    }

    /**
     * Subscribes this channel to the Supabase Realtime server.
     */
    fun subscribe(): RealtimeChannel {
        if (!isSubscribed) {
            isSubscribed = true
            client.sendJoin(topic)
            try { Log.d("RealtimeChannel", "Subscribed to channel: $topic") } catch (_: Throwable) {}
        }
        return this
    }

    /**
     * Unsubscribes from this channel and removes listeners.
     */
    fun unsubscribe() {
        if (isSubscribed) {
            isSubscribed = false
            client.sendLeave(topic)
            try { Log.d("RealtimeChannel", "Unsubscribed from channel: $topic") } catch (_: Throwable) {}
        }
        listeners.clear()
        client.removeChannel(topic)
    }

    /**
     * Broadcasts an event to this channel.
     */
    fun send(event: String, payloadJson: String = "{}"): Boolean {
        return client.sendMessage(topic, event, payloadJson)
    }

    internal fun dispatchMessage(message: RealtimeMessage) {
        for ((eventFilter, callback) in listeners) {
            if (eventFilter == "*" || eventFilter.equals(message.event, ignoreCase = true)) {
                try {
                    callback(message)
                } catch (e: Exception) {
                    try { Log.e("RealtimeChannel", "Error executing listener on channel $topic: ${e.message}", e) } catch (_: Throwable) {}
                }
            }
        }
    }
}
