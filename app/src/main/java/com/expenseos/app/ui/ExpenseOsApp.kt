package com.expenseos.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenseos.app.ui.navigation.NavigationDestination
import com.expenseos.app.ui.screens.CalendarScreen
import com.expenseos.app.ui.screens.HomeScreen
import com.expenseos.app.ui.screens.ProfileScreen
import com.expenseos.app.ui.screens.RewardsScreen
import com.expenseos.app.ui.screens.UdhaarScreen
import com.expenseos.app.ui.theme.Gold
import com.expenseos.app.ui.theme.Mint
import com.expenseos.app.ui.theme.MutedInk
import com.expenseos.app.ui.theme.Paper
import com.expenseos.app.ui.theme.Pine
import kotlinx.coroutines.delay

@Composable
fun ExpenseOsApp(viewModel: ExpenseOsViewModel = viewModel()) {
    var currentDestination by remember { mutableStateOf(NavigationDestination.HOME) }
    val uiState by viewModel.uiState.collectAsState()

    // Auto-dismiss notification after 3 seconds
    uiState.lastNotificationMessage?.let { _ ->
        LaunchedEffect(uiState.lastNotificationMessage) {
            delay(3500)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationDestination.entries.forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                dest.icon, 
                                contentDescription = dest.title,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                dest.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Pine,
                            selectedTextColor = Pine,
                            indicatorColor = Mint.copy(alpha = 0.4f),
                            unselectedIconColor = MutedInk,
                            unselectedTextColor = MutedInk
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Paper)
        ) {
            // Screen Routing
            when (currentDestination) {
                NavigationDestination.HOME -> HomeScreen(uiState = uiState, viewModel = viewModel)
                NavigationDestination.CALENDAR -> CalendarScreen(uiState = uiState, viewModel = viewModel)
                NavigationDestination.UDHAAR -> UdhaarScreen(uiState = uiState, viewModel = viewModel)
                NavigationDestination.REWARDS -> RewardsScreen(uiState = uiState, viewModel = viewModel)
                NavigationDestination.PROFILE -> ProfileScreen(uiState = uiState, viewModel = viewModel)
            }

            // High-Fidelity Animated Notification HUD at the top
            AnimatedVisibility(
                visible = uiState.lastNotificationMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                uiState.lastNotificationMessage?.let { message ->
                    NotificationHud(message = message, onClick = viewModel::clearNotification)
                }
            }
        }
    }
}

@Composable
private fun NotificationHud(message: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Pine),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(Pine, Color(0xFF1F4A40))))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info, 
                contentDescription = "Alert", 
                tint = Gold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "DISMISS",
                color = Mint,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
