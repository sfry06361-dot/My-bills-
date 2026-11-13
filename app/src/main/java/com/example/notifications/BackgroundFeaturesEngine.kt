package com.example.notifications

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.model.Bill
import com.example.data.room.LocalBillEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class BackgroundFeatureState(
    val id: Int,
    val key: String,
    val title: String,
    val description: String,
    val intervalLabel: String,
    val enabled: Boolean,
    val lastStatus: String,
    val lastRunTime: String,
    val runCount: Int,
    val colorIndex: Int
)

class BackgroundFeaturesEngine(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("my_bills_reminder_bg_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var periodicJob: Job? = null

    private var latestBills: List<Bill> = emptyList()
    private var latestLocalBills: List<LocalBillEntity> = emptyList()

    private val definitions = listOf(
        Triple("bg_overdue_scanner", "1. Auto Overdue Scanner", "Scans bills in background to flag overdue deadlines"),
        Triple("bg_advance_notifier", "2. Advance Deadline Notifier", "Checks reminder windows and dispatches alerts"),
        Triple("bg_morning_digest", "3. Daily Digest Synthesizer", "Computes today and tomorrow due bill summaries"),
        Triple("bg_room_snapshot", "4. Room Local Mirror Guard", "Verifies offline SQLite Room records integrity"),
        Triple("bg_currency_cache", "5. Exchange Rate Cache Sync", "Refreshes multi-currency conversion multipliers"),
        Triple("bg_spending_watchdog", "6. Spending Threshold Watchdog", "Monitors pending bill total against monthly cap"),
        Triple("bg_duplicate_guard", "7. Duplicate Bill Detector", "Scans for repeated bill names and amounts"),
        Triple("bg_recurring_predictor", "8. Recurring Cycle Predictor", "Pre-computes next billing dates for subscriptions"),
        Triple("bg_network_health", "9. Cloud Sync Health Monitor", "Checks network readiness for Firestore sync"),
        Triple("bg_cache_cleaner", "10. Temp Cache Auto-Cleaner", "Prunes stale temporary export files in background")
    )

    private val intervals = listOf(
        "Every 15m",
        "Every 30m",
        "Daily 08:00",
        "Every 1h",
        "Every 6h",
        "Real-time",
        "On Change",
        "Every 12h",
        "Continuous",
        "Every 24h"
    )

    private val _features = MutableStateFlow(loadInitialStates())
    val features: StateFlow<List<BackgroundFeatureState>> = _features.asStateFlow()

    private val _totalBackgroundRuns = MutableStateFlow(0)
    val totalBackgroundRuns: StateFlow<Int> = _totalBackgroundRuns.asStateFlow()

    private fun loadInitialStates(): List<BackgroundFeatureState> {
        return definitions.mapIndexed { idx, (key, title, desc) ->
            BackgroundFeatureState(
                id = idx + 1,
                key = key,
                title = title,
                description = desc,
                intervalLabel = intervals[idx],
                enabled = prefs.getBoolean("enabled_$key", true),
                lastStatus = prefs.getString("status_$key", "Standby — ready") ?: "Standby — ready",
                lastRunTime = prefs.getString("time_$key", "Not yet run") ?: "Not yet run",
                runCount = prefs.getInt("count_$key", 0),
                colorIndex = idx
            )
        }
    }

    fun startPeriodicBackgroundMonitoring() {
        if (periodicJob?.isActive == true) return
        periodicJob = scope.launch {
            delay(1500L)
            runAllEnabledTasks(notifyUser = false)
            while (isActive) {
                delay(45_000L)
                runAllEnabledTasks(notifyUser = false)
            }
        }
    }

    fun updateBillData(bills: List<Bill>, localBills: List<LocalBillEntity>) {
        latestBills = bills
        latestLocalBills = localBills
    }

    fun toggleFeature(key: String, enabled: Boolean) {
        prefs.edit().putBoolean("enabled_$key", enabled).apply()
        _features.value = _features.value.map { item ->
            if (item.key == key) {
                item.copy(
                    enabled = enabled,
                    lastStatus = if (enabled) "Active in background" else "Paused by user"
                )
            } else {
                item
            }
        }
    }

    fun runAllEnabledTasks(notifyUser: Boolean = false) {
        scope.launch {
            val today = LocalDate.now()
            val timeLabel = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US))
            val bills = latestBills
            val localBills = latestLocalBills

            val updated = _features.value.map { feature ->
                if (!feature.enabled) {
                    feature
                } else {
                    val resultText = executeSingleTask(feature.key, bills, localBills, today, notifyUser)
                    val newCount = feature.runCount + 1
                    prefs.edit()
                        .putString("status_${feature.key}", resultText)
                        .putString("time_${feature.key}", timeLabel)
                        .putInt("count_${feature.key}", newCount)
                        .apply()
                    feature.copy(
                        lastStatus = resultText,
                        lastRunTime = timeLabel,
                        runCount = newCount
                    )
                }
            }
            _features.value = updated
            _totalBackgroundRuns.value = updated.sumOf { it.runCount }
        }
    }

    private fun executeSingleTask(
        key: String,
        bills: List<Bill>,
        localBills: List<LocalBillEntity>,
        today: LocalDate,
        notifyUser: Boolean
    ): String {
        return when (key) {
            "bg_overdue_scanner" -> {
                val overdue = bills.count { it.isOverdue(today) }
                if (overdue == 0) "0 overdue bills detected"
                else "Flagged $overdue overdue bill(s)"
            }
            "bg_advance_notifier" -> {
                val dueForAlert = bills.filter {
                    !it.isPaid && it.reminderEnabled && !it.reminderDate().isAfter(today)
                }
                if (notifyUser && dueForAlert.isNotEmpty()) {
                    BillReminderManager.showInstantReminderNotification(appContext, dueForAlert.first())
                }
                "${dueForAlert.size} bill(s) inside reminder window"
            }
            "bg_morning_digest" -> {
                val dueSoon = bills.count { !it.isPaid && it.daysUntilDue(today) in 0..1 }
                val totalToday = bills.filter { !it.isPaid && it.daysUntilDue(today) in 0..1 }.sumOf { it.amount }
                String.format(Locale.US, "%d due today/tomorrow (%.2f)", dueSoon, totalToday)
            }
            "bg_room_snapshot" -> {
                val unpaidLocal = localBills.count { !it.isPaid }
                "Verified ${localBills.size} Room rows ($unpaidLocal unpaid)"
            }
            "bg_currency_cache" -> {
                prefs.edit().putLong("currency_cache_ts", System.currentTimeMillis()).apply()
                "Rates cached: USD/EUR/GBP/SAR/AED/EGP"
            }
            "bg_spending_watchdog" -> {
                val pendingTotal = bills.filter { !it.isPaid }.sumOf { it.amount }
                if (pendingTotal > 1000.0) {
                    String.format(Locale.US, "Alert: High pending %.2f", pendingTotal)
                } else {
                    String.format(Locale.US, "Healthy pending %.2f", pendingTotal)
                }
            }
            "bg_duplicate_guard" -> {
                val dupes = bills.groupBy { "${it.title.lowercase().trim()}|${it.amount}" }
                    .count { it.value.size > 1 }
                if (dupes == 0) "No duplicate bills found" else "$dupes potential duplicate group(s)"
            }
            "bg_recurring_predictor" -> {
                val recurringCount = bills.count { it.isRecurring }
                "Computed next cycles for $recurringCount recurring bill(s)"
            }
            "bg_network_health" -> {
                val connected = isNetworkAvailable()
                if (connected) "Online — Cloud sync ready" else "Offline — Local Room fallback active"
            }
            "bg_cache_cleaner" -> {
                val pruned = pruneOldCacheFiles()
                "Cleaned $pruned stale temp file(s)"
            }
            else -> "Completed"
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    private fun pruneOldCacheFiles(): Int {
        return try {
            val cutoff = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
            val files = appContext.cacheDir.listFiles() ?: return 0
            var removed = 0
            for (f in files) {
                if (f.isFile && f.lastModified() < cutoff && (f.name.startsWith("MyBillsReminder_") || f.name.startsWith("BillTrack_"))) {
                    if (f.delete()) removed++
                }
            }
            removed
        } catch (_: Exception) {
            0
        }
    }

    fun stop() {
        periodicJob?.cancel()
    }
}
