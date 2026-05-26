package com.expenseos.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationDestination(
    val title: String,
    val icon: ImageVector
) {
    HOME("Home", Icons.Filled.Home),
    BUDGETS("Budgets", Icons.Filled.Info),
    GOALS("Goals", Icons.Filled.Star), // Star used for goals
    CALENDAR("Calendar", Icons.Filled.DateRange),
    UDHAAR("Udhaar", Icons.Filled.List),
    REWARDS("Rewards", Icons.Filled.Star),
    PROFILE("Profile", Icons.Filled.AccountCircle),
    MONTHLY_REPORT("Report", Icons.Filled.Info), // Not in bottom bar
    BILL_SPLIT("Split Bill", Icons.Filled.Info) // Not in bottom bar
}
