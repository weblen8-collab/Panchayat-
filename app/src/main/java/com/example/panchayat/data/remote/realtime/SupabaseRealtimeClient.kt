package com.example.panchayat.data.remote.realtime

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Realtime client for Supabase WebSocket connection.
 * Connects to the Supabase Realtime endpoint:
 * `wss://<project-ref>.supabase.co/realtime/v1/websocket?apikey=<anonKey>&vsn=1.0.0`
 *
 * Enables real-time database changes (Postgres CDC subscriptions),
 * broadcast messaging, and presence updates across connected clients.
 */
class SupabaseRealtimeClient(
    val supabaseUrl: String,
    val supabaseAnonKey: String,
    private val okHttpClient: OkHttpClient = OkHttpClient()
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val refCounter = AtomicLong(1)
    private val channels = ConcurrentHashMap<String, RealtimeChannel>()

    private val _status = MutableStateFlow(RealtimeStatus.DISCONNECTED)
    val status: StateFlow<RealtimeStatus> = _status.asStateFlow()

    private val _events = MutableSharedFlow<RealtimeMessage>(replay = 10)
    val events: SharedFlow<RealtimeMessage> = _events.asSharedFlow()

    @Volatile
    private var webSocket: WebSocket? = null

    @Volatile
    private var heartbeatJob: Job? = null

    /**
     * Constructs the official Supabase Realtime WebSocket URL.
     */
    val realtimeUrl: String by lazy {
        val cleanUrl = supabaseUrl.trim().removeSuffix("/")
        val hostWithPort = when {
            cleanUrl.startsWith("https://", ignoreCase = true) -> cleanUrl.removePrefix("https://")
            cleanUrl.startsWith("http://", ignoreCase = true) -> cleanUrl.removePrefix("http://")
            else -> cleanUrl
        }
        val scheme = if (cleanUrl.startsWith("http://", ignoreCase = true)) "ws" else "wss"
        "$scheme://$hostWithPort/realtime/v1/websocket?apikey=$supabaseAnonKey&vsn=1.0.0"
    }

    /**
     * Connects to the Supabase Realtime WebSocket endpoint.
     */
    @Synchronized
    fun connect(): SupabaseRealtimeClient {
        if (_status.value == RealtimeStatus.CONNECTED || _status.value == RealtimeStatus.CONNECTING) {
            return this
        }

        _status.value = RealtimeStatus.CONNECTING
        logD("SupabaseRealtime", "Connecting to Supabase Realtime at $realtimeUrl")

        try {
            val request = Request.Builder()
                .url(realtimeUrl)
                .addHeader("apikey", supabaseAnonKey)
                .build()

            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    logD("SupabaseRealtime", "Connected to Supabase Realtime!")
                    _status.value = RealtimeStatus.CONNECTED
                    startHeartbeat()

                    // Resubscribe existing active channels
                    for (channel in channels.values) {
                        if (channel.isSubscribed) {
                            sendJoin(channel.topic)
                        }
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleIncomingMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    logD("SupabaseRealtime", "WebSocket closing: $code / $reason")
                    _status.value = RealtimeStatus.DISCONNECTED
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    logD("SupabaseRealtime", "WebSocket closed: $code / $reason")
                    _status.value = RealtimeStatus.DISCONNECTED
                    stopHeartbeat()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    logW("SupabaseRealtime", "Realtime WebSocket connection failure: ${t.message}")
                    _status.value = RealtimeStatus.ERROR
                    stopHeartbeat()
                }
            })
        } catch (e: Exception) {
            logE("SupabaseRealtime", "Failed to initiate WebSocket connection: ${e.message}", e)
            _status.value = RealtimeStatus.ERROR
        }

        return this
    }

    /**
     * Disconnects from the Realtime WebSocket and cancels heartbeat timers.
     */
    @Synchronized
    fun disconnect() {
        stopHeartbeat()
        try {
            webSocket?.close(1000, "Client disconnect")
        } catch (e: Exception) {
            Log.w("SupabaseRealtime", "Error closing WebSocket: ${e.message}")
        }
        webSocket = null
        _status.value = RealtimeStatus.DISCONNECTED
    }

    /**
     * Gets or creates a [RealtimeChannel] for a specific topic.
     */
    fun channel(topic: String): RealtimeChannel {
        return channels.computeIfAbsent(topic) {
            RealtimeChannel(topic, this)
        }
    }

    /**
     * Subscribes to changes on a specific public database table (e.g. "posts").
     * Uses the Supabase Postgres Changes convention `realtime:public:<tableName>`.
     */
    fun subscribeToTable(
        tableName: String,
        schema: String = "public",
        event: String = "*",
        callback: (RealtimeMessage) -> Unit
    ): RealtimeChannel {
        val topic = "realtime:$schema:$tableName"
        val ch = channel(topic)
        ch.on(event, callback)
        ch.subscribe()
        return ch
    }

    /**
     * Subscribes directly to real-time events for the 'posts' table.
     */
    fun subscribeToPosts(
        onPostEvent: (table: String, eventType: String, payloadJson: String) -> Unit
    ): RealtimeChannel {
        return subscribeToTable("posts") { message ->
            onPostEvent("posts", message.event, message.payload)
        }
    }

    internal fun sendJoin(topic: String) {
        val ref = refCounter.getAndIncrement().toString()
        val joinPayload = """{"topic":"$topic","event":"phx_join","payload":{},"ref":"$ref"}"""
        webSocket?.send(joinPayload)
    }

    internal fun sendLeave(topic: String) {
        val ref = refCounter.getAndIncrement().toString()
        val leavePayload = """{"topic":"$topic","event":"phx_leave","payload":{},"ref":"$ref"}"""
        webSocket?.send(leavePayload)
    }

    internal fun sendMessage(topic: String, event: String, payloadJson: String): Boolean {
        val ref = refCounter.getAndIncrement().toString()
        val formattedPayload = if (payloadJson.trim().startsWith("{") || payloadJson.trim().startsWith("[")) {
            payloadJson.trim()
        } else {
            "\"$payloadJson\""
        }
        val msg = """{"topic":"$topic","event":"$event","payload":$formattedPayload,"ref":"$ref"}"""
        val sent = webSocket?.send(msg) ?: false

        // Broadcast locally to registered channels as well
        val localMessage = RealtimeMessage(topic, event, payloadJson, ref)
        channels[topic]?.dispatchMessage(localMessage)
        scope.launch { _events.emit(localMessage) }
        return sent
    }

    internal fun removeChannel(topic: String) {
        channels.remove(topic)
    }

    /**
     * Dispatches a broadcast event locally across all subscribed channels.
     * Useful for immediate optimistic updates and mock/prototype mode.
     */
    fun dispatchLocalEvent(topic: String, event: String, payloadJson: String) {
        val msg = RealtimeMessage(topic, event, payloadJson, refCounter.getAndIncrement().toString())
        channels[topic]?.dispatchMessage(msg)
        scope.launch { _events.emit(msg) }
    }

    private fun handleIncomingMessage(text: String) {
        try {
            val topic = extractJsonField(text, "topic") ?: ""
            val event = extractJsonField(text, "event") ?: ""
            val ref = extractJsonField(text, "ref")
            val payload = extractJsonPayload(text)

            val message = RealtimeMessage(topic, event, payload, ref)
            logD("SupabaseRealtime", "Received event: $event on topic $topic")

            channels[topic]?.dispatchMessage(message)
            scope.launch { _events.emit(message) }
        } catch (e: Exception) {
            logE("SupabaseRealtime", "Failed to parse realtime frame: $text", e)
        }
    }

    private fun extractJsonField(json: String, field: String): String? {
        val regex = Regex("""\"$field\"\s*:\s*\"([^\"]*)\"""")
        return regex.find(json)?.groupValues?.getOrNull(1)
    }

    private fun extractJsonPayload(json: String): String {
        val pattern = Regex("""\"payload\"\s*:\s*(\{.*?\}|\[.*?\]|\"[^\"]*\"|[0-9]+|true|false|null)""", RegexOption.DOT_MATCHES_ALL)
        return pattern.find(json)?.groupValues?.getOrNull(1) ?: "{}"
    }

    private fun logD(tag: String, msg: String) {
        try { Log.d(tag, msg) } catch (_: Throwable) {}
    }

    private fun logW(tag: String, msg: String) {
        try { Log.w(tag, msg) } catch (_: Throwable) {}
    }

    private fun logE(tag: String, msg: String, t: Throwable? = null) {
        try { Log.e(tag, msg, t) } catch (_: Throwable) {}
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(30_000) // 30-second Phoenix heartbeat
                if (_status.value == RealtimeStatus.CONNECTED) {
                    val ref = refCounter.getAndIncrement().toString()
                    val ping = """{"topic":"phoenix","event":"heartbeat","payload":{},"ref":"$ref"}"""
                    webSocket?.send(ping)
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }
}
