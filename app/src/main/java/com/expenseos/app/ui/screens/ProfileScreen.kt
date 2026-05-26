package com.expenseos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.expenseos.app.ui.ExpenseOsUiState
import com.expenseos.app.ui.ExpenseOsViewModel
import com.expenseos.app.ui.theme.Coral
import com.expenseos.app.ui.theme.Gold
import com.expenseos.app.ui.theme.Mint
import com.expenseos.app.ui.theme.MutedInk
import com.expenseos.app.ui.theme.Paper
import com.expenseos.app.ui.theme.Pine

@Composable
fun ProfileScreen(
    uiState: ExpenseOsUiState,
    viewModel: ExpenseOsViewModel
) {
    var smsParsingToggle by remember { mutableStateOf(true) }
    var biometricLockToggle by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Paper
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Profile & Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Pine
                    )
                    Text(
                        text = "Manage your ledger sync and local app configurations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk
                    )
                }
            }

            // User Info Card
            item {
                UserInfoCard(name = "Zahid Khan", email = "zahid.khan@outlook.com")
            }

            // Pro Membership Premium Banner
            item {
                ProBanner(
                    isPremium = uiState.isPremiumUser,
                    onTogglePremium = viewModel::togglePremium
                )
            }

            // Section: Sync & Privacy
            item {
                Text(
                    text = "SYNC & ZERO-CLOUD SAFETY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Pine,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto local SMS parsing", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Pine)
                                Text(
                                    "Read raw bank wallet messages offline inside the phone.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk
                                )
                            }
                            Switch(
                                checked = smsParsingToggle,
                                onCheckedChange = { smsParsingToggle = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Pine,
                                    uncheckedThumbColor = MutedInk,
                                    uncheckedTrackColor = Paper
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Mint.copy(alpha = 0.2f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🔒 Your SMS messages never leave your phone. 100% private local architecture, zero network leaks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Pine,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Section: App Settings
            item {
                Text(
                    text = "APP CONFIGURATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Pine,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingsRow(label = "Primary Currency", value = "PKR (Rs.)")
                        SettingsRow(label = "Display Language", value = "English (Urdu coming soon)")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Biometric Ledger Lock", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Pine)
                                Text("Require fingerprint to view timeline and balances.", style = MaterialTheme.typography.bodySmall, color = MutedInk)
                            }
                            Switch(
                                checked = biometricLockToggle,
                                onCheckedChange = { biometricLockToggle = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Pine,
                                    uncheckedThumbColor = MutedInk,
                                    uncheckedTrackColor = Paper
                                )
                            )
                        }
                    }
                }
            }

            // Section: Data & Backup
            item {
                Text(
                    text = "DATA MANAGEMENT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Pine,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ActionRow(label = "Export ledger data (CSV)")
                        ActionRow(label = "Restore Ledger Backup")
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfoCard(name: String, email: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Pine)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Pine)
                Text(text = email, style = MaterialTheme.typography.bodySmall, color = MutedInk)
            }
        }
    }
}

@Composable
private fun ProBanner(isPremium: Boolean, onTogglePremium: () -> Unit) {
    val gradient = Brush.horizontalGradient(listOf(Pine, Coral, Gold))
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Pine),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTogglePremium() }
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPremium) "PRO ACTIVE" else "UPGRADE",
                        color = Gold,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Pro",
                    tint = Gold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = if (isPremium) "You are an Expense OS Pro Member!" else "Get Expense OS Pro",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = if (isPremium) {
                    "Unlimited local parsing, premium badges enabled, and +100 Coins reward claim activated!"
                } else {
                    "Unlock unlimited voice logs, auto-SMS sync background alerts, and cloud backup. Just Rs. 250/month."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Paper.copy(alpha = 0.9f)
            )

            if (!isPremium) {
                Text(
                    text = "Tap this card to start 7-day free trial →",
                    style = MaterialTheme.typography.labelLarge,
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Pine)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MutedInk)
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Edit settings", tint = MutedInk)
        }
    }
}

@Composable
private fun ActionRow(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Pine)
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Action", tint = MutedInk)
    }
}
