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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenseos.app.ui.navigation.NavigationDestination
import com.expenseos.app.ui.screens.BudgetScreen
import com.expenseos.app.ui.screens.BillSplitScreen
import com.expenseos.app.ui.screens.CalendarScreen
import com.expenseos.app.ui.screens.GoalsScreen
import com.expenseos.app.ui.screens.MonthlyReportScreen
import com.expenseos.app.ui.screens.HomeScreen
import com.expenseos.app.ui.screens.ProfileScreen
import com.expenseos.app.ui.screens.RewardsScreen
import com.expenseos.app.ui.screens.UdhaarScreen
import com.expenseos.app.ui.theme.Gold
import com.expenseos.app.ui.theme.Mint
import com.expenseos.app.ui.theme.CoralPink
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
                val tabsToShow = NavigationDestination.entries.filter { 
                    it != NavigationDestination.MONTHLY_REPORT && it != NavigationDestination.BILL_SPLIT 
                }
                tabsToShow.forEach { dest ->
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                                fontSize = 10.sp
                            ) 
                        },
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Pine,
                            selectedTextColor = Pine,
                            indicatorColor = Mint,
                            unselectedIconColor = MutedInk,
                            unselectedTextColor = MutedInk
                        ),
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
        ) {
            // Vibrant Colorful Corners (Top End)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 100.dp, y = (-100).dp)
                    .size(300.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Mint.copy(alpha = 0.4f), Color.Transparent),
                            radius = 400f
                        ),
                        shape = CircleShape
                    )
                    .blur(50.dp)
            )

            // Vibrant Colorful Corners (Bottom Start)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-100).dp, y = 100.dp)
                    .size(350.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CoralPink.copy(alpha = 0.3f), Color.Transparent),
                            radius = 450f
                        ),
                        shape = CircleShape
                    )
                    .blur(60.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Screen Routing
                when (currentDestination) {
                    NavigationDestination.HOME -> HomeScreen(uiState = uiState, viewModel = viewModel)
                    NavigationDestination.BUDGETS -> BudgetScreen(uiState = uiState, viewModel = viewModel)
                    NavigationDestination.GOALS -> GoalsScreen(uiState = uiState, viewModel = viewModel)
                    NavigationDestination.CALENDAR -> CalendarScreen(uiState = uiState, viewModel = viewModel)
                    NavigationDestination.UDHAAR -> UdhaarScreen(uiState = uiState, viewModel = viewModel, onNavigate = { currentDestination = it })
                    NavigationDestination.REWARDS -> RewardsScreen(uiState = uiState, viewModel = viewModel)
                    NavigationDestination.PROFILE -> ProfileScreen(uiState = uiState, viewModel = viewModel)
                    NavigationDestination.MONTHLY_REPORT -> MonthlyReportScreen(uiState = uiState, viewModel = viewModel)
                    NavigationDestination.BILL_SPLIT -> BillSplitScreen(
                        uiState = uiState, 
                        viewModel = viewModel, 
                        onBack = { currentDestination = NavigationDestination.HOME }
                    )
                }
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
