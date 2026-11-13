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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.signOut
import com.example.data.model.Bill
import com.example.notifications.BackgroundFeaturesEngine
import com.example.notifications.BillReminderManager
import com.example.ui.components.AddEditBillDialog
import com.example.ui.components.AnalyticsTabContent
import com.example.ui.components.BillCard
import com.example.ui.components.CalendarView
import com.example.ui.components.FeaturesHubScreen
import com.example.ui.components.LocalBillsScreen
import com.example.ui.components.SettingsScreen
import com.example.ui.components.StartupVideoScreen
import com.example.ui.theme.MultiColorPalette
import com.example.ui.theme.PaletteAmber
import com.example.ui.theme.PaletteEmerald
import com.example.ui.theme.PaletteIndigo
import com.example.ui.theme.PaletteMagenta
import com.example.ui.theme.PaletteOrange
import com.example.ui.theme.PaletteRose
import com.example.ui.theme.PaletteSky
import com.example.ui.theme.PaletteViolet
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

    // 0: Calendar, 1: Bills (Cloud/Reminders/Room), 2: Features (40+10), 3: Analytics, 4: Settings
    var selectedTab by remember { mutableIntStateOf(0) }
    var billsSubTab by remember { mutableIntStateOf(0) } // 0: All Bills, 1: Reminders, 2: Room DB
    var billToEdit by remember { mutableStateOf<Bill?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    // Keyed on currentUserId so StartupVideoScreen auto-plays immediately on every sign-in
    var showStartupVideo by remember(viewModel.currentUserId) { mutableStateOf(true) }

    val appCurrency by viewModel.appCurrency.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val localBillViewModel: LocalBillViewModel = viewModel()
    val localBills by localBillViewModel.allLocalBills.collectAsStateWithLifecycle()

    // 10 Background Features Engine
    val bgEngine = remember(context) { BackgroundFeaturesEngine(context) }
    val backgroundFeatures by bgEngine.features.collectAsStateWithLifecycle()

    DisposableEffect(bgEngine) {
        bgEngine.startPeriodicBackgroundMonitoring()
        onDispose { bgEngine.stop() }
    }

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

    LaunchedEffect(allBills, localBills) {
        bgEngine.updateBillData(allBills, localBills)
    }

    val tabColors = remember {
        listOf(
            PaletteIndigo,   // 0: Calendar
            PaletteSky,      // 1: Bills
            PaletteMagenta,  // 2: Features
            PaletteViolet,   // 3: Analytics
            PaletteOrange    // 4: Settings
        )
    }
    val activeTabColor = tabColors[selectedTab.coerceIn(0, tabColors.lastIndex)]

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(activeTabColor, PaletteMagenta)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "My bills reminder",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val userEmail = Firebase.auth.currentUser?.email
                            val subtitle = userEmail ?: "Guest • 40+10 Features"
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = activeTabColor
                            )
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
                    label = { Text("Calendar", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PaletteIndigo,
                        selectedTextColor = PaletteIndigo,
                        indicatorColor = PaletteIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_calendar")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Bills") },
                    label = { Text("Bills", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PaletteSky,
                        selectedTextColor = PaletteSky,
                        indicatorColor = PaletteSky.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_bills")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Features") },
                    label = { Text("Features", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PaletteMagenta,
                        selectedTextColor = PaletteMagenta,
                        indicatorColor = PaletteMagenta.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_features")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PaletteViolet,
                        selectedTextColor = PaletteViolet,
                        indicatorColor = PaletteViolet.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_analytics")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PaletteOrange,
                        selectedTextColor = PaletteOrange,
                        indicatorColor = PaletteOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 || (selectedTab == 1 && billsSubTab != 2)) {
                ExtendedFloatingActionButton(
                    onClick = {
                        billToEdit = null
                        showAddDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Bill") },
                    text = { Text("Add Bill") },
                    modifier = Modifier.testTag("add_bill_fab"),
                    containerColor = activeTabColor,
                    contentColor = Color.White
                )
            }
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
                            appCurrency = appCurrency,
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
                        1 -> Column(modifier = Modifier.fillMaxSize()) {
                            BillsSubTabBar(
                                selectedSubTab = billsSubTab,
                                onSelectSubTab = { billsSubTab = it }
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                when (billsSubTab) {
                                    0 -> AllBillsTabContent(
                                        bills = state.data,
                                        currentFilter = filterStatus,
                                        searchQuery = searchQuery,
                                        appCurrency = appCurrency,
                                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
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
                                    1 -> RemindersTabContent(
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
                                    else -> LocalBillsScreen(
                                        viewModel = localBillViewModel,
                                        appCurrency = appCurrency
                                    )
                                }
                            }
                        }
                        2 -> FeaturesHubScreen(
                            bills = state.data,
                            localBills = localBills,
                            backgroundFeatures = backgroundFeatures,
                            currency = appCurrency,
                            onToggleBackgroundFeature = { key, enabled ->
                                bgEngine.toggleFeature(key, enabled)
                            },
                            onRunAllBackgroundNow = {
                                bgEngine.runAllEnabledTasks(notifyUser = true)
                            },
                            onAddSampleBills = {
                                val today = LocalDate.now()
                                val samples = listOf(
                                    Bill(
                                        title = "Fiber Internet",
                                        amount = 59.99,
                                        currency = appCurrency,
                                        dueDateEpochDay = today.plusDays(2).toEpochDay(),
                                        category = "Internet",
                                        isRecurring = true,
                                        reminderDaysBefore = 2,
                                        notes = "Monthly high-speed fiber"
                                    ),
                                    Bill(
                                        title = "Electric Grid",
                                        amount = 112.50,
                                        currency = appCurrency,
                                        dueDateEpochDay = today.plusDays(5).toEpochDay(),
                                        category = "Utilities",
                                        isRecurring = true,
                                        reminderDaysBefore = 3,
                                        notes = "Home electricity bill"
                                    ),
                                    Bill(
                                        title = "Health Shield Insurance",
                                        amount = 185.00,
                                        currency = appCurrency,
                                        dueDateEpochDay = today.plusDays(1).toEpochDay(),
                                        category = "Insurance",
                                        isRecurring = true,
                                        reminderDaysBefore = 3
                                    ),
                                    Bill(
                                        title = "Apartment Rent",
                                        amount = 850.00,
                                        currency = appCurrency,
                                        dueDateEpochDay = today.plusDays(9).toEpochDay(),
                                        category = "Housing",
                                        isRecurring = true,
                                        reminderDaysBefore = 5
                                    ),
                                    Bill(
                                        title = "Auto Lease",
                                        amount = 290.00,
                                        currency = appCurrency,
                                        dueDateEpochDay = today.minusDays(1).toEpochDay(),
                                        category = "Car",
                                        isRecurring = true,
                                        reminderDaysBefore = 3
                                    )
                                )
                                samples.forEach { sample -> viewModel.createBill(sample) }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Added 5 colorful sample bills!")
                                }
                            },
                            onMarkBillPaid = { viewModel.togglePaid(it) },
                            onNavigateToCalendarToday = {
                                viewModel.goToToday()
                                selectedTab = 0
                            },
                            onNavigateToBillsSearch = {
                                billsSubTab = 0
                                selectedTab = 1
                            },
                            onNavigateToRoomDb = {
                                billsSubTab = 2
                                selectedTab = 1
                            },
                            onReplayStartupVideo = { showStartupVideo = true }
                        )
                        3 -> AnalyticsTabContent(
                            bills = state.data,
                            monthlyStats = monthlyStats,
                            currency = appCurrency
                        )
                        4 -> SettingsScreen(
                            viewModel = viewModel,
                            localViewModel = localBillViewModel,
                            onReplayStartupVideo = { showStartupVideo = true }
                        )
                    }
                }
            }
        }
    }

    if (showStartupVideo) {
        StartupVideoScreen(
            onDismiss = { showStartupVideo = false }
        )
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
                val billWithCurrency = bill.copy(currency = appCurrency)
                if (billToEdit == null) {
                    viewModel.createBill(billWithCurrency) { res ->
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
                BillReminderManager.showInstantReminderNotification(context, bill.copy(currency = appCurrency))
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Preview notification triggered!")
                }
            },
            appCurrency = appCurrency
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
    appCurrency: String,
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
            MonthlySummaryCard(stats = monthlyStats, currency = appCurrency)
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
    searchQuery: String,
    appCurrency: String,
    onSearchQueryChanged: (String) -> Unit,
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
        // Search Bar
        item {
            androidx.compose.material3.OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bill_search_bar"),
                placeholder = { Text("Search bills...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }

        // Filter Chips Row (each filter has its own distinct color)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filterColors = listOf(PaletteIndigo, PaletteSky, PaletteRose, PaletteEmerald)
                BillFilter.values().forEachIndexed { idx, filter ->
                    val chipColor = filterColors[idx % filterColors.size]
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.18f),
                            selectedLabelColor = chipColor
                        ),
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
private fun BillsSubTabBar(
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit
) {
    val pills = listOf(
        Triple("Cloud Bills", Icons.AutoMirrored.Filled.ReceiptLong, PaletteSky),
        Triple("Reminders", Icons.Default.NotificationsActive, PaletteAmber),
        Triple("Room DB", Icons.Default.Storage, PaletteEmerald)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        pills.forEachIndexed { index, (label, icon, accentColor) ->
            val selected = selectedSubTab == index
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectSubTab(index) }
                    .testTag("bills_subtab_$index"),
                shape = RoundedCornerShape(14.dp),
                color = if (selected) accentColor else accentColor.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) Color.White else accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (selected) Color.White else accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    stats: MonthlyStats,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaletteIndigo.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Month At A Glance",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = PaletteIndigo
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PaletteMagenta.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "${stats.upcomingCount} upcoming",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PaletteMagenta,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStatItem(
                    label = "Total Due",
                    value = String.format(Locale.US, "%s%.2f", currency, stats.totalDue),
                    color = PaletteViolet
                )
                SummaryStatItem(
                    label = "Paid So Far",
                    value = String.format(Locale.US, "%s%.2f", currency, stats.totalPaid),
                    color = PaletteEmerald
                )
                SummaryStatItem(
                    label = "Remaining",
                    value = String.format(Locale.US, "%s%.2f", currency, stats.totalPending),
                    color = PaletteAmber
                )
            }

            if (stats.overdueCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PaletteRose.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PaletteRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${stats.overdueCount} overdue bill deadline(s) require attention!",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PaletteRose
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
