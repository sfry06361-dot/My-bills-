package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BillCard(
    bill: Bill,
    onTogglePaid: (Bill) -> Unit,
    onEdit: (Bill) -> Unit,
    onDelete: (Bill) -> Unit,
    onTestReminder: (Bill) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val isOverdue = remember(bill, today) { bill.isOverdue(today) }
    val daysUntil = remember(bill, today) { bill.daysUntilDue(today) }
    var menuExpanded by remember { mutableStateOf(false) }

    val formattedAmount = remember(bill.amount, bill.currency) {
        String.format(Locale.US, "%s%.2f", bill.currency, bill.amount)
    }

    val dateFormatted = remember(bill.dueDate) {
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
        bill.dueDate.format(formatter)
    }

    val statusBadgeColor: Color
    val statusBadgeBg: Color
    val statusText: String

    when {
        bill.isPaid -> {
            statusBadgeColor = MaterialTheme.colorScheme.secondary
            statusBadgeBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            statusText = "Paid"
        }
        isOverdue -> {
            statusBadgeColor = MaterialTheme.colorScheme.error
            statusBadgeBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            val overdueDays = -daysUntil
            statusText = if (overdueDays == 1L) "Overdue by 1 day" else "Overdue by $overdueDays days"
        }
        daysUntil == 0L -> {
            statusBadgeColor = MaterialTheme.colorScheme.tertiary
            statusBadgeBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
            statusText = "Due Today"
        }
        daysUntil == 1L -> {
            statusBadgeColor = MaterialTheme.colorScheme.tertiary
            statusBadgeBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            statusText = "Due Tomorrow"
        }
        daysUntil in 2..3 -> {
            statusBadgeColor = MaterialTheme.colorScheme.tertiary
            statusBadgeBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            statusText = "Due in $daysUntil days"
        }
        else -> {
            statusBadgeColor = MaterialTheme.colorScheme.primary
            statusBadgeBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            statusText = "Due in $daysUntil days"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bill_card_${bill.id}")
            .clickable { onEdit(bill) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (bill.isPaid) 1.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                CategoryIconBox(category = bill.category)

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Category
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = bill.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (bill.isPaid) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (bill.isRecurring) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Recurring",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${bill.category} • $dateFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (bill.isPaid) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (bill.isPaid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                )

                // Menu button
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("bill_menu_${bill.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Bill") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit(bill)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Test Reminder Alert") },
                            leadingIcon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onTestReminder(bill)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete(bill)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Badges & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Deadline Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusBadgeBg,
                        contentColor = statusBadgeColor
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Reminder Badge
                    if (bill.reminderEnabled) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${bill.reminderDaysBefore}d before",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                // Quick Action: Mark as Paid / Unpaid Button
                FilledTonalButton(
                    onClick = { onTogglePaid(bill) },
                    modifier = Modifier.testTag("bill_toggle_paid_${bill.id}"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (bill.isPaid) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (bill.isPaid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (bill.isPaid) "Paid" else "Mark Paid",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            if (bill.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = bill.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoryIconBox(
    category: String,
    modifier: Modifier = Modifier
) {
    val (icon, color, bg) = when (category.lowercase()) {
        "utilities", "electricity", "power" -> Triple(
            Icons.Default.Bolt,
            Color(0xFFD97706),
            Color(0xFFFEF3C7)
        )
        "internet", "wifi", "cable" -> Triple(
            Icons.Default.Wifi,
            Color(0xFF0284C7),
            Color(0xFFE0F2FE)
        )
        "housing", "rent", "mortgage" -> Triple(
            Icons.Default.Home,
            Color(0xFF0F766E),
            Color(0xFFCCFBF1)
        )
        "insurance", "health", "medical" -> Triple(
            Icons.Default.MedicalServices,
            Color(0xFFE11D48),
            Color(0xFFFFE4E6)
        )
        "car", "auto", "vehicle", "transport" -> Triple(
            Icons.Default.DirectionsCar,
            Color(0xFF7C3AED),
            Color(0xFFEDE9FE)
        )
        "credit", "card", "loan", "bank" -> Triple(
            Icons.Default.CreditCard,
            Color(0xFF059669),
            Color(0xFFD1FAE5)
        )
        else -> Triple(
            Icons.Default.Receipt,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}
