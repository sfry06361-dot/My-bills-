package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.MultiColorPalette

enum class FeatureCategory(val label: String) {
    ALL("All 40"),
    CALCULATORS("Calculators"),
    INSIGHTS("Insights"),
    QUICK_ACTIONS("Quick Actions"),
    PREFERENCES("Toggles")
}

enum class FeatureKind {
    CALCULATOR,
    INSIGHT,
    ACTION,
    TOGGLE
}

data class AppFeatureItem(
    val number: Int,
    val key: String,
    val title: String,
    val subtitle: String,
    val category: FeatureCategory,
    val kind: FeatureKind,
    val icon: ImageVector,
    val accentColor: Color
)

object FeaturesCatalog {
    val fortyFeatures: List<AppFeatureItem> = listOf(
        AppFeatureItem(1, "vat_calc", "1. VAT / Tax Calculator", "Calculate sales tax & total bill amount", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.Percent, MultiColorPalette[0]),
        AppFeatureItem(2, "tip_split", "2. Tip & Service Splitter", "Compute tip % and split per person", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.Splitscreen, MultiColorPalette[1]),
        AppFeatureItem(3, "late_fee", "3. Late Fee Estimator", "Estimate penalty interest on delayed bills", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.Alarm, MultiColorPalette[2]),
        AppFeatureItem(4, "budget_50_30_20", "4. 50/30/20 Budget Rule", "Split monthly income into Needs, Wants & Savings", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.AccountBalance, MultiColorPalette[3]),
        AppFeatureItem(5, "savings_goal", "5. Monthly Savings Target", "Find daily & weekly savings needed", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.Savings, MultiColorPalette[4]),
        AppFeatureItem(6, "annual_projection", "6. Annual Bill Projection", "Project 12-month cost from current bills", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.TrendingUp, MultiColorPalette[5]),
        AppFeatureItem(7, "daily_cost", "7. Daily Burn Rate", "See average cost per day across bills", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.Speed, MultiColorPalette[6]),
        AppFeatureItem(8, "weekly_reserve", "8. Weekly Cash Reserve", "Recommended weekly set-aside for bills", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.DateRange, MultiColorPalette[7]),
        AppFeatureItem(9, "debt_snowball", "9. Debt Snowball Simulator", "Estimate months to pay off a balance", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.Calculate, MultiColorPalette[8]),
        AppFeatureItem(10, "currency_convert", "10. Quick Currency Converter", "Convert USD, EUR, GBP, SAR, AED & EGP", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.CurrencyExchange, MultiColorPalette[9]),
        AppFeatureItem(11, "sample_pack", "11. Add 5 Colorful Demo Bills", "Populate 5 sample bills across categories", FeatureCategory.QUICK_ACTIONS, FeatureKind.ACTION, Icons.Default.AutoAwesome, MultiColorPalette[0]),
        AppFeatureItem(12, "top_expensive", "12. Highest Bills Ranking", "Inspect your largest upcoming expenses", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.ShowChart, MultiColorPalette[1]),
        AppFeatureItem(13, "urgent_48h", "13. 48-Hour Urgent Radar", "List unpaid bills due within 2 days", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.FlashOn, MultiColorPalette[2]),
        AppFeatureItem(14, "overdue_rescue", "14. Overdue Rescue Center", "Inspect & clear overdue bills fast", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.Schedule, MultiColorPalette[3]),
        AppFeatureItem(15, "paid_vault", "15. Paid Receipts Vault", "Review total settled payments", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.CheckCircle, MultiColorPalette[4]),
        AppFeatureItem(16, "recurring_radar", "16. Subscription Inspector", "Audit recurring monthly & yearly bills", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.Repeat, MultiColorPalette[5]),
        AppFeatureItem(17, "instant_bell", "17. Instant Notification Bell", "Fire a live test reminder notification", FeatureCategory.QUICK_ACTIONS, FeatureKind.ACTION, Icons.Default.NotificationsActive, MultiColorPalette[6]),
        AppFeatureItem(18, "top_category", "18. Top Category Spotlight", "Identify your highest-spend category", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.Category, MultiColorPalette[7]),
        AppFeatureItem(19, "small_subs", "19. Micro-Subscription Finder", "Find recurring bills under 25 units", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.Receipt, MultiColorPalette[8]),
        AppFeatureItem(20, "emergency_fund", "20. 6-Month Safety Cushion", "Compute ideal emergency fund from bills", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.Security, MultiColorPalette[9]),
        AppFeatureItem(21, "payday_countdown", "21. Payday Countdown", "Calculate days & budget until next paycheck", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.EventAvailable, MultiColorPalette[0]),
        AppFeatureItem(22, "roommate_split", "22. Roommate Rent Splitter", "Split shared rent & utilities equally", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.Groups, MultiColorPalette[1]),
        AppFeatureItem(23, "discount_calc", "23. Discount & Promo Calc", "Find net price after % early-pay discount", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.LocalOffer, MultiColorPalette[2]),
        AppFeatureItem(24, "kwh_estimator", "24. Electricity kWh Estimator", "Estimate monthly power bill from watts", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.Bolt, MultiColorPalette[3]),
        AppFeatureItem(25, "installment_calc", "25. Installment Plan Split", "Divide an invoice into 3/6/12 months", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.Layers, MultiColorPalette[4]),
        AppFeatureItem(26, "inflation_calc", "26. 5-Year Inflation Forecast", "Estimate future bill cost with inflation %", FeatureCategory.CALCULATORS, FeatureKind.CALCULATOR, Icons.Default.Timeline, MultiColorPalette[5]),
        AppFeatureItem(27, "export_csv_quick", "27. One-Tap CSV Download", "Save all bills straight to Downloads", FeatureCategory.QUICK_ACTIONS, FeatureKind.ACTION, Icons.Default.Download, MultiColorPalette[6]),
        AppFeatureItem(28, "export_json_quick", "28. One-Tap JSON Snapshot", "Save app backup JSON to Downloads", FeatureCategory.QUICK_ACTIONS, FeatureKind.ACTION, Icons.Default.History, MultiColorPalette[7]),
        AppFeatureItem(29, "toggle_high_contrast", "29. Vibrant Border Mode", "Highlight bill cards with vivid borders", FeatureCategory.PREFERENCES, FeatureKind.TOGGLE, Icons.Default.Palette, MultiColorPalette[8]),
        AppFeatureItem(30, "toggle_compact_cards", "30. Compact Card Layout", "Show tighter spacing across bill lists", FeatureCategory.PREFERENCES, FeatureKind.TOGGLE, Icons.Default.FilterList, MultiColorPalette[9]),
        AppFeatureItem(31, "toggle_hide_paid", "31. Hide Settled Bills", "Auto-filter paid bills in quick lists", FeatureCategory.PREFERENCES, FeatureKind.TOGGLE, Icons.Default.VisibilityOff, MultiColorPalette[0]),
        AppFeatureItem(32, "toggle_weekend_warn", "32. Weekend Deadline Shield", "Flag bills falling on Fri/Sat/Sun", FeatureCategory.PREFERENCES, FeatureKind.TOGGLE, Icons.Default.CalendarToday, MultiColorPalette[1]),
        AppFeatureItem(33, "toggle_vibrate", "33. Haptic Alert Feedback", "Enable vibration on notification cues", FeatureCategory.PREFERENCES, FeatureKind.TOGGLE, Icons.Default.NotificationsActive, MultiColorPalette[2]),
        AppFeatureItem(34, "arpic_chime", "34. Play Audio Chime Cue", "Play pleasant confirmation chime tone", FeatureCategory.QUICK_ACTIONS, FeatureKind.ACTION, Icons.Default.VolumeUp, MultiColorPalette[3]),
        AppFeatureItem(35, "jump_search", "35. Instant Bill Search", "Jump straight to Bills search bar", FeatureCategory.QUICK_ACTIONS, FeatureKind.ACTION, Icons.Default.Search, MultiColorPalette[4]),
        AppFeatureItem(36, "jump_today", "36. Calendar Today Focus", "Jump directly to today on Calendar", FeatureCategory.QUICK_ACTIONS, FeatureKind.ACTION, Icons.Default.CalendarToday, MultiColorPalette[5]),
        AppFeatureItem(37, "jump_room_vault", "37. Open Offline Room Vault", "Switch directly to local SQLite Room DB", FeatureCategory.QUICK_ACTIONS, FeatureKind.ACTION, Icons.Default.Storage, MultiColorPalette[6]),
        AppFeatureItem(38, "replay_intro", "38. Replay Welcome Video", "Watch the animated onboarding video again", FeatureCategory.QUICK_ACTIONS, FeatureKind.ACTION, Icons.Default.PlayCircle, MultiColorPalette[7]),
        AppFeatureItem(39, "privacy_audit", "39. Privacy & Permissions Audit", "Verify zero storage permissions & isolation", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.VerifiedUser, MultiColorPalette[8]),
        AppFeatureItem(40, "system_diagnostics", "40. App Health & Diagnostics", "Inspect sync status, DB counts & BG engine", FeatureCategory.INSIGHTS, FeatureKind.INSIGHT, Icons.Default.Info, MultiColorPalette[9])
    )
}
