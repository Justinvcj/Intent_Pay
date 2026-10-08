package com.intentpay.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.ui.components.GlassCard
import com.intentpay.ui.components.MeshBackground
import com.intentpay.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val spends by viewModel.categorySpends.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text("Analytics", fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = Color.White) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            cornerRadius = 24.dp
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text("This Month", color = TextSecondary, fontSize = 16.sp)
                                SpendingRing(
                                    spends = spends,
                                    totalSpent = totalSpent,
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                )
                            }
                        }
                    }

                    item {
                        Text("Category Breakdown", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                    }

                    if (spends.isEmpty()) {
                        item {
                            Text("No spending data yet this month.", color = TextSecondary, modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(spends) { spend ->
                            CategorySpendRow(spend)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(140.dp)) // padding for bottom nav
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySpendRow(spend: CategorySpend) {
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(spend.category.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(spend.category.emoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(spend.category.displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                
                // Mini progress bar
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { spend.percentage },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color(spend.category.colorHex),
                    trackColor = Color(0x1AFFFFFF),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text("₹${spend.amount}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${(spend.percentage * 100).roundToInt()}%", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}
