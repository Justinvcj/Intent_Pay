package com.intentpay.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.intentpay.ui.components.GroupedCard
import com.intentpay.ui.components.ListDivider
import com.intentpay.ui.components.ScreenTitle
import com.intentpay.ui.components.SectionLabel
import com.intentpay.ui.components.formatInr
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val spends by viewModel.categorySpends.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val monthLabel = SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(Date())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
    ) {
        item { ScreenTitle("Analytics", modifier = Modifier.padding(bottom = 24.dp)) }

        item {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatInr(totalSpent),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            GroupedCard {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SpendingRing(
                        spends = spends,
                        totalSpent = totalSpent,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel("By category")
        }

        if (spends.isEmpty()) {
            item {
                Text(
                    "No spending yet this month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        } else {
            item {
                GroupedCard {
                    spends.forEachIndexed { index, spend ->
                        CategoryBreakdownRow(spend)
                        if (index != spends.lastIndex) ListDivider(startInset = 44.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySpendRow(spend: CategorySpend) = CategoryBreakdownRow(spend)

@Composable
private fun CategoryBreakdownRow(spend: CategorySpend) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(spend.category.colorHex))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            spend.category.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "${formatInr(spend.amount)} · ${(spend.percentage * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
