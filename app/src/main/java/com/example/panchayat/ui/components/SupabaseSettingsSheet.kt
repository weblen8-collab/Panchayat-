package com.example.panchayat.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.panchayat.data.remote.SupabaseClientManager
import com.example.panchayat.data.remote.SupabaseStatus
import com.example.ui.theme.CivicAmber
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.TwitterBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseSettingsSheet(
    supabaseStatus: SupabaseStatus,
    initialUrl: String,
    initialAnonKey: String,
    isSyncing: Boolean,
    syncMessage: String?,
    onSaveConfig: (url: String, anonKey: String) -> Unit,
    onTestConnection: ((Boolean, String) -> Unit) -> Unit,
    onSyncPosts: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    onOpenSupabaseFeed: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf(initialUrl) }
    var keyText by remember { mutableStateOf(initialAnonKey) }
    var isTesting by remember { mutableStateOf(false) }
    var testResultMsg by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf<Boolean?>(null) }
    var showSqlSchema by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = TwitterBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Supabase Database Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Status Banner
            Surface(
                color = when (supabaseStatus) {
                    is SupabaseStatus.Connected -> RepostGreen.copy(alpha = 0.12f)
                    is SupabaseStatus.Connecting -> TwitterBlue.copy(alpha = 0.12f)
                    is SupabaseStatus.Error -> CivicAmber.copy(alpha = 0.12f)
                    is SupabaseStatus.LocalOnly -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when (supabaseStatus) {
                        is SupabaseStatus.Connected -> RepostGreen.copy(alpha = 0.4f)
                        is SupabaseStatus.Connecting -> TwitterBlue.copy(alpha = 0.4f)
                        is SupabaseStatus.Error -> CivicAmber.copy(alpha = 0.4f)
                        is SupabaseStatus.LocalOnly -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when (supabaseStatus) {
                            is SupabaseStatus.Connected -> Icons.Default.CloudDone
                            is SupabaseStatus.Connecting -> Icons.Default.Refresh
                            is SupabaseStatus.Error -> Icons.Default.Warning
                            is SupabaseStatus.LocalOnly -> Icons.Default.CloudQueue
                        },
                        contentDescription = null,
                        tint = when (supabaseStatus) {
                            is SupabaseStatus.Connected -> RepostGreen
                            is SupabaseStatus.Connecting -> TwitterBlue
                            is SupabaseStatus.Error -> CivicAmber
                            is SupabaseStatus.LocalOnly -> PanchayatSaffron
                        }
                    )
                    Column {
                        Text(
                            text = when (supabaseStatus) {
                                is SupabaseStatus.Connected -> "Supabase Live Connected"
                                is SupabaseStatus.Connecting -> "Connecting to Supabase..."
                                is SupabaseStatus.Error -> "Local SQLite Active (Supabase Offline)"
                                is SupabaseStatus.LocalOnly -> "Local SQLite Database Active"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = when (supabaseStatus) {
                                is SupabaseStatus.Connected -> "Posts & likes are syncing directly with your Supabase cloud tables."
                                is SupabaseStatus.Connecting -> "Verifying connection with PostgREST endpoint..."
                                is SupabaseStatus.Error -> (supabaseStatus as SupabaseStatus.Error).message
                                is SupabaseStatus.LocalOnly -> "Full offline support enabled. Enter your Supabase credentials below to sync with cloud."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Supabase URL Input
            Text(
                text = "Supabase Project URL",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                placeholder = { Text("https://your-project.supabase.co") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supabase_url_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TwitterBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            if (urlText.trim().startsWith("sb_", ignoreCase = true) || urlText.trim().startsWith("ey", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notice: This looks like an API key (e.g. sb_pub...). The Project URL should be formatted as https://<project-ref>.supabase.co",
                    style = MaterialTheme.typography.bodySmall,
                    color = CivicAmber
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Supabase Anon Key Input
            Text(
                text = "Supabase Anon / Public API Key",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = keyText,
                onValueChange = { keyText = it },
                placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supabase_key_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TwitterBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Save & Test)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val trimmedU = urlText.trim()
                        val trimmedK = keyText.trim()
                        val (finalUrl, finalKey) = if ((trimmedU.startsWith("sb_", ignoreCase = true) || trimmedU.startsWith("ey", ignoreCase = true)) &&
                            (trimmedK.startsWith("http", ignoreCase = true) || trimmedK.contains(".supabase.co", ignoreCase = true))) {
                            Pair(trimmedK, trimmedU)
                        } else {
                            Pair(trimmedU, trimmedK)
                        }
                        urlText = finalUrl
                        keyText = finalKey
                        onSaveConfig(finalUrl, finalKey)
                        Toast.makeText(context, "Supabase configuration updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PanchayatSaffron),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("supabase_save_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Config", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        isTesting = true
                        testResultMsg = null
                        onSaveConfig(urlText.trim(), keyText.trim())
                        onTestConnection { success, msg ->
                            isTesting = false
                            isTestSuccess = success
                            testResultMsg = msg
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("supabase_test_button")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = TwitterBlue
                        )
                    } else {
                        Text("Test Connection", fontWeight = FontWeight.Bold, color = TwitterBlue)
                    }
                }
            }

            // Test Feedback Message
            if (testResultMsg != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = if (isTestSuccess == true) RepostGreen.copy(alpha = 0.15f) else CivicAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = testResultMsg ?: "",
                        color = if (isTestSuccess == true) RepostGreen else CivicAmber,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sync Button
            Button(
                onClick = onSyncPosts,
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supabase_sync_posts_button")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Syncing from Supabase...", color = MaterialTheme.colorScheme.onSurface)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Remote Posts From Supabase", color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (syncMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = syncMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onOpenSupabaseFeed != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenSupabaseFeed,
                    colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_view_feed_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse Supabase Posts Feed", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // SQL Schema Helper Accordion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSqlSchema = !showSqlSchema }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showSqlSchema) "Hide Supabase SQL Schema" else "View Supabase SQL Schema (For Setup)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TwitterBlue
                    )
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy SQL",
                    tint = TwitterBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (showSqlSchema) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = SupabaseClientManager.SQL_SCHEMA,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF58A6FF),
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Supabase SQL Schema", SupabaseClientManager.SQL_SCHEMA)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "SQL schema copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Copy SQL to Clipboard")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
