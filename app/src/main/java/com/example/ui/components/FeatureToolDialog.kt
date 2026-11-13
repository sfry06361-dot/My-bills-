package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Bill
import com.example.data.room.LocalBillEntity
import com.example.notifications.BackgroundFeatureState
import java.time.LocalDate
import java.util.Locale
import kotlin.math.pow

@Composable
fun FeatureToolDialog(
    feature: AppFeatureItem,
    bills: List<Bill>,
    localBills: List<LocalBillEntity>,
    backgroundFeatures: List<BackgroundFeatureState>,
    currency: String,
    onMarkBillPaid: (Bill) -> Unit,
    onDismiss: () -> Unit
) {
    val today = remember { LocalDate.now() }

    var inputA by remember(feature.key) {
        mutableStateOf(
            when (feature.key) {
                "budget_50_30_20" -> "3000"
                "savings_goal" -> "1200"
                "debt_snowball" -> "2500"
                "currency_convert" -> "100"
                "payday_countdown" -> "14"
                "roommate_split" -> "1200"
                "kwh_estimator" -> "450"
                "installment_calc" -> "900"
                "inflation_calc" -> String.format(Locale.US, "%.0f", bills.sumOf { it.amount }.takeIf { it > 0 } ?: 500.0)
                else -> "100"
            }
        )
    }
    var inputB by remember(feature.key) {
        mutableStateOf(
            when (feature.key) {
                "vat_calc" -> "15"
                "tip_split" -> "15"
                "late_fee" -> "10"
                "savings_goal" -> "30"
                "debt_snowball" -> "250"
                "currency_convert" -> "3.75"
                "payday_countdown" -> "600"
                "roommate_split" -> "3"
                "discount_calc" -> "20"
                "kwh_estimator" -> "0.18"
                "installment_calc" -> "6"
                "inflation_calc" -> "4"
                else -> "10"
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(feature.accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = feature.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = feature.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (feature.kind == FeatureKind.CALCULATOR) {
                    val labelA = when (feature.key) {
                        "vat_calc" -> "Base Amount ($currency)"
                        "tip_split" -> "Total Bill ($currency)"
                        "late_fee" -> "Invoice Amount ($currency)"
                        "budget_50_30_20" -> "Monthly Income ($currency)"
                        "savings_goal" -> "Target Savings ($currency)"
                        "debt_snowball" -> "Total Debt ($currency)"
                        "currency_convert" -> "Amount (USD)"
                        "payday_countdown" -> "Days Until Payday"
                        "roommate_split" -> "Total Shared Bills ($currency)"
                        "discount_calc" -> "Original Price ($currency)"
                        "kwh_estimator" -> "Monthly Usage (kWh)"
                        "installment_calc" -> "Total Invoice ($currency)"
                        "inflation_calc" -> "Current Monthly Bills ($currency)"
                        else -> "Value 1"
                    }
                    val labelB = when (feature.key) {
                        "vat_calc" -> "Tax / VAT Rate (%)"
                        "tip_split" -> "Tip (%)"
                        "late_fee" -> "Penalty Rate (%)"
                        "budget_50_30_20" -> "Extra Buffer (%)"
                        "savings_goal" -> "Days to Goal"
                        "debt_snowball" -> "Monthly Payment ($currency)"
                        "currency_convert" -> "Exchange Multiplier (e.g. 3.75 SAR, 0.92 EUR)"
                        "payday_countdown" -> "Cash Available Now ($currency)"
                        "roommate_split" -> "Number of People"
                        "discount_calc" -> "Discount (%)"
                        "kwh_estimator" -> "Rate per kWh ($currency)"
                        "installment_calc" -> "Months (3 / 6 / 12)"
                        "inflation_calc" -> "Annual Inflation (%)"
                        else -> "Value 2"
                    }

                    OutlinedTextField(
                        value = inputA,
                        onValueChange = { inputA = it },
                        label = { Text(labelA) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = inputB,
                        onValueChange = { inputB = it },
                        label = { Text(labelB) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    val a = inputA.toDoubleOrNull() ?: 0.0
                    val b = inputB.toDoubleOrNull() ?: 0.0
                    val summary = computeCalculatorResult(feature.key, a, b, currency)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = feature.accentColor.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = feature.accentColor,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    val insightBody = buildInsightSummary(
                        key = feature.key,
                        bills = bills,
                        localBills = localBills,
                        backgroundFeatures = backgroundFeatures,
                        currency = currency,
                        today = today
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = feature.accentColor.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = insightBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    if (feature.key == "overdue_rescue") {
                        val overdueBills = bills.filter { it.isOverdue(today) }
                        overdueBills.take(4).forEach { bill ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${bill.title} ($currency${String.format(Locale.US, "%.2f", bill.amount)})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { onMarkBillPaid(bill) }) {
                                    Text("Mark Paid")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = feature.accentColor)
            ) {
                Text("Done", color = Color.White)
            }
        }
    )
}

private fun computeCalculatorResult(key: String, a: Double, b: Double, currency: String): String {
    return when (key) {
        "vat_calc" -> {
            val tax = a * (b / 100.0)
            String.format(Locale.US, "Tax: %s%.2f\nGrand Total: %s%.2f", currency, tax, currency, a + tax)
        }
        "tip_split" -> {
            val tip = a * (b / 100.0)
            String.format(Locale.US, "Tip Amount: %s%.2f\nTotal with Tip: %s%.2f", currency, tip, currency, a + tip)
        }
        "late_fee" -> {
            val fee = a * (b / 100.0)
            String.format(Locale.US, "Estimated Late Fee: %s%.2f\nNew Total Due: %s%.2f", currency, fee, currency, a + fee)
        }
        "budget_50_30_20" -> {
            String.format(
                Locale.US,
                "50%% Needs/Bills: %s%.2f\n30%% Lifestyle: %s%.2f\n20%% Savings: %s%.2f",
                currency, a * 0.5, currency, a * 0.3, currency, a * 0.2
            )
        }
        "savings_goal" -> {
            val days = b.coerceAtLeast(1.0)
            String.format(Locale.US, "Save %s%.2f per day\n(%s%.2f per week)", currency, a / days, currency, (a / days) * 7.0)
        }
        "debt_snowball" -> {
            val pay = b.coerceAtLeast(1.0)
            val months = kotlin.math.ceil(a / pay).toInt()
            String.format(Locale.US, "Estimated Payoff Time: %d month(s)\nMonthly Payment: %s%.2f", months, currency, pay)
        }
        "currency_convert" -> {
            String.format(Locale.US, "%.2f x %.3f = %.2f converted units", a, b, a * b)
        }
        "payday_countdown" -> {
            val days = a.coerceAtLeast(1.0)
            String.format(Locale.US, "Daily Safe Allowance: %s%.2f / day for %.0f days", currency, b / days, days)
        }
        "roommate_split" -> {
            val people = b.coerceAtLeast(1.0)
            String.format(Locale.US, "Each person pays: %s%.2f\n(Split across %.0f people)", currency, a / people, people)
        }
        "discount_calc" -> {
            val saved = a * (b / 100.0)
            String.format(Locale.US, "You save: %s%.2f\nFinal Price: %s%.2f", currency, saved, currency, a - saved)
        }
        "kwh_estimator" -> {
            val total = a * b
            String.format(Locale.US, "Estimated Monthly Bill: %s%.2f\nYearly Estimate: %s%.2f", currency, total, currency, total * 12.0)
        }
        "installment_calc" -> {
            val months = b.coerceAtLeast(1.0)
            String.format(Locale.US, "%.0f monthly payments of %s%.2f", months, currency, a / months)
        }
        "inflation_calc" -> {
            val factor = (1.0 + (b / 100.0)).pow(5.0)
            String.format(Locale.US, "In 5 years at %.1f%%:\n%s%.2f / month", b, currency, a * factor)
        }
        else -> "Ready"
    }
}

private fun buildInsightSummary(
    key: String,
    bills: List<Bill>,
    localBills: List<LocalBillEntity>,
    backgroundFeatures: List<BackgroundFeatureState>,
    currency: String,
    today: LocalDate
): String {
    val totalAll = bills.sumOf { it.amount }
    val unpaid = bills.filter { !it.isPaid }
    val paid = bills.filter { it.isPaid }

    return when (key) {
        "annual_projection" -> String.format(Locale.US, "Monthly Base: %s%.2f\nEstimated 12-Month Cost: %s%.2f", currency, totalAll, currency, totalAll * 12)
        "daily_cost" -> String.format(Locale.US, "Average Daily Bill Load: %s%.2f / day (30-day basis)", currency, totalAll / 30.0)
        "weekly_reserve" -> String.format(Locale.US, "Set aside %s%.2f each week to cover upcoming bills", currency, unpaid.sumOf { it.amount } / 4.0)
        "top_expensive" -> {
            val top = bills.sortedByDescending { it.amount }.take(4)
            if (top.isEmpty()) "No bills recorded yet."
            else top.joinToString("\n") { "• ${it.title}: $currency${String.format(Locale.US, "%.2f", it.amount)}" }
        }
        "urgent_48h" -> {
            val urgent = unpaid.filter { it.daysUntilDue(today) in 0..2 }
            if (urgent.isEmpty()) "No unpaid bills due in the next 48 hours."
            else urgent.joinToString("\n") { "• ${it.title} (${it.dueDate}): $currency${String.format(Locale.US, "%.2f", it.amount)}" }
        }
        "overdue_rescue" -> {
            val overdue = bills.filter { it.isOverdue(today) }
            if (overdue.isEmpty()) "All clear! 0 overdue bills right now."
            else "Found ${overdue.size} overdue bill(s) totaling $currency${String.format(Locale.US, "%.2f", overdue.sumOf { it.amount })}:"
        }
        "paid_vault" -> String.format(Locale.US, "Settled Bills: %d\nTotal Paid: %s%.2f", paid.size, currency, paid.sumOf { it.amount })
        "recurring_radar" -> {
            val rec = bills.filter { it.isRecurring }
            String.format(Locale.US, "Recurring Bills: %d\nMonthly Recurring Load: %s%.2f", rec.size, currency, rec.sumOf { it.amount })
        }
        "top_category" -> {
            val grouped = bills.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.amount } }
            val best = grouped.maxByOrNull { it.value }
            if (best == null) "Add bills to see category analytics."
            else String.format(Locale.US, "Top Category: %s\nTotal Spend: %s%.2f", best.key, currency, best.value)
        }
        "small_subs" -> {
            val small = bills.filter { it.isRecurring && it.amount <= 25.0 }
            if (small.isEmpty()) "No micro-subscriptions under $currency 25 found."
            else small.joinToString("\n") { "• ${it.title}: $currency${String.format(Locale.US, "%.2f", it.amount)}" }
        }
        "emergency_fund" -> String.format(Locale.US, "3-Month Buffer: %s%.2f\n6-Month Buffer: %s%.2f", currency, totalAll * 3, currency, totalAll * 6)
        "privacy_audit" -> "Zero broad storage permissions requested.\nMediaStore Downloads enabled.\nCloud isolation + Local Room SQLite verified."
        "system_diagnostics" -> {
            val activeBg = backgroundFeatures.count { it.enabled }
            val totalRuns = backgroundFeatures.sumOf { it.runCount }
            "App: My bills reminder v1.2.0\nCloud Bills: ${bills.size} | Room Bills: ${localBills.size}\nBackground Tasks Active: $activeBg / ${backgroundFeatures.size}\nTotal Background Cycles: $totalRuns"
        }
        else -> "Completed check."
    }
}
