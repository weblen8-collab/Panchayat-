package com.example.panchayat.data.remote.realtime

/**
 * Connection states for the Supabase Realtime WebSocket client.
 */
enum class RealtimeStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Represents a Supabase Realtime message frame exchanged over WebSocket.
 * Conforms to the Phoenix channels wire format used by Supabase Realtime.
 */
data class RealtimeMessage(
    val topic: String,
    val event: String,
    val payload: String = "{}",
    val ref: String? = null
)

/**
 * Event type representing change notifications on a Supabase database table.
 */
data class PostgresChangeEvent(
    val schema: String = "public",
    val table: String,
    val eventType: String, // INSERT, UPDATE, DELETE, or *
    val record: Map<String, Any?> = emptyMap(),
    val oldRecord: Map<String, Any?> = emptyMap(),
    val commitTimestamp: Long = System.currentTimeMillis()
)
