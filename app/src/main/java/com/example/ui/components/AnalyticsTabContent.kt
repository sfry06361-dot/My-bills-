package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Bill
import com.example.ui.MonthlyStats
import java.time.LocalDate
import java.util.Locale

@Composable
fun AnalyticsTabContent(
    bills: List<Bill>,
    monthlyStats: MonthlyStats,
    currency: String = "$",
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val totalBillsCount = bills.size
    val paidBillsCount = bills.count { it.isPaid }
    val overdueBillsCount = bills.count { it.isOverdue(today) }

    val spendingByCategory = remember(bills) {
        bills.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
    }

    val maxCategorySpend = spendingByCategory.maxOfOrNull { it.value } ?: 1.0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.PaletteViolet.copy(alpha = 0.14f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(com.example.ui.theme.PaletteViolet, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Spending Analytics",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Multi-color category breakdown & payment statistics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Bills",
                    value = totalBillsCount.toString(),
                    subtitle = "$paidBillsCount paid",
                    color = com.example.ui.theme.PaletteIndigo,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Paid Ratio",
                    value = if (totalBillsCount > 0) "${(paidBillsCount * 100) / totalBillsCount}%" else "0%",
                    subtitle = String.format(Locale.US, "%s%.2f paid", currency, monthlyStats.totalPaid),
                    color = com.example.ui.theme.PaletteCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Overdue",
                    value = overdueBillsCount.toString(),
                    subtitle = "Needs attention",
                    color = com.example.ui.theme.PaletteRose,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category Breakdown Header
        item {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (spendingByCategory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No bills recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(spendingByCategory.size) { index ->
                val (category, amount) = spendingByCategory[index]
                val fraction = (amount / maxCategorySpend).toFloat().coerceIn(0.05f, 1f)
                val percentageOfTotal = if (monthlyStats.totalDue > 0) (amount / monthlyStats.totalDue * 100).toInt() else 0
                val barColor = com.example.ui.theme.MultiColorPalette[index % com.example.ui.theme.MultiColorPalette.size]

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CategoryIconBox(category = category)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "$percentageOfTotal% of monthly total",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = String.format(Locale.US, "%s%.2f", currency, amount),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = barColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = barColor,
                            trackColor = barColor.copy(alpha = 0.16f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
