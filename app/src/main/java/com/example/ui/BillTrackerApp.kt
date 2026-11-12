package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.signOut
import com.example.data.model.Bill
import com.example.notifications.BillReminderManager
import com.example.ui.components.AddEditBillDialog
import com.example.ui.components.BillCard
import com.example.ui.components.CalendarView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillTrackerApp(
    viewModel: BillViewModel,
    onSignOutClicked: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val billsState by viewModel.bills.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()
    val monthlyStats by viewModel.monthlyStats.collectAsStateWithLifecycle()
    val billsOnSelectedDate by viewModel.billsOnSelectedDate.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Calendar, 1: All Bills, 2: Reminders
    var billToEdit by remember { mutableStateOf<Bill?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Permission handling for notifications
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Payment notifications enabled!")
            }
        }
    }

    LaunchedEffect(Unit) {
        BillReminderManager.createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val allBills = (billsState as? UiState.Success)?.data ?: emptyList()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BillTrack",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            val userEmail = Firebase.auth.currentUser?.email
                            if (userEmail != null) {
                                Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val credentialManager = CredentialManager.create(context)
                            signOut(credentialManager, coroutineScope, onSignOutClicked)
                        },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar") },
                    modifier = Modifier.testTag("nav_tab_calendar")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Bills") },
                    label = { Text("Bills") },
                    modifier = Modifier.testTag("nav_tab_bills")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.NotificationsActive, contentDescription = "Reminders") },
                    label = { Text("Reminders") },
                    modifier = Modifier.testTag("nav_tab_reminders")
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    billToEdit = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Bill") },
                text = { Text("Add Bill") },
                modifier = Modifier.testTag("add_bill_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = billsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Error Loading Bills",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    when (selectedTab) {
                        0 -> CalendarTabContent(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            allBills = state.data,
                            billsOnSelectedDate = billsOnSelectedDate,
                            monthlyStats = monthlyStats,
                            onDateSelected = { viewModel.setSelectedDate(it) },
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onTodayClicked = { viewModel.goToToday() },
                            onTogglePaid = { viewModel.togglePaid(it) },
                            onEdit = {
                                billToEdit = it
                                showAddDialog = true
                            },
                            onDelete = { bill ->
                                viewModel.deleteBill(bill.id) { res ->
                                    coroutineScope.launch {
                                        if (res.isSuccess) {
                                            snackbarHostState.showSnackbar("Deleted ${bill.title}")
                                        }
                                    }
                                }
                            },
                            onTestReminder = { bill ->
                                BillReminderManager.showInstantReminderNotification(context, bill)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Reminder sent for ${bill.title}")
                                }
                            }
                        )
                        1 -> AllBillsTabContent(
                            bills = state.data,
                            currentFilter = filterStatus,
                            onFilterSelected = { viewModel.setFilter(it) },
                            onTogglePaid = { viewModel.togglePaid(it) },
                            onEdit = {
                                billToEdit = it
                                showAddDialog = true
                            },
                            onDelete = { bill ->
                                viewModel.deleteBill(bill.id) { res ->
                                    coroutineScope.launch {
                                        if (res.isSuccess) {
                                            snackbarHostState.showSnackbar("Deleted ${bill.title}")
                                        }
                                    }
                                }
                            },
                            onTestReminder = { bill ->
                                BillReminderManager.showInstantReminderNotification(context, bill)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Reminder sent for ${bill.title}")
                                }
                            }
                        )
                        2 -> RemindersTabContent(
                            bills = state.data,
                            hasPermission = hasNotificationPermission,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            onEdit = {
                                billToEdit = it
                                showAddDialog = true
                            },
                            onTestReminder = { bill ->
                                BillReminderManager.showInstantReminderNotification(context, bill)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Sent reminder for ${bill.title} (${bill.reminderDaysBefore}d in advance)")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog) {
        AddEditBillDialog(
            billToEdit = billToEdit,
            initialDate = selectedDate,
            onDismiss = {
                showAddDialog = false
                billToEdit = null
            },
            onSave = { bill ->
                if (billToEdit == null) {
                    viewModel.createBill(bill) { res ->
                        showAddDialog = false
                        coroutineScope.launch {
                            if (res.isSuccess) {
                                snackbarHostState.showSnackbar("Bill added: ${bill.title}")
                            } else {
                                snackbarHostState.showSnackbar("Failed to add bill")
                            }
                        }
                    }
                } else {
                    viewModel.updateBill(bill) { res ->
                        showAddDialog = false
                        billToEdit = null
                        coroutineScope.launch {
                            if (res.isSuccess) {
                                snackbarHostState.showSnackbar("Updated ${bill.title}")
                            } else {
                                snackbarHostState.showSnackbar("Failed to update bill")
                            }
                        }
                    }
                }
            },
            onTestNotification = { bill ->
                BillReminderManager.showInstantReminderNotification(context, bill)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Preview notification triggered!")
                }
            }
        )
    }
}

