package com.expenseos.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.expenseos.app.features.rewards.RewardEvent
import com.expenseos.app.ui.ExpenseOsUiState
import com.expenseos.app.ui.ExpenseOsViewModel
import com.expenseos.app.ui.theme.Coral
import com.expenseos.app.ui.theme.Gold
import com.expenseos.app.ui.theme.Mint
import com.expenseos.app.ui.theme.MutedInk
import com.expenseos.app.ui.theme.Paper
import com.expenseos.app.ui.theme.Pine
import java.time.format.DateTimeFormatter

@Composable
fun RewardsScreen(
    uiState: ExpenseOsUiState,
    viewModel: ExpenseOsViewModel
) {
    val totalCoins = uiState.rewardProfile.totalCoins
    val activeBadges = uiState.rewardProfile.badges

    // Calculate progression details
    val (rankName, nextRankName, progressToNext) = when {
        totalCoins < 300 -> Triple("Bronze Saver", "Silver Saver", totalCoins / 300f)
        totalCoins < 1000 -> Triple("Silver Saver", "Gold Capitalist", (totalCoins - 300) / 700f)
        else -> Triple("Gold Capitalist", "Max Rank", 1.0f)
    }

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
                        text = "Gamified Rewards",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Pine
                    )
                    Text(
                        text = "Earn Expense Coins & unlock financial milestones.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk
                    )
                }
            }

            // Coin score dashboard
            item {
                CoinsDashboard(totalCoins = totalCoins, rankName = rankName)
            }

            // Tier Progress
            item {
                TierProgress(
                    rankName = rankName,
                    nextRankName = nextRankName,
                    progress = progressToNext,
                    totalCoins = totalCoins
                )
            }

            // Badges Grid title
            item {
                Text(
                    text = "UNLOCKED MILESTONES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Pine,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Badges Layout
            item {
                BadgesLayout(activeBadges = activeBadges)
            }

            // Rewards Feed title
            item {
                Text(
                    text = "POINTS LEDGER FEED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Pine,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // Points events list
            if (uiState.recentRewardEvents.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("No recent coin earnings yet. Log transactions or settle Udhaar to start!", color = MutedInk)
                        }
                    }
                }
            } else {
                items(uiState.recentRewardEvents, key = { it.id }) { event ->
                    RewardEventRow(event = event)
                }
            }
        }
    }
}

@Composable
private fun CoinsDashboard(totalCoins: Int, rankName: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Pine),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Pine, Color(0xFF1B493E))))
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "EXPENSE COINS BALANCE",
                    color = Mint.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = totalCoins.toString(),
                    color = Gold,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Rank: $rankName",
                    color = Mint,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Glowing coin icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.15f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Gold)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Coin",
                        tint = Pine,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TierProgress(
    rankName: String,
    nextRankName: String,
    progress: Float,
    totalCoins: Int
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rankName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Pine
                )
                if (nextRankName != "Max Rank") {
                    Text(
                        text = "Next: $nextRankName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                } else {
                    Text(
                        text = "Reached Max Level!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Pine,
                trackColor = Mint.copy(alpha = 0.3f)
            )

            if (nextRankName != "Max Rank") {
                val coinsNeeded = when (nextRankName) {
                    "Silver Saver" -> 300 - totalCoins
                    else -> 1000 - totalCoins
                }
                Text(
                    text = "Collect $coinsNeeded more coins to rank up.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk
                )
            }
        }
    }
}

@Composable
private fun BadgesLayout(activeBadges: List<String>) {
    val allBadges = listOf(
        BadgeDef("First Step", "First manual log confirmed", Brush.horizontalGradient(listOf(Mint, Pine))),
        BadgeDef("Udhaar Master", "Settled 2+ active debts", Brush.horizontalGradient(listOf(Coral, Gold))),
        BadgeDef("The Saver", "Earned 1000+ Expense Coins", Brush.horizontalGradient(listOf(Gold, Pine))),
        BadgeDef("Pro Club", "Subscribed to Premium", Brush.horizontalGradient(listOf(Pine, Coral)))
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Render 2 rows of 2 cards
        for (i in 0 until 4 step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BadgeCard(def = allBadges[i], isUnlocked = activeBadges.contains(allBadges[i].name), modifier = Modifier.weight(1f))
                BadgeCard(def = allBadges[i+1], isUnlocked = activeBadges.contains(allBadges[i+1].name), modifier = Modifier.weight(1f))
            }
        }
    }
}

data class BadgeDef(
    val name: String,
    val description: String,
    val brush: Brush
)

@Composable
private fun BadgeCard(
    def: BadgeDef,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 1.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) Color.Transparent else Paper)
                    .then(if (isUnlocked) Modifier.background(def.brush) else Modifier)
            ) {
                if (isUnlocked) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Badge",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MutedInk,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = def.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Pine else MutedInk,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = def.description,
                style = MaterialTheme.typography.labelSmall,
                color = MutedInk,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2
            )
        }
    }
}

@Composable
private fun RewardEventRow(event: RewardEvent) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.15f))
            ) {
                Text("🪙", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Pine
                )
                Text(
                    text = event.timestamp.format(DateTimeFormatter.ofPattern("MMM d, h:mm a")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
            }
            Text(
                text = "+${event.points}",
                color = Pine,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
