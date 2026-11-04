package com.example.panchayat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.panchayat.ui.NavigationItem
import com.example.ui.theme.LikePink
import com.example.ui.theme.PanchayatSaffron

@Composable
fun PanchayatBottomNav(
    currentScreen: NavigationItem,
    unreadNotifCount: Int,
    onNavigate: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            NavigationItem.HOME to Pair(Icons.Filled.Home, Icons.Outlined.Home),
            NavigationItem.EXPLORE to Pair(Icons.Filled.Search, Icons.Outlined.Search),
            NavigationItem.NOTIFICATIONS to Pair(Icons.Filled.Notifications, Icons.Outlined.Notifications),
            NavigationItem.PROFILE to Pair(Icons.Filled.Person, Icons.Outlined.Person)
        )

        items.forEach { (item, icons) ->
            val isSelected = currentScreen == item
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item) },
                icon = {
                    if (item == NavigationItem.NOTIFICATIONS && unreadNotifCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = LikePink,
                                    contentColor = Color.White
                                ) {
                                    Text(text = "$unreadNotifCount")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) icons.first else icons.second,
                                contentDescription = item.name
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) icons.first else icons.second,
                            contentDescription = item.name
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PanchayatSaffron,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.testTag("nav_item_${item.name.lowercase()}")
            )
        }
    }
}
