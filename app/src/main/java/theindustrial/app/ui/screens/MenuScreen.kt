package theindustrial.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theindustrial.app.R
import theindustrial.app.ui.theme.ThemeManager

data class MenuIconItem(val title: String, val icon: Any)

@Composable
fun MenuScreen(onMenuItemClick: (String) -> Unit) {
    val userName = ThemeManager.userName.value ?: "User"

    // Row 1: Primary Platform Items (Using Outlined icons for Lucide look)
    val row1 = listOf(
        MenuIconItem("Ask Kivaa", Icons.AutoMirrored.Outlined.Message),
        MenuIconItem("My Orders", Icons.Outlined.Inventory2),
        MenuIconItem("Subscription", R.drawable.cart),
        MenuIconItem("Exclusive", R.drawable.exclusive)
    )

    // Row 2: Content Engagement
    val row2 = listOf(
        MenuIconItem("Bookmarks", Icons.Outlined.BookmarkBorder),
        MenuIconItem("Liked", Icons.Outlined.ThumbUpOffAlt),
        MenuIconItem("History", Icons.Outlined.History),
        MenuIconItem("My Comments", Icons.Outlined.ChatBubbleOutline)
    )

    // Row 3: Account & Support
    val row3 = listOf(
        MenuIconItem("My Reviews", Icons.Outlined.StarOutline),
        MenuIconItem("Addresses", Icons.Outlined.LocationOn),
        MenuIconItem("Preferences", Icons.Outlined.NotificationsNone),
        MenuIconItem("Support", Icons.Outlined.SupportAgent)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- Namaste & Account Settings Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Namaste,",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // "Account Settings" button (Lucide Settings wheel icon equivalent)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onMenuItemClick("Account Settings") }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Account Settings",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Account Settings",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // --- GRID BODY ---
        MenuRow(row1, onMenuItemClick)
        Spacer(modifier = Modifier.height(24.dp))
        MenuRow(row2, onMenuItemClick)
        Spacer(modifier = Modifier.height(24.dp))
        MenuRow(row3, onMenuItemClick)

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun MenuRow(items: List<MenuIconItem>, onItemClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onItemClick(item.title) }
            ) {
                if (item.icon is ImageVector) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (item.icon is Int) {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