@Composable
private fun CalendarTabContent(
    currentMonth: java.time.YearMonth,
    selectedDate: LocalDate,
    allBills: List<Bill>,
    billsOnSelectedDate: List<Bill>,
    monthlyStats: MonthlyStats,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClicked: () -> Unit,
    onTogglePaid: (Bill) -> Unit,
    onEdit: (Bill) -> Unit,
    onDelete: (Bill) -> Unit,
    onTestReminder: (Bill) -> Unit
) {
    val selectedDateFormatted = remember(selectedDate) {
        val today = LocalDate.now()
        val base = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
        when (selectedDate) {
            today -> "$base (Today)"
            today.plusDays(1) -> "$base (Tomorrow)"
            today.minusDays(1) -> "$base (Yesterday)"
            else -> base
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Monthly Summary Overview Card
        item {
            MonthlySummaryCard(stats = monthlyStats)
        }

        // Calendar Grid
        item {
            CalendarView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                bills = allBills,
                onDateSelected = onDateSelected,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onTodayClicked = onTodayClicked
            )
        }

        // Section header for selected date
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedDateFormatted,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${billsOnSelectedDate.size} due",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (billsOnSelectedDate.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No bills due on this date",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Select another date or tap + to add a bill",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(billsOnSelectedDate, key = { it.id }) { bill ->
                BillCard(
                    bill = bill,
                    onTogglePaid = onTogglePaid,
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onTestReminder = onTestReminder
                )
            }
        }
    }
}

@Composable
private fun AllBillsTabContent(
    bills: List<Bill>,
    currentFilter: BillFilter,
    onFilterSelected: (BillFilter) -> Unit,
    onTogglePaid: (Bill) -> Unit,
    onEdit: (Bill) -> Unit,
    onDelete: (Bill) -> Unit,
    onTestReminder: (Bill) -> Unit
) {
    val today = remember { LocalDate.now() }

    val filteredBills = remember(bills, currentFilter) {
        when (currentFilter) {
            BillFilter.ALL -> bills
            BillFilter.UPCOMING -> bills.filter { !it.isPaid && !it.isOverdue(today) }
            BillFilter.OVERDUE -> bills.filter { it.isOverdue(today) }
            BillFilter.PAID -> bills.filter { it.isPaid }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter Chips Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BillFilter.values().forEach { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter.label) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                    )
                }
            }
        }

        if (filteredBills.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No ${currentFilter.label.lowercase()} bills found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap the + button below to create your first bill",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredBills, key = { it.id }) { bill ->
                BillCard(
                    bill = bill,
                    onTogglePaid = onTogglePaid,
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onTestReminder = onTestReminder
                )
            }
        }
    }
}

@Composable
private fun RemindersTabContent(
    bills: List<Bill>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onEdit: (Bill) -> Unit,
    onTestReminder: (Bill) -> Unit
) {
    val today = remember { LocalDate.now() }
    val reminderBills = remember(bills) {
        bills.filter { it.reminderEnabled && !it.isPaid }.sortedBy { it.dueDateEpochDay }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Notification Permission Banner if not granted
        if (!hasPermission) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notifications Disabled",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Enable notifications to receive alerts before your payment deadlines",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        androidx.compose.material3.FilledTonalButton(
                            onClick = onRequestPermission,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }
        }

        // Reminders Summary
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Customizable Payment Reminders",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You can set how many days in advance to be notified for each individual bill (e.g., 1, 3, 5, or 7 days). Tap any bill below to adjust its reminder timeline or test the instant alert.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Active Reminders (${reminderBills.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (reminderBills.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pending bills with reminders active.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(reminderBills, key = { it.id }) { bill ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(bill) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bill.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Due on ${bill.dueDate} • ${bill.currency}${String.format(Locale.US, "%.2f", bill.amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "Alert ${bill.reminderDaysBefore} days before (notifies on ${bill.reminderDate()})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onTestReminder(bill) },
                            modifier = Modifier.testTag("test_reminder_${bill.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Test Notification",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    stats: MonthlyStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Month At A Glance",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStatItem(
                    label = "Total Due",
                    value = String.format(Locale.US, "$%.2f", stats.totalDue),
                    color = MaterialTheme.colorScheme.onSurface
                )
                SummaryStatItem(
                    label = "Paid So Far",
                    value = String.format(Locale.US, "$%.2f", stats.totalPaid),
                    color = MaterialTheme.colorScheme.secondary
                )
                SummaryStatItem(
                    label = "Remaining",
                    value = String.format(Locale.US, "$%.2f", stats.totalPending),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (stats.overdueCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${stats.overdueCount} overdue bill deadline(s) require attention!",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
