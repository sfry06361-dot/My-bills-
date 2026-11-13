package com.example.ui.components

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Bill
import com.example.data.room.LocalBillEntity
import com.example.data.util.DataExportUtility
import com.example.notifications.BackgroundFeatureState
import com.example.notifications.BillReminderManager
import com.example.ui.theme.MultiColorPalette
import com.example.ui.theme.PaletteIndigo
import com.example.ui.theme.PaletteMagenta
import com.example.ui.theme.PaletteOrange
import java.time.LocalDate

@Composable
fun FeaturesHubScreen(
    bills: List<Bill>,
    localBills: List<LocalBillEntity>,
    backgroundFeatures: List<BackgroundFeatureState>,
    currency: String,
    onToggleBackgroundFeature: (String, Boolean) -> Unit,
    onRunAllBackgroundNow: () -> Unit,
    onAddSampleBills: () -> Unit,
    onMarkBillPaid: (Bill) -> Unit,
    onNavigateToCalendarToday: () -> Unit,
    onNavigateToBillsSearch: () -> Unit,
    onNavigateToRoomDb: () -> Unit,
    onReplayStartupVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("my_bills_reminder_feature_prefs", Context.MODE_PRIVATE) }

    var searchQuery by remember { mutableStateOf("") }
    var showBackgroundOnly by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(FeatureCategory.ALL) }
    var activeFeatureDialog by remember { mutableStateOf<AppFeatureItem?>(null) }

    val toggleStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            FeaturesCatalog.fortyFeatures
                .filter { it.kind == FeatureKind.TOGGLE }
                .forEach { item ->
                    this[item.key] = prefs.getBoolean(item.key, item.key == "toggle_vibrate")
                }
        }
    }

    val filteredFeatures = remember(searchQuery, selectedCategory) {
        FeaturesCatalog.fortyFeatures.filter { item ->
            val matchesCat = selectedCategory == FeatureCategory.ALL || item.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    val filteredBgFeatures = remember(searchQuery) {
        backgroundFeatures.filter { bg ->
            searchQuery.isBlank() ||
                bg.title.contains(searchQuery, ignoreCase = true) ||
                bg.description.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Multi-Color Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PaletteIndigo, PaletteMagenta, PaletteOrange)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "40 Smart Features + 10 Background",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val activeBgCount = backgroundFeatures.count { it.enabled }
                        Text(
                            text = "Every feature has its own color & live action • $activeBgCount/10 background engines active",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showBackgroundOnly = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!showBackgroundOnly) Color.White else Color.White.copy(alpha = 0.22f),
                                    contentColor = if (!showBackgroundOnly) PaletteIndigo else Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("tab_40_features")
                            ) {
                                Text("40 Features", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showBackgroundOnly = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (showBackgroundOnly) Color.White else Color.White.copy(alpha = 0.22f),
                                    contentColor = if (showBackgroundOnly) PaletteOrange else Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("tab_10_background")
                            ) {
                                Text("10 Background", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Search input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("features_search_input"),
                placeholder = { Text("Search 40 features & 10 background tasks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        if (!showBackgroundOnly) {
            // Category filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureCategory.values().forEachIndexed { idx, cat ->
                        val chipColor = MultiColorPalette[idx % MultiColorPalette.size]
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.18f),
                                selectedLabelColor = chipColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // All 40 Features cards (each with distinct color)
            items(filteredFeatures, key = { it.key }) { feature ->
                val isToggleOn = toggleStates[feature.key] ?: false
                FeatureCardRow(
                    feature = feature,
                    isToggleOn = isToggleOn,
                    onToggleChanged = { checked ->
                        toggleStates[feature.key] = checked
                        prefs.edit().putBoolean(feature.key, checked).apply()
                        Toast.makeText(
                            context,
                            "${feature.title}: ${if (checked) "ON" else "OFF"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onClick = {
                        when (feature.kind) {
                            FeatureKind.CALCULATOR, FeatureKind.INSIGHT -> {
                                activeFeatureDialog = feature
                            }
                            FeatureKind.TOGGLE -> {
                                val next = !(toggleStates[feature.key] ?: false)
                                toggleStates[feature.key] = next
                                prefs.edit().putBoolean(feature.key, next).apply()
                                Toast.makeText(
                                    context,
                                    "${feature.title}: ${if (next) "ON" else "OFF"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            FeatureKind.ACTION -> {
                                executeFeatureAction(
                                    context = context,
                                    key = feature.key,
                                    bills = bills,
                                    localBills = localBills,
                                    onAddSampleBills = onAddSampleBills,
                                    onNavigateToCalendarToday = onNavigateToCalendarToday,
                                    onNavigateToBillsSearch = onNavigateToBillsSearch,
                                    onNavigateToRoomDb = onNavigateToRoomDb,
                                    onReplayStartupVideo = onReplayStartupVideo
                                )
                            }
                        }
                    }
                )
            }
        } else {
            // 10 Background Features Section
            item {
                Button(
                    onClick = {
                        onRunAllBackgroundNow()
                        Toast.makeText(context, "Running all active background engines...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("run_all_background_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PaletteOrange)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run All 10 Background Features Now", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            items(filteredBgFeatures, key = { it.key }) { bg ->
                val accent = MultiColorPalette[bg.colorIndex % MultiColorPalette.size]
                BackgroundFeatureCard(
                    item = bg,
                    accentColor = accent,
                    onToggle = { enabled -> onToggleBackgroundFeature(bg.key, enabled) }
                )
            }
        }
    }

    val dialogFeature = activeFeatureDialog
    if (dialogFeature != null) {
        FeatureToolDialog(
            feature = dialogFeature,
            bills = bills,
            localBills = localBills,
            backgroundFeatures = backgroundFeatures,
            currency = currency,
            onMarkBillPaid = onMarkBillPaid,
            onDismiss = { activeFeatureDialog = null }
        )
    }
}

@Composable
private fun FeatureCardRow(
    feature: AppFeatureItem,
    isToggleOn: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feature_card_${feature.number}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, feature.accentColor.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(feature.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = feature.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = feature.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (feature.kind == FeatureKind.TOGGLE) {
                Switch(
                    checked = isToggleOn,
                    onCheckedChange = onToggleChanged
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = feature.accentColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = when (feature.kind) {
                            FeatureKind.CALCULATOR -> "Open"
                            FeatureKind.INSIGHT -> "View"
                            FeatureKind.ACTION -> "Run"
                            FeatureKind.TOGGLE -> ""
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = feature.accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundFeatureCard(
    item: BackgroundFeatureState,
    accentColor: Color,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bg_feature_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.30f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.enabled) Icons.Default.Bolt else Icons.Default.SettingsSuggest,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = item.enabled,
                    onCheckedChange = onToggle
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.lastStatus,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${item.intervalLabel} • #${item.runCount} (${item.lastRunTime})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun executeFeatureAction(
    context: Context,
    key: String,
    bills: List<Bill>,
    localBills: List<LocalBillEntity>,
    onAddSampleBills: () -> Unit,
    onNavigateToCalendarToday: () -> Unit,
    onNavigateToBillsSearch: () -> Unit,
    onNavigateToRoomDb: () -> Unit,
    onReplayStartupVideo: () -> Unit
) {
    when (key) {
        "sample_pack" -> {
            onAddSampleBills()
            Toast.makeText(context, "Adding 5 colorful sample bills!", Toast.LENGTH_SHORT).show()
        }
        "instant_bell" -> {
            val bill = bills.firstOrNull() ?: Bill(
                id = "demo_alert",
                title = "My bills reminder Alert",
                amount = 49.99,
                dueDateEpochDay = LocalDate.now().plusDays(1).toEpochDay(),
                reminderDaysBefore = 1,
                reminderEnabled = true
            )
            BillReminderManager.showInstantReminderNotification(context, bill)
            Toast.makeText(context, "Live reminder notification triggered!", Toast.LENGTH_SHORT).show()
        }
        "export_csv_quick" -> {
            val res = DataExportUtility.exportBillsToCsv(context, bills, localBills)
            Toast.makeText(context, res.getOrElse { "Export failed" }, Toast.LENGTH_LONG).show()
        }
        "export_json_quick" -> {
            val res = DataExportUtility.exportAppBackupBundle(context, bills, localBills)
            Toast.makeText(context, res.getOrElse { "Backup failed" }, Toast.LENGTH_LONG).show()
        }
        "arpic_chime" -> {
            try {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80).startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
            } catch (_: Exception) {}
            Toast.makeText(context, "Audio chime played!", Toast.LENGTH_SHORT).show()
        }
        "jump_search" -> onNavigateToBillsSearch()
        "jump_today" -> onNavigateToCalendarToday()
        "jump_room_vault" -> onNavigateToRoomDb()
        "replay_intro" -> onReplayStartupVideo()
    }
}
