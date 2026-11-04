package com.example.panchayat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.ui.theme.CivicAmber
import com.example.ui.theme.LikePink
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.TwitterBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostSheet(
    currentUser: UserProfileEntity?,
    onDismiss: () -> Unit,
    onSubmitPost: (content: String, category: String, mediaRes: Int?) -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    var contentText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var selectedMediaRes by remember { mutableStateOf<Int?>(null) }

    val maxChars = 280
    val charsRemaining = maxChars - contentText.length
    val charRatio = (contentText.length.toFloat() / maxChars.toFloat()).coerceIn(0f, 1f)

    val categories = listOf("General", "Gram Sabha", "Civic Notice", "Development", "Agriculture")
    val suggestedTags = listOf("#GramSabha", "#PanchayatVikas", "#DigitalPanchayat", "#CleanVillage", "#MandiPrice")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet Header (Close, Progress Gauge, "Panch" button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("compose_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Character Progress Gauge
                    if (contentText.isNotEmpty()) {
                        val gaugeColor = when {
                            charsRemaining < 0 -> LikePink
                            charsRemaining < 20 -> CivicAmber
                            else -> TwitterBlue
                        }
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { charRatio },
                                modifier = Modifier.size(24.dp),
                                color = gaugeColor,
                                strokeWidth = 2.5.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            if (charsRemaining <= 20) {
                                Text(
                                    text = "$charsRemaining",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = gaugeColor
                                )
                            }
                        }
                    }

                    // Primary "Panch" Post Button
                    Button(
                        onClick = {
                            if (contentText.isNotBlank() && charsRemaining >= 0) {
                                onSubmitPost(contentText.trim(), selectedCategory, selectedMediaRes)
                            }
                        },
                        enabled = contentText.isNotBlank() && charsRemaining >= 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PanchayatSaffron,
                            contentColor = Color.White,
                            disabledContainerColor = PanchayatSaffron.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("compose_submit_button")
                    ) {
                        Text(
                            text = "Panch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Info Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PanchayatAvatar(
                    name = currentUser?.name ?: "Me",
                    modifier = Modifier.size(40.dp)
                )
                Column {
                    Text(
                        text = currentUser?.name ?: "Aman Verma",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "@${currentUser?.handle ?: "aman_verma"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Content Text Input
            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                placeholder = {
                    Text(
                        text = "What's happening in your Panchayat? Share updates, village issues or suggestions...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .testTag("compose_text_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            // Attached Media Preview if selected
            if (selectedMediaRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = selectedMediaRes!!),
                        contentDescription = "Attachment preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { selectedMediaRes = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove media",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Category Selector Chips
            Text(
                text = "Select Category / Notice Type:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PanchayatSaffron,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Hashtag Insert
            Text(
                text = "Insert Hashtags:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestedTags.forEach { tag ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clickable {
                                contentText = if (contentText.isBlank()) tag else "$contentText $tag"
                            }
                    ) {
                        Text(
                            text = tag,
                            color = TwitterBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Attach photo preset options
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = TwitterBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Attach photo preset:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = if (selectedMediaRes == R.drawable.panchayat_banner) PanchayatSaffron.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        selectedMediaRes = if (selectedMediaRes == R.drawable.panchayat_banner) null else R.drawable.panchayat_banner
                    }
                ) {
                    Text(
                        text = "Village Center",
                        fontSize = 11.sp,
                        color = if (selectedMediaRes == R.drawable.panchayat_banner) PanchayatSaffron else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = if (selectedMediaRes == R.drawable.gram_sabha_meeting) PanchayatSaffron.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        selectedMediaRes = if (selectedMediaRes == R.drawable.gram_sabha_meeting) null else R.drawable.gram_sabha_meeting
                    }
                ) {
                    Text(
                        text = "Gram Sabha",
                        fontSize = 11.sp,
                        color = if (selectedMediaRes == R.drawable.gram_sabha_meeting) PanchayatSaffron else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
