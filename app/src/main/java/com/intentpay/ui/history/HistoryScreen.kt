package com.intentpay.ui.history

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.ui.components.GlassCard
import com.intentpay.ui.components.MeshBackground
import com.intentpay.ui.dashboard.TransactionRow
import com.intentpay.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val groupedTransactions by viewModel.groupedTransactions.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text("History", fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = Color.White) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { padding ->
                if (groupedTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No transactions yet.", color = TextSecondary, fontSize = 16.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        groupedTransactions.forEach { (month, transactions) ->
                            stickyHeader {
                                Text(
                                    text = month,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp, top = if (month == groupedTransactions.keys.first()) 0.dp else 24.dp)
                                )
                            }
                            
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth().animateContentSize(
                                        animationSpec = androidx.compose.animation.core.spring(
                                            dampingRatio = 1f,
                                            stiffness = 400f
                                        )
                                    ),
                                    cornerRadius = 20.dp
                                ) {
                                    Column {
                                        transactions.forEachIndexed { index, tx ->
                                            TransactionRow(tx)
                                            if (index < transactions.size - 1) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(start = 72.dp),
                                                    color = Color(0x1AFFFFFF),
                                                    thickness = 0.5.dp
                                                )
                                            }
                                        }
                                    }
                                }
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
}
