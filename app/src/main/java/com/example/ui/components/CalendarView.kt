package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    bills: List<Bill>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = remember(currentMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        currentMonth.format(formatter)
    }

    val daysInMonth = remember(currentMonth) { currentMonth.lengthOfMonth() }
    val firstDayOfMonth = remember(currentMonth) { currentMonth.atDay(1) }
    val firstDayOfWeekIndex = remember(firstDayOfMonth) {
        // Monday = 1 ... Sunday = 7 -> let's make Sunday = 0
        (firstDayOfMonth.dayOfWeek.value % 7)
    }

    // Map epoch day to list of bills
    val billsByDate = remember(bills, currentMonth) {
        bills.groupBy { it.dueDate }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_view"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Month Header & Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Payment Deadlines",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTodayClicked,
                        modifier = Modifier.testTag("calendar_today_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Today",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier.testTag("calendar_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month"
                        )
                    }
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.testTag("calendar_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Days of week header (Sun, Mon, Tue...)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                dayNames.forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            val totalCells = firstDayOfWeekIndex + daysInMonth
            val totalRows = (totalCells + 6) / 7

            val today = LocalDate.now()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOfWeekIndex + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellDate = currentMonth.atDay(dayNumber)
                                val isSelected = cellDate == selectedDate
                                val isToday = cellDate == today
                                val dayBills = billsByDate[cellDate] ?: emptyList()

                                DayCell(
                                    date = cellDate,
                                    dayNumber = dayNumber,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    bills = dayBills,
                                    onClick = { onDateSelected(cellDate) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = MaterialTheme.colorScheme.error, label = "Overdue")
                LegendItem(color = MaterialTheme.colorScheme.tertiary, label = "Due Soon (≤3d)")
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Upcoming")
                LegendItem(color = MaterialTheme.colorScheme.secondary, label = "Paid")
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    bills: List<Bill>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val hasOverdue = bills.any { it.isOverdue(today) }
    val hasDueSoon = bills.any { it.isDueSoon(today) }
    val allPaid = bills.isNotEmpty() && bills.all { it.isPaid }

    val dotColor = when {
        hasOverdue -> MaterialTheme.colorScheme.error
        hasDueSoon -> MaterialTheme.colorScheme.tertiary
        allPaid -> MaterialTheme.colorScheme.secondary
        bills.isNotEmpty() -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "cellBg"
    )

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .testTag("calendar_day_$dayNumber"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || isToday || bills.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = textColor
            )

            if (bills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else dotColor)
                    )
                    if (bills.size > 1) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else dotColor.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
