package com.expenseos.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationDestination(
    val title: String,
    val icon: ImageVector
) {
    HOME("Home", Icons.Filled.Home),
    CALENDAR("Calendar", Icons.Filled.DateRange),
    UDHAAR("Udhaar", Icons.Filled.List),
    REWARDS("Rewards", Icons.Filled.Star),
    PROFILE("Profile", Icons.Filled.AccountCircle)
}
