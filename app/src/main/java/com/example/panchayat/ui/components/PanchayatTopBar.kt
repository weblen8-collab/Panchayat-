package com.example.panchayat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.panchayat.data.remote.SupabaseStatus
import com.example.panchayat.ui.FeedTab
import com.example.panchayat.ui.NavigationItem
import com.example.ui.theme.CivicAmber
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.TwitterBlue

@Composable
fun PanchayatTopBar(
    currentUser: UserProfileEntity?,
    supabaseStatus: SupabaseStatus,
    currentScreen: NavigationItem,
    feedTab: FeedTab,
    onFeedTabSelected: (FeedTab) -> Unit,
    onAvatarClick: () -> Unit,
    onSupabaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: User Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAvatarClick)
                    .testTag("top_bar_avatar")
            ) {
                PanchayatAvatar(
                    name = currentUser?.name ?: "User",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Center: App Logo + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.testTag("app_logo_title")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_panchayat_logo),
                    contentDescription = "Panchayat Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Panchayat",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Right: Supabase Status Chip
            SupabaseStatusBadge(
                status = supabaseStatus,
                onClick = onSupabaseClick
            )
        }

        // Feed Tabs (Only visible on Home screen)
        if (currentScreen == NavigationItem.HOME) {
            val tabs = listOf(
                FeedTab.FOR_YOU to "For You",
                FeedTab.FOLLOWING to "Following",
                FeedTab.CIVIC_PANCHAYAT to "Gram Sabha",
                FeedTab.SUPABASE_FEED to "Supabase"
            )
            val selectedIndex = tabs.indexOfFirst { it.first == feedTab }.coerceAtLeast(0)

            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = { tabPositions ->
                    if (selectedIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                            color = PanchayatSaffron,
                            height = 3.dp
                        )
                    }
                },
                divider = {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            ) {
                tabs.forEachIndexed { index, (tab, title) ->
                    val isSelected = selectedIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { onFeedTabSelected(tab) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("feed_tab_$index")
                    )
                }
            }
        } else {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun SupabaseStatusBadge(
    status: SupabaseStatus,
    onClick: () -> Unit
) {
    val (label, icon, tintColor, dotColor) = when (status) {
        is SupabaseStatus.Connected -> Quadruple(
            "Supabase",
            Icons.Default.CloudDone,
            RepostGreen,
            RepostGreen
        )
        is SupabaseStatus.Connecting -> Quadruple(
            "Syncing...",
            Icons.Default.CloudQueue,
            TwitterBlue,
            TwitterBlue
        )
        is SupabaseStatus.Error -> Quadruple(
            "Local DB",
            Icons.Default.Warning,
            CivicAmber,
            CivicAmber
        )
        is SupabaseStatus.LocalOnly -> Quadruple(
            "Supabase DB",
            Icons.Default.CloudQueue,
            MaterialTheme.colorScheme.onSurfaceVariant,
            PanchayatSaffron
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("supabase_status_badge")
    ) {
        // Status indicator dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Icon(
            imageVector = icon,
            contentDescription = "Database Status",
            tint = tintColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
