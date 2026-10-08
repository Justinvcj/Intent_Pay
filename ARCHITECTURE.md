# Intent Pay — Architecture, Implementation Plan & Design System

> **Version:** 2.0 — Full Vision Blueprint  
> **Last Updated:** August 2026  
> **Stack:** Kotlin · Jetpack Compose · Room · WorkManager · Google Drive API  
> **Design Language:** Glassmorphism + Apple Liquid Glass Aesthetic  
> **Budget:** $0 — 100% Open Source

---

## Table of Contents

1. [Vision & Philosophy](#1-vision--philosophy)
2. [System Architecture](#2-system-architecture)
3. [Module Breakdown](#3-module-breakdown)
4. [Data Layer — Deep Dive](#4-data-layer--deep-dive)
5. [Transaction Detection Pipeline](#5-transaction-detection-pipeline)
6. [Smart Categorization Engine](#6-smart-categorization-engine)
7. [Reminder & Notification System](#7-reminder--notification-system)
8. [Reports & Analytics Engine](#8-reports--analytics-engine)
9. [Export & Cloud Sync](#9-export--cloud-sync)
10. [UI Design System](#10-ui-design-system)
11. [Screen-by-Screen Blueprint](#11-screen-by-screen-blueprint)
12. [Implementation Roadmap](#12-implementation-roadmap)
13. [Tech Stack & Dependencies](#13-tech-stack--dependencies)
14. [Testing Strategy](#14-testing-strategy)

---

## 1. Vision & Philosophy

Intent Pay is not another expense tracker. It's a **post-payment intelligence layer** that sits on top of any UPI app on your Android phone. You pay with GPay, PhonePe, Paytm — whatever you want. Intent Pay wakes up *after* the payment, captures it, categorizes it, learns your habits, and gives you clarity on where your money actually goes.

**Core Principles:**

- **Zero-friction capture** — the app detects payments automatically. You never open the app to "log" something.
- **Offline-first, privacy-first** — all data lives on your device. No accounts, no servers, no analytics tracking you.
- **Learn once, remember forever** — categorize Zomato as "Food" once, and it stays Food forever.
- **Gentle persistence** — if you skip the categorization popup, the app reminds you at end-of-day, then at 3 days. It doesn't nag, but it doesn't forget.
- **Your data, your format** — export to any format, including custom Excel/Sheets templates you define.

---

## 2. System Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                        DETECTION LAYER                               │
│                                                                      │
│   ┌─────────────────┐    ┌──────────────────────┐                   │
│   │  SMS Broadcast   │    │  Notification         │                   │
│   │  Receiver        │    │  Listener Service     │                   │
│   │  (Bank SMS)      │    │  (GPay/PhonePe/Paytm) │                   │
│   └────────┬─────────┘    └──────────┬────────────┘                   │
│            │                         │                                │
│            └────────────┬────────────┘                                │
│                         ▼                                             │
│              ┌─────────────────────┐                                  │
│              │  Unified Transaction │                                  │
│              │  Parser              │                                  │
│              └──────────┬──────────┘                                   │
└─────────────────────────┼────────────────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     INTELLIGENCE LAYER                                │
│                                                                      │
│   ┌──────────────────┐   ┌──────────────────┐                        │
│   │  Deduplication    │   │  Merchant Memory  │                        │
│   │  Engine           │──▶│  (Auto-Categorize) │                        │
│   └────────┬─────────┘   └────────┬─────────┘                        │
│            │                      │                                   │
│            ▼                      ▼                                   │
│   ┌──────────────────────────────────────────┐                        │
│   │         Room Database (SSOT)              │                        │
│   │  transactions │ merchant_rules │ budgets  │                        │
│   └──────────────────────┬───────────────────┘                        │
│                          │                                            │
│   ┌──────────────────┐   │   ┌──────────────────┐                    │
│   │  Reminder         │   │   │  Report           │                    │
│   │  Scheduler        │◀──┤   │  Generator        │                    │
│   │  (WorkManager)    │   │   │  (Weekly/Monthly)  │                    │
│   └──────────────────┘   │   └──────────────────┘                    │
└──────────────────────────┼───────────────────────────────────────────┘
                           ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                                │
│                                                                      │
│   ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐       │
│   │  Dashboard  │ │ Reflection │ │  Reports   │ │  Settings  │       │
│   │  Screen     │ │ Overlay    │ │  Screen    │ │  Screen    │       │
│   └────────────┘ └────────────┘ └────────────┘ └────────────┘       │
│                                                                      │
│   ┌────────────┐ ┌────────────┐ ┌────────────┐                      │
│   │  Manual     │ │  Export    │ │  Insights  │                      │
│   │  Entry      │ │  Wizard   │ │  Screen    │                      │
│   └────────────┘ └────────────┘ └────────────┘                      │
│                                                                      │
│              Jetpack Compose + Glassmorphism Design System            │
└──────────────────────────────────────────────────────────────────────┘
                           ▼
┌──────────────────────────────────────────────────────────────────────┐
│                        SYNC LAYER (Optional)                         │
│                                                                      │
│   ┌──────────────────┐   ┌──────────────────┐                        │
│   │  Google Drive     │   │  Custom Template  │                        │
│   │  Backup Worker    │   │  Export Engine     │                        │
│   └──────────────────┘   └──────────────────┘                        │
└──────────────────────────────────────────────────────────────────────┘
```

**Architecture Pattern:** Clean Architecture (MVVM) with Unidirectional Data Flow

```
UI (Compose) → ViewModel → UseCase → Repository → DataSource (Room / Preferences)
     ▲                                                    │
     └────────────── StateFlow / Flow ────────────────────┘
```

---

## 3. Module Breakdown

### Package Structure

```
com.intentpay/
├── IntentPayApplication.kt
│
├── data/
│   ├── local/
│   │   ├── IntentPayDatabase.kt
│   │   ├── TransactionEntity.kt
│   │   ├── TransactionDao.kt
│   │   ├── BudgetEntity.kt
│   │   ├── BudgetDao.kt
│   │   ├── MerchantRuleEntity.kt          ← NEW
│   │   ├── MerchantRuleDao.kt             ← NEW
│   │   ├── ReminderEntity.kt              ← NEW
│   │   └── ReminderDao.kt                 ← NEW
│   │
│   ├── detection/                          ← RENAMED from parser/
│   │   ├── SmsReceiver.kt                  (existing, enhanced)
│   │   ├── SmsParser.kt                    (existing, enhanced)
│   │   ├── NotificationListener.kt         ← NEW
│   │   ├── NotificationParser.kt           ← NEW
│   │   ├── UnifiedTransactionDetector.kt   ← NEW
│   │   └── DeduplicationEngine.kt          ← NEW (extracted from repo)
│   │
│   ├── categorization/                     ← NEW
│   │   ├── MerchantMemory.kt
│   │   ├── CategoryResolver.kt
│   │   └── KeywordRules.kt
│   │
│   ├── export/                             ← NEW
│   │   ├── ExportManager.kt
│   │   ├── CsvExporter.kt
│   │   ├── JsonExporter.kt
│   │   ├── ExcelExporter.kt
│   │   ├── TemplateParser.kt
│   │   └── GoogleDriveSync.kt
│   │
│   ├── repository/
│   │   ├── TransactionRepository.kt        (replaces ExpenseRepository)
│   │   ├── BudgetRepository.kt             ← NEW (split)
│   │   ├── MerchantRepository.kt           ← NEW
│   │   └── ReminderRepository.kt           ← NEW
│   │
│   └── worker/                             ← NEW
│       ├── EndOfDayReminderWorker.kt
│       ├── ThreeDayReminderWorker.kt
│       ├── WeeklyReportWorker.kt
│       ├── GoogleDriveSyncWorker.kt
│       └── WorkerScheduler.kt
│
├── domain/
│   ├── models/
│   │   ├── Category.kt                     (enhanced)
│   │   ├── Transaction.kt                  ← NEW (domain model)
│   │   ├── SpendingSummary.kt              ← NEW
│   │   ├── WeeklyReport.kt                ← NEW
│   │   └── ExportTemplate.kt              ← NEW
│   │
│   └── usecases/
│       ├── CategorizeTransactionUseCase.kt  ← NEW
│       ├── GenerateReportUseCase.kt         ← NEW
│       ├── ExportTransactionsUseCase.kt     (enhanced)
│       ├── GetSpendingInsightsUseCase.kt    ← NEW
│       └── ScheduleReminderUseCase.kt       ← NEW
│
└── ui/
    ├── MainActivity.kt
    ├── navigation/
    │   └── IntentPayNavGraph.kt             ← NEW (extracted)
    │
    ├── designsystem/                        ← NEW
    │   ├── GlassCard.kt
    │   ├── GlassBottomBar.kt
    │   ├── GlassTopBar.kt
    │   ├── GlassButton.kt
    │   ├── GlassTextField.kt
    │   ├── GlassSurface.kt
    │   ├── AnimatedCounter.kt
    │   ├── CategoryChip.kt
    │   ├── TransactionTile.kt
    │   ├── SpendingRing.kt
    │   └── GradientMesh.kt
    │
    ├── theme/
    │   ├── Color.kt                         (enhanced)
    │   ├── Theme.kt                         (enhanced)
    │   ├── Type.kt                          (enhanced)
    │   ├── Spacing.kt                       ← NEW
    │   ├── GlassTokens.kt                  ← NEW
    │   └── Animation.kt                    ← NEW
    │
    ├── splash/
    │   └── SplashScreen.kt
    │
    ├── dashboard/
    │   ├── DashboardScreen.kt               (redesigned)
    │   └── DashboardViewModel.kt            (enhanced)
    │
    ├── reflection/
    │   ├── ReflectionActivity.kt
    │   ├── ReflectionScreen.kt              (redesigned)
    │   └── ReflectionViewModel.kt
    │
    ├── insights/                            ← NEW
    │   ├── InsightsScreen.kt
    │   └── InsightsViewModel.kt
    │
    ├── reports/                             ← NEW
    │   ├── WeeklyReportScreen.kt
    │   └── ReportViewModel.kt
    │
    ├── export/                              ← NEW
    │   ├── ExportScreen.kt
    │   └── ExportViewModel.kt
    │
    ├── manualpayment/
    │   ├── ManualPaymentScreen.kt           (redesigned)
    │   └── ManualPaymentViewModel.kt
    │
    ├── history/                             ← NEW
    │   ├── HistoryScreen.kt
    │   └── HistoryViewModel.kt
    │
    ├── settings/
    │   ├── SettingsScreen.kt                (redesigned)
    │   └── SettingsViewModel.kt
    │
    └── qrscanner/
        └── QrScannerScreen.kt
```

---

## 4. Data Layer — Deep Dive

### 4.1 Room Database Schema

#### `transactions` (enhanced)

| Column | Type | Notes |
|--------|------|-------|
| id | LONG (PK, auto) | — |
| amount | DOUBLE | — |
| merchant | TEXT? | raw merchant string from SMS/notification |
| normalizedMerchant | TEXT? | **NEW** — lowercase trimmed key for rule matching |
| dateMillis | LONG | — |
| purpose | TEXT? | user-provided justification |
| categoryName | TEXT | enum name from Category |
| upiRefId | TEXT? | 12-digit UPI reference |
| autoDetected | BOOLEAN | true = SMS/notification, false = manual |
| isIncome | BOOLEAN | — |
| source | TEXT | **NEW** — `SMS`, `NOTIFICATION`, `MANUAL` |
| needsReview | BOOLEAN | **NEW** — true if user hasn't categorized yet |
| reminderCount | INT | **NEW** — how many times we've reminded about this |
| syncedWithDrive | BOOLEAN | **NEW** — replaces syncedWithSheets |

#### `merchant_rules` (NEW)

| Column | Type | Notes |
|--------|------|-------|
| id | LONG (PK, auto) | — |
| merchantPattern | TEXT (UNIQUE) | normalized merchant string (e.g., `zomato`, `swiggy`) |
| categoryName | TEXT | the category this merchant maps to |
| timesUsed | INT | confidence counter — how many times this rule was applied |
| lastUsedMillis | LONG | — |
| createdByUser | BOOLEAN | true = user explicitly set this, false = auto-inferred |

#### `budget_config` (enhanced)

| Column | Type | Notes |
|--------|------|-------|
| id | INT (PK, singleton=1) | — |
| monthlyBudget | DOUBLE | — |
| dailyLimit | DOUBLE | — |
| reflectionThreshold | DOUBLE | — |
| endOfDayReminderHour | INT | **NEW** — default 21 (9 PM) |
| endOfDayReminderMinute | INT | **NEW** — default 0 |
| enableThreeDayReminder | BOOLEAN | **NEW** — default true |
| enableWeeklyReport | BOOLEAN | **NEW** — default true |
| weeklyReportDay | INT | **NEW** — Calendar.SUNDAY = 1 |
| googleDriveFolderId | TEXT? | **NEW** — replaces googleSheetId |
| exportTemplatePath | TEXT? | **NEW** — path to custom template file |

#### `pending_reviews` (NEW — virtual via query)

Not a separate table. A DAO query that returns all transactions where `needsReview = true`, ordered by `reminderCount ASC, dateMillis DESC`. This powers the end-of-day and 3-day reminder screens.

### 4.2 Key DAO Additions

```kotlin
// MerchantRuleDao.kt
@Dao
interface MerchantRuleDao {
    @Query("SELECT * FROM merchant_rules WHERE merchantPattern = :pattern LIMIT 1")
    suspend fun findRule(pattern: String): MerchantRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: MerchantRuleEntity)

    @Query("UPDATE merchant_rules SET timesUsed = timesUsed + 1, lastUsedMillis = :now WHERE merchantPattern = :pattern")
    suspend fun incrementUsage(pattern: String, now: Long)

    @Query("SELECT * FROM merchant_rules ORDER BY timesUsed DESC")
    fun getAllRules(): Flow<List<MerchantRuleEntity>>
}

// TransactionDao additions
@Query("SELECT * FROM transactions WHERE needsReview = 1 ORDER BY dateMillis DESC")
fun getUncategorized(): Flow<List<TransactionEntity>>

@Query("SELECT COUNT(*) FROM transactions WHERE needsReview = 1")
fun getUncategorizedCount(): Flow<Int>

@Query("SELECT categoryName, SUM(amount) as total FROM transactions WHERE isIncome = 0 AND dateMillis BETWEEN :start AND :end GROUP BY categoryName")
fun getSpendingByCategory(start: Long, end: Long): Flow<List<CategorySpending>>

@Query("SELECT * FROM transactions WHERE isIncome = 0 AND dateMillis BETWEEN :start AND :end ORDER BY amount DESC LIMIT :limit")
fun getTopSpends(start: Long, end: Long, limit: Int = 5): Flow<List<TransactionEntity>>
```

### 4.3 Data Class for Aggregations

```kotlin
data class CategorySpending(
    val categoryName: String,
    val total: Double
)

data class DailySpending(
    val dayMillis: Long,
    val total: Double
)
```

---

## 5. Transaction Detection Pipeline

### 5.1 Dual-Source Detection

The app uses **two parallel detection channels** for maximum coverage:

#### Channel 1: SMS BroadcastReceiver (existing, enhanced)

Listens for `android.provider.Telephony.SMS_RECEIVED`. Parses bank transaction SMS using regex patterns.

**Enhanced SmsParser** — support more banks and formats:

```kotlin
object SmsParser {
    // Amount patterns: "Rs.500", "INR 1,200.50", "₹500", "Rs 500.00"
    private val AMOUNT_PATTERNS = listOf(
        Regex("(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+\\.?\\d*)"),
        Regex("(?i)amount\\s*(?:of)?\\s*(?:rs\\.?|inr|₹)?\\s*([\\d,]+\\.?\\d*)")
    )

    // Merchant patterns: "to MERCHANT", "at MERCHANT", "paid to MERCHANT"
    private val MERCHANT_PATTERNS = listOf(
        Regex("(?i)(?:to|at|paid to|transferred to)\\s+([a-zA-Z0-9@._\\s-]+?)(?:\\s+(?:on|ref|upi|via|$))"),
        Regex("(?i)(?:at)\\s+([A-Z][A-Za-z0-9\\s]+?)(?:\\s+on)")
    )

    // UPI Ref: 12-digit reference number
    private val UPI_REF_PATTERNS = listOf(
        Regex("(?i)(?:upi|txn|ref)\\s*(?:no\\.?|id|ref)?\\s*:?\\s*(\\d{12})"),
        Regex("(?i)(\\d{12})")
    )
}
```

#### Channel 2: NotificationListenerService (NEW — primary channel)

This is **faster and more reliable** than SMS. UPI apps push a notification the instant a payment completes, often before the bank SMS arrives.

```kotlin
class PaymentNotificationListener : NotificationListenerService() {

    // Package names to monitor
    private val UPI_PACKAGES = setOf(
        "com.google.android.apps.nbu.paisa.user",  // GPay
        "com.phonepe.app",                           // PhonePe
        "net.one97.paytm",                           // Paytm
        "in.org.npci.upiapp",                        // BHIM
        "com.cred.members",                          // CRED
        "com.amazon.mShop.android.shopping",          // Amazon Pay
        "app.navi.android",                           // Navi
        "com.whatsapp"                                // WhatsApp Pay
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in UPI_PACKAGES) return

        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val fullText = "$title $text $bigText"
        val parsed = NotificationParser.parse(sbn.packageName, fullText)

        if (parsed != null) {
            // Feed into UnifiedTransactionDetector
        }
    }
}
```

#### NotificationParser

```kotlin
object NotificationParser {
    fun parse(packageName: String, text: String): ParsedTransaction? {
        val lowerText = text.lowercase()

        // Check if it's a payment notification (not a promotional one)
        val isPayment = lowerText.containsAny("paid", "sent", "debited", "received",
            "credited", "payment of", "transferred")
        if (!isPayment) return null

        // Extract amount
        val amount = extractAmount(lowerText) ?: return null

        // Extract merchant — different apps format differently
        val merchant = extractMerchant(packageName, text)

        // Determine direction
        val isIncome = lowerText.containsAny("received", "credited", "added")

        return ParsedTransaction(
            amount = amount,
            merchant = merchant,
            isIncome = isIncome,
            source = DetectionSource.NOTIFICATION,
            sourcePackage = packageName
        )
    }
}
```

### 5.2 Unified Transaction Detector

Merges both channels, deduplicates, and feeds into the categorization engine.

```kotlin
class UnifiedTransactionDetector(
    private val transactionRepo: TransactionRepository,
    private val merchantMemory: MerchantMemory,
    private val budgetRepo: BudgetRepository
) {
    suspend fun process(parsed: ParsedTransaction): Long {
        // 1. Deduplicate — check if same amount ± 2 min window exists
        val existing = transactionRepo.findDuplicate(
            amount = parsed.amount,
            windowMillis = 120_000
        )
        if (existing != null) return existing.id

        // 2. Auto-categorize via Merchant Memory
        val category = merchantMemory.resolve(parsed.merchant)

        // 3. Build entity
        val entity = TransactionEntity(
            amount = parsed.amount,
            merchant = parsed.merchant,
            normalizedMerchant = parsed.merchant?.normalize(),
            dateMillis = System.currentTimeMillis(),
            categoryName = category.name,
            autoDetected = true,
            isIncome = parsed.isIncome,
            source = parsed.source.name,
            needsReview = (category == Category.UNCATEGORIZED),
            reminderCount = 0
        )

        // 4. Insert
        val id = transactionRepo.insert(entity)

        // 5. Trigger reflection if above threshold
        val threshold = budgetRepo.getThreshold()
        if (parsed.amount > threshold && !parsed.isIncome) {
            triggerReflectionOverlay(id, parsed.amount)
        }

        return id
    }
}
```

### 5.3 Deduplication Strategy

Because both SMS and notification can fire for the same payment:

```
Payment → Notification arrives (T+0s) → saved with upiRef or amount+time fingerprint
       → SMS arrives (T+5-30s) → dedup check finds match → skip or merge missing fields
```

The dedup window is **2 minutes**. Matching is done on:
1. Exact UPI reference match (if both sources have it)
2. Same amount within the time window (fallback)

---

## 6. Smart Categorization Engine

### 6.1 Merchant Memory

The core idea: **categorize once, remember forever.**

```kotlin
class MerchantMemory(
    private val merchantRuleDao: MerchantRuleDao
) {
    // In-memory cache for fast lookups
    private val cache = mutableMapOf<String, Category>()

    suspend fun resolve(rawMerchant: String?): Category {
        if (rawMerchant == null) return Category.UNCATEGORIZED

        val normalized = rawMerchant.normalize()

        // 1. Check cache
        cache[normalized]?.let { return it }

        // 2. Check database
        val rule = merchantRuleDao.findRule(normalized)
        if (rule != null) {
            cache[normalized] = Category.valueOf(rule.categoryName)
            merchantRuleDao.incrementUsage(normalized, System.currentTimeMillis())
            return Category.valueOf(rule.categoryName)
        }

        // 3. Check keyword rules (built-in heuristics)
        val keywordMatch = KeywordRules.match(normalized)
        if (keywordMatch != null) {
            // Auto-save this as a rule
            learn(normalized, keywordMatch, createdByUser = false)
            return keywordMatch
        }

        return Category.UNCATEGORIZED
    }

    suspend fun learn(normalizedMerchant: String, category: Category, createdByUser: Boolean) {
        val rule = MerchantRuleEntity(
            merchantPattern = normalizedMerchant,
            categoryName = category.name,
            timesUsed = 1,
            lastUsedMillis = System.currentTimeMillis(),
            createdByUser = createdByUser
        )
        merchantRuleDao.insertOrUpdate(rule)
        cache[normalizedMerchant] = category
    }
}
```

### 6.2 Built-in Keyword Rules

```kotlin
object KeywordRules {
    private val RULES: Map<List<String>, Category> = mapOf(
        listOf("zomato", "swiggy", "dominos", "pizza", "burger", "food",
               "restaurant", "cafe", "bakery", "kitchen", "biryani",
               "mcdonalds", "kfc", "starbucks", "chai") to Category.FOOD,

        listOf("uber", "ola", "rapido", "irctc", "redbus", "metro",
               "petrol", "diesel", "fuel", "parking", "toll",
               "makemytrip", "goibibo", "cleartrip") to Category.TRAVEL,

        listOf("jio", "airtel", "vi", "bsnl", "broadband", "wifi",
               "electricity", "water", "gas", "rent", "insurance",
               "emi", "loan") to Category.BILLS,

        listOf("amazon", "flipkart", "myntra", "ajio", "nykaa",
               "meesho", "shopping", "mall") to Category.SHOPPING,

        listOf("netflix", "hotstar", "spotify", "prime", "youtube",
               "movie", "cinema", "pvr", "inox", "game") to Category.ENTERTAINMENT,

        listOf("apollo", "pharmeasy", "1mg", "netmeds", "hospital",
               "doctor", "clinic", "medical", "lab", "diagnostic") to Category.HEALTH,

        listOf("udemy", "coursera", "book", "library", "tuition",
               "college", "school", "exam", "coaching") to Category.STUDY,

        listOf("gym", "salon", "spa", "parlour", "grooming",
               "luxury", "jewel", "watch", "perfume") to Category.LUXURY
    )

    fun match(normalizedMerchant: String): Category? {
        for ((keywords, category) in RULES) {
            if (keywords.any { normalizedMerchant.contains(it) }) {
                return category
            }
        }
        return null
    }
}
```

### 6.3 Enhanced Category Enum

```kotlin
enum class Category(
    val displayName: String,
    val emoji: String,
    val colorHex: Long
) {
    FOOD("Food", "🍕", 0xFFFF6B35),
    TRAVEL("Travel", "🚗", 0xFF4ECDC4),
    BILLS("Bills", "📄", 0xFFFFE66D),
    STUDY("Study", "📚", 0xFF95E1D3),
    SHOPPING("Shopping", "🛍️", 0xFFFF85A1),
    ENTERTAINMENT("Entertainment", "🎬", 0xFFB388FF),
    HEALTH("Health", "💊", 0xFF69F0AE),
    LUXURY("Luxury", "✨", 0xFFFFD700),
    TRANSFER("Transfer", "↔️", 0xFF90CAF9),
    OTHER("Other", "📌", 0xFFBDBDBD),
    UNCATEGORIZED("Uncategorized", "❓", 0xFF757575)
}
```

---

## 7. Reminder & Notification System

### 7.1 End-of-Day Reminder

Fires at the user's configured time (default 9 PM). Shows a notification with the count of uncategorized transactions from today.

```kotlin
class EndOfDayReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = /* get from app */
        val uncategorizedToday = repo.getUncategorizedSince(todayStartMillis())

        if (uncategorizedToday.isNotEmpty()) {
            val count = uncategorizedToday.size
            val total = uncategorizedToday.sumOf { it.amount }

            showNotification(
                title = "₹${total.format()} uncategorized today",
                body = "$count payment${if (count > 1) "s" else ""} need your attention",
                intent = buildBatchCategorizeIntent(uncategorizedToday.map { it.id })
            )

            // Increment reminder count
            repo.incrementReminderCount(uncategorizedToday.map { it.id })
        }
        return Result.success()
    }
}
```

**Scheduling** (via WorkManager):

```kotlin
object WorkerScheduler {
    fun scheduleEndOfDay(context: Context, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val request = OneTimeWorkRequestBuilder<EndOfDayReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("end_of_day_reminder")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("end_of_day", ExistingWorkPolicy.REPLACE, request)
    }
}
```

### 7.2 Three-Day Reminder

For transactions that have been uncategorized for 3+ days:

```kotlin
class ThreeDayReminderWorker(...) : CoroutineWorker(...) {
    override suspend fun doWork(): Result {
        val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
        val stale = repo.getUncategorizedBefore(threeDaysAgo)

        if (stale.isNotEmpty()) {
            showNotification(
                title = "${stale.size} old payments still uncategorized",
                body = "Tap to categorize them before you forget",
                intent = buildBatchCategorizeIntent(stale.map { it.id })
            )
        }
        return Result.success()
    }
}
```

### 7.3 Batch Categorize Screen

When the user taps a reminder notification, they land on a **swipeable card stack** showing each uncategorized transaction one-by-one. They select a category, optionally type a purpose, and swipe to the next. Fast and satisfying.

---

## 8. Reports & Analytics Engine

### 8.1 Weekly Report

Generated every Sunday (configurable). Contains:

```kotlin
data class WeeklyReport(
    val weekStartMillis: Long,
    val weekEndMillis: Long,
    val totalSpent: Double,
    val totalIncome: Double,
    val netFlow: Double,
    val categoryBreakdown: List<CategorySpending>,
    val topMerchants: List<MerchantSpending>,
    val dailyTrend: List<DailySpending>,
    val comparisonWithLastWeek: Double,       // percentage change
    val budgetUtilization: Double,            // percentage of monthly budget used
    val uncategorizedCount: Int,
    val biggestSingleSpend: TransactionEntity?
)
```

The weekly report is:
1. Pushed as a **rich notification** with key stats
2. Viewable as a **full-screen report** inside the app
3. Optionally **exported as PDF/image** for sharing

### 8.2 Insights Engine

Real-time insights surfaced on the dashboard:

```kotlin
class GetSpendingInsightsUseCase(private val repo: TransactionRepository) {

    // "You've spent 40% more on Food this week compared to last week"
    suspend fun getCategoryTrends(): List<Insight>

    // "Your biggest spend today was ₹1200 at Croma"
    suspend fun getDailyHighlight(): Insight?

    // "You're on track — 45% of monthly budget used with 15 days left"
    suspend fun getBudgetHealth(): BudgetInsight

    // "Zomato is your most frequent merchant this month (12 times)"
    suspend fun getTopMerchant(): MerchantInsight?
}
```

### 8.3 Data Visualizations

All charts built with **Compose Canvas** (no external chart library needed):

- **Spending Ring** — donut chart showing category breakdown with animated segments
- **Weekly Bar Chart** — 7 bars for daily spending, current day highlighted
- **Monthly Trend Line** — line graph of daily cumulative spending vs. budget line
- **Category Cards** — horizontal scroll of category totals with mini progress bars

---

## 9. Export & Cloud Sync

### 9.1 Export Formats

| Format | Library | Notes |
|--------|---------|-------|
| CSV | Built-in Kotlin | Simple, universal |
| JSON | kotlinx.serialization | For developers / API integration |
| Excel (.xlsx) | Apache POI (`poi-ooxml`) | Full formatting, multiple sheets |
| PDF | Android Print Framework | Via HTML → WebView → PDF |
| Custom Template | Apache POI | User provides .xlsx template, app fills it |

### 9.2 Custom Template Export

This is the killer feature. The user provides an Excel file with a specific layout — headers, formatting, formulas, colors — and Intent Pay fills in the data while preserving the template structure.

```kotlin
class TemplateParser {
    /**
     * Reads a .xlsx template file and identifies:
     * - Header row (auto-detected or marked with {{HEADER}})
     * - Data start row
     * - Column mappings via placeholder tags:
     *   {{DATE}}, {{AMOUNT}}, {{MERCHANT}}, {{CATEGORY}}, {{PURPOSE}}, {{TYPE}}
     * - Summary formulas (preserved as-is)
     */
    fun parseTemplate(templateFile: File): TemplateMapping

    /**
     * Fills the template with transaction data.
     * - Copies the template structure
     * - Inserts rows starting from dataStartRow
     * - Preserves all formatting, formulas, and styles
     * - Adjusts formula ranges if row count changes
     */
    fun fillTemplate(template: TemplateMapping, transactions: List<TransactionEntity>): File
}
```

**Template placeholder tags the user puts in their Excel:**

| Tag | Replaced With |
|-----|--------------|
| `{{DATE}}` | Transaction date (formatted) |
| `{{AMOUNT}}` | Amount (numeric) |
| `{{MERCHANT}}` | Merchant name |
| `{{CATEGORY}}` | Category display name |
| `{{PURPOSE}}` | User-entered purpose |
| `{{TYPE}}` | "Debit" or "Credit" |
| `{{UPI_REF}}` | UPI reference ID |
| `{{WEEK_TOTAL}}` | Sum formula for the week |
| `{{MONTH_TOTAL}}` | Sum formula for the month |
| `{{CATEGORY_TOTAL:Food}}` | Sum for a specific category |

### 9.3 Google Drive Backup

Using Google Drive API (not Sheets API — simpler, no formatting constraints):

```kotlin
class GoogleDriveSync(private val context: Context) {
    private val FOLDER_NAME = "IntentPay Backups"

    suspend fun backup(transactions: List<TransactionEntity>) {
        val driveService = buildDriveService()

        // 1. Find or create IntentPay folder
        val folderId = findOrCreateFolder(driveService)

        // 2. Export as JSON (complete data, small size)
        val jsonFile = JsonExporter.export(transactions)

        // 3. Upload with timestamp filename
        val filename = "intentpay_backup_${dateFormat.format(Date())}.json"
        uploadFile(driveService, folderId, filename, jsonFile)

        // 4. Also upload the formatted Excel if template exists
        // ...
    }

    suspend fun restore(): List<TransactionEntity> {
        // Find latest backup and import
    }
}
```

**Why Google Drive over Google Sheets:**
- Simpler API, fewer permissions
- Can store any file format (JSON, Excel, PDF)
- No 10MB cell limit issues
- Free 15GB storage is more than enough (a year of transactions is < 1MB)

---

## 10. UI Design System

### 10.1 Design Philosophy

**"Liquid Glass on Black Canvas"**

The design merges two aesthetics:
- **Apple's Liquid Glass** (iOS 26) — translucent materials, depth through blur, light-catching borders
- **Glassmorphism** — frosted glass cards floating over gradient meshes, semi-transparency, soft shadows

The result: a dark, premium feel where UI elements look like pieces of frosted glass floating over a deep, subtly animated gradient background.

### 10.2 Color Tokens

```kotlin
// === FOUNDATION ===
val CanvasBlack = Color(0xFF000000)
val CanvasDeep = Color(0xFF050510)          // very slightly blue-black

// === GLASS SURFACES ===
val GlassPrimary = Color(0x1AFFFFFF)        // 10% white — main card fill
val GlassElevated = Color(0x26FFFFFF)       // 15% white — elevated cards
val GlassPressed = Color(0x33FFFFFF)        // 20% white — tap feedback
val GlassBorder = Color(0x33FFFFFF)         // 20% white — subtle edge
val GlassBorderLight = Color(0x1AFFFFFF)    // 10% white — very subtle

// === ACCENT COLORS (vibrant, optimistic) ===
val AccentBlue = Color(0xFF0A84FF)          // primary actions
val AccentGreen = Color(0xFF30D158)         // income, positive
val AccentRed = Color(0xFFFF453A)           // alerts, overspend
val AccentOrange = Color(0xFFFF9F0A)        // warnings
val AccentPurple = Color(0xFFBF5AF2)        // insights, analytics
val AccentCyan = Color(0xFF64D2FF)          // secondary info
val AccentPink = Color(0xFFFF375F)          // shopping category
val AccentYellow = Color(0xFFFFD60A)        // bills category

// === TEXT ===
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0x99EBEBF5)       // 60% white
val TextTertiary = Color(0x4DEBEBF5)        // 30% white
val TextOnAccent = Color(0xFFFFFFFF)

// === GRADIENT MESH (background) ===
val MeshColor1 = Color(0xFF0A1628)          // deep navy
val MeshColor2 = Color(0xFF1A0A2E)          // deep purple
val MeshColor3 = Color(0xFF0A2818)          // deep emerald
// These shift subtly over time for a living background feel
```

### 10.3 Typography Scale

Based on Apple's SF Pro conventions, using system default (which renders as Roboto on Android, matching the weight/size philosophy):

```kotlin
object IntentPayType {
    // Display — huge numbers (spending totals, balances)
    val displayLarge = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
    val displayMedium = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)

    // Title — screen headers, section headers
    val titleLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold)
    val titleMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
    val titleSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)

    // Body — general content
    val bodyLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal)
    val bodyMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal)
    val bodySmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal)

    // Label — chips, buttons, section headers
    val labelLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    val labelMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
    val labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)

    // Caption — timestamps, metadata
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = TextSecondary)
}
```

### 10.4 Spacing System

8pt grid with 4pt subdivisions (Apple convention):

```kotlin
object Spacing {
    val xxxs = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp
    val xxxl = 56.dp

    // Screen padding
    val screenHorizontal = 20.dp  // Apple uses 20dp side margins
    val screenVertical = 16.dp

    // Card internal padding
    val cardPadding = 20.dp

    // Section spacing
    val sectionGap = 32.dp
}
```

### 10.5 Core Glass Components

#### GlassCard

The foundational component. A frosted glass card with blur, border, and subtle shadow.

```kotlin
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    blur: Dp = 20.dp,
    fillAlpha: Float = 0.10f,
    borderAlpha: Float = 0.20f,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            // Background blur (requires Android 12+ or Cloudy library fallback)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = fillAlpha),
                        Color.White.copy(alpha = fillAlpha * 0.6f)
                    )
                )
            )
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderAlpha),
                        Color.White.copy(alpha = borderAlpha * 0.3f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding),
            content = content
        )
    }
}
```

#### GlassBottomBar

Tab bar with frosted glass background:

```kotlin
@Composable
fun GlassBottomBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.08f)
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RectangleShape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Tab.values().forEach { tab ->
                GlassTabItem(
                    tab = tab,
                    isSelected = tab == selectedTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

enum class Tab(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Rounded.Home),
    INSIGHTS("Insights", Icons.Rounded.PieChart),
    HISTORY("History", Icons.Rounded.Receipt),
    SETTINGS("Settings", Icons.Rounded.Settings)
}
```

#### GlassButton

```kotlin
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true
) {
    val bg = if (isPrimary) AccentBlue else GlassPrimary
    val contentColor = if (isPrimary) Color.White else TextSecondary

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            disabledContainerColor = GlassPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            style = IntentPayType.labelLarge,
            color = if (enabled) contentColor else TextTertiary
        )
    }
}
```

#### SpendingRing (Donut Chart)

```kotlin
@Composable
fun SpendingRing(
    categorySpending: List<CategorySpending>,
    totalBudget: Double,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 24.dp
) {
    // Animated donut chart drawn with Canvas
    // Each category gets a colored arc segment
    // Center shows total amount with animated counter
    // Gentle glow effect on the active/largest segment
}
```

#### AnimatedCounter

Numbers that roll up/down when values change:

```kotlin
@Composable
fun AnimatedCounter(
    targetValue: Double,
    prefix: String = "₹",
    style: TextStyle = IntentPayType.displayMedium,
    color: Color = TextPrimary
) {
    val animatedValue by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
    Text(
        text = "$prefix${animatedValue.format()}",
        style = style,
        color = color
    )
}
```

### 10.6 Gradient Mesh Background

A subtle, slowly-shifting gradient that sits behind all content:

```kotlin
@Composable
fun GradientMeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw 3-4 large radial gradients at different positions
            // Positions shift slowly based on offset
            // Creates a subtle "living" background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MeshColor2, Color.Transparent),
                    center = Offset(size.width * (0.2f + offset * 0.3f), size.height * 0.3f),
                    radius = size.width * 0.6f
                )
            )
            // ... more circles for mesh effect
        }
        content()
    }
}
```

### 10.7 Animations & Motion

```kotlin
object IntentPayMotion {
    // Screen transitions
    val enterTransition = fadeIn(tween(300)) + slideInVertically(
        initialOffsetY = { it / 20 },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    // Card appearance (staggered)
    fun cardDelay(index: Int) = tween<Float>(
        durationMillis = 400,
        delayMillis = index * 60,
        easing = FastOutSlowInEasing
    )

    // Number counter roll
    val counterSpec = tween<Float>(800, easing = FastOutSlowInEasing)

    // Reflection overlay entrance
    val reflectionEnter = fadeIn(tween(200)) + slideInVertically(
        initialOffsetY = { it },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    )

    // Category chip selection
    val chipScale = spring<Float>(dampingRatio = 0.6f, stiffness = 500f)
}
```

### 10.8 Haptic Feedback

```kotlin
object IntentPayHaptics {
    // Light tap for selections
    fun selection(view: View) = view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)

    // Medium for confirmations
    fun confirm(view: View) = view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

    // Heavy for reflection popup
    fun alert(view: View) = view.performHapticFeedback(HapticFeedbackConstants.REJECT)
}
```

---

## 11. Screen-by-Screen Blueprint

### 11.1 Splash Screen

```
┌─────────────────────────────────────┐
│                                     │
│         [gradient mesh bg]          │
│                                     │
│                                     │
│            ╭─────────╮              │
│            │  LOGO   │  (animated)  │
│            ╰─────────╯              │
│                                     │
│          I N T E N T  P A Y         │
│        ─────────────────────        │
│         Your money, clarified       │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

Animated logo morphs in, text fades up. 1.5s total. Transitions with a smooth fade to Dashboard.

### 11.2 Dashboard Screen

```
┌─────────────────────────────────────┐
│ ◀ ────────────────────────── ⚙️     │ ← Glass top bar (translucent)
│                                     │
│  Summary                            │ ← Title, 34sp bold
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ░░░░░░ GLASS CARD ░░░░░░░░ │    │ ← Primary spending card
│  │                             │    │
│  │  Monthly Spending           │    │
│  │  ₹12,450  ← animated       │    │ ← 40sp bold, rolls up
│  │  of ₹25,000 budget          │    │
│  │                             │    │
│  │  ━━━━━━━━━━━━━━░░░░░░░░░░  │    │ ← Progress bar (gradient)
│  │  49.8%                      │    │
│  │                             │    │
│  │  Today          ₹1,200      │    │
│  │  This Week      ₹5,800      │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌────────────────────────────┐     │
│  │ ░░ SPENDING RING ░░░░░░░░ │     │ ← Donut chart card
│  │                            │     │
│  │     ╭───────╮              │     │
│  │    ╱  ₹12.4K ╲  🍕 35%    │     │
│  │   │  total    │  🚗 20%    │     │
│  │    ╲         ╱   📄 18%    │     │
│  │     ╰───────╯    🛍️ 15%    │     │
│  │                   ...      │     │
│  └────────────────────────────┘     │
│                                     │
│  ┌────────────────────────────┐     │
│  │ ░░ INSIGHT CARD ░░░░░░░░░ │     │ ← Dynamic insight
│  │ 💡 You spent 40% more on   │     │
│  │    Food this week vs last   │     │
│  └────────────────────────────┘     │
│                                     │
│  Recent                             │
│  ┌────────────────────────────┐     │
│  │ 🍕 Zomato      -₹350      │     │ ← Transaction tile
│  │    Food • Today 2:30 PM     │     │
│  │─────────────────────────── │     │
│  │ 🚗 Uber        -₹180      │     │
│  │    Travel • Today 11:00 AM  │     │
│  │─────────────────────────── │     │
│  │ ↔️  Rajesh      +₹2,000    │     │ ← Income (green)
│  │    Transfer • Yesterday     │     │
│  └────────────────────────────┘     │
│                                     │
│  ┌────┐┌────┐┌────┐┌────┐          │
│  │Home││Ins.││Hist││Set.│          │ ← Glass bottom bar
│  └────┘└────┘└────┘└────┘          │
└─────────────────────────────────────┘
```

### 11.3 Reflection Overlay (Post-Payment Popup)

```
┌─────────────────────────────────────┐
│                                     │
│     [full screen glass overlay]     │
│     [gradient mesh intensified]     │
│                                     │
│                                     │
│  Hold on.                           │ ← 40sp, AccentRed, bold
│                                     │
│  You just spent                     │
│  ₹1,200                            │ ← 56sp, white, animated
│  at Croma                           │ ← 20sp, TextSecondary
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ░░░░░░ GLASS INPUT ░░░░░░░ │    │
│  │  What was this for?         │    │ ← Placeholder text
│  └─────────────────────────────┘    │
│                                     │
│  CATEGORY                           │
│  ┌──────┐ ┌──────┐ ┌──────┐        │
│  │🍕Food│ │🚗Trvl│ │📄Bill│        │ ← Glass chips
│  └──────┘ └──────┘ └──────┘        │
│  ┌──────┐ ┌──────┐ ┌──────┐        │
│  │🛍️Shop│ │🎬Ent.│ │💊Hlth│        │
│  └──────┘ └──────┘ └──────┘        │
│  ┌──────┐ ┌──────┐ ┌──────┐        │
│  │📚Stdy│ │✨Luxr│ │📌Othr│        │
│  └──────┘ └──────┘ └──────┘        │
│                                     │
│                                     │
│  ┌─────────────────────────────┐    │
│  │      Save & Continue        │    │ ← AccentBlue button
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │      Skip for now           │    │ ← Glass secondary button
│  └─────────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

Key changes from current:
- Added **"Skip for now"** button (current version forces categorization)
- Skip marks it as `needsReview = true` and the reminder system takes over
- Merchant name shown prominently
- Amount is animated and huge
- Category chips are glass-styled with emojis

### 11.4 Batch Categorize Screen (NEW)

Triggered by reminder notifications. Shows uncategorized transactions as swipeable cards.

```
┌─────────────────────────────────────┐
│ ✕ ──── Catch Up ──── 3 left        │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ░░░░░░ GLASS CARD ░░░░░░░░ │    │
│  │                             │    │
│  │  ₹350                       │    │ ← 40sp
│  │  Swiggy                     │    │
│  │  2 days ago, 8:30 PM        │    │
│  │                             │    │
│  │  ┌───────────────────────┐  │    │
│  │  │ What was this for?    │  │    │
│  │  └───────────────────────┘  │    │
│  │                             │    │
│  │  🍕 🚗 📄 🛍️ 🎬 💊 📚 ✨ 📌  │    │ ← Tappable emoji row
│  │                             │    │
│  └─────────────────────────────┘    │
│                                     │
│          ← swipe to next →          │
│                                     │
│  ┌─────────────────────────────┐    │
│  │         Done                │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

### 11.5 Insights Screen (NEW)

```
┌─────────────────────────────────────┐
│                                     │
│  Insights                           │ ← 34sp bold
│  August 2026                        │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ░░ WEEKLY BAR CHART ░░░░░░ │    │
│  │                             │    │
│  │  ▐▌                         │    │
│  │  ▐▌  ▐▌      ▐▌             │    │
│  │  ▐▌  ▐▌  ▐▌  ▐▌  ▐▌        │    │
│  │  ▐▌  ▐▌  ▐▌  ▐▌  ▐▌  ▐▌   │    │
│  │  Mo  Tu  We  Th  Fr  Sa Su  │    │
│  │                             │    │
│  │  This week: ₹5,800          │    │
│  │  vs last week: ₹4,200 ↑38% │    │ ← red if up, green if down
│  └─────────────────────────────┘    │
│                                     │
│  Top Merchants                      │
│  ┌─────────────────────────────┐    │
│  │ 1. Zomato      ₹2,100  12x │    │
│  │ 2. Uber        ₹1,800   8x │    │
│  │ 3. Amazon      ₹1,200   3x │    │
│  └─────────────────────────────┘    │
│                                     │
│  Category Trends                    │
│  ┌──────┐ ┌──────┐ ┌──────┐        │
│  │🍕     │ │🚗     │ │🛍️     │       │
│  │₹4,200│ │₹2,100│ │₹1,800│        │ ← Horizontally scrollable
│  │ ↑12% │ │ ↓5%  │ │ ↑30% │        │
│  └──────┘ └──────┘ └──────┘        │
│                                     │
│  ┌────┐┌────┐┌────┐┌────┐          │
│  │Home││Ins.││Hist││Set.│          │
│  └────┘└────┘└────┘└────┘          │
└─────────────────────────────────────┘
```

### 11.6 History Screen (NEW)

Full transaction history with search and filters.

```
┌─────────────────────────────────────┐
│                                     │
│  History                            │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 🔍 Search merchants...      │    │ ← Glass search bar
│  └─────────────────────────────┘    │
│                                     │
│  ┌──────┐ ┌──────┐ ┌──────┐        │
│  │ All  │ │Debit │ │Credit│        │ ← Filter chips
│  └──────┘ └──────┘ └──────┘        │
│                                     │
│  Today                              │ ← Date group header
│  ┌─────────────────────────────┐    │
│  │ 🍕 Zomato      -₹350       │    │
│  │─────────────────────────────│    │
│  │ 🚗 Uber        -₹180       │    │
│  └─────────────────────────────┘    │
│                                     │
│  Yesterday                          │
│  ┌─────────────────────────────┐    │
│  │ ↔️  Rajesh      +₹2,000     │    │
│  │─────────────────────────────│    │
│  │ 📄 Jio Recharge -₹399      │    │
│  │─────────────────────────────│    │
│  │ 🛍️  Amazon      -₹1,299    │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌────┐┌────┐┌────┐┌────┐          │
│  │Home││Ins.││Hist││Set.│          │
│  └────┘└────┘└────┘└────┘          │
└─────────────────────────────────────┘
```

### 11.7 Export Screen (NEW)

```
┌─────────────────────────────────────┐
│ ← ──── Export Data                  │
│                                     │
│  Date Range                         │
│  ┌─────────────────────────────┐    │
│  │ This Month          ▼       │    │ ← Dropdown: week/month/custom
│  └─────────────────────────────┘    │
│                                     │
│  Format                             │
│  ┌─────────────────────────────┐    │
│  │ ░░ CSV ░░░░░░░░░░░░░░░░░░ │    │
│  │   Simple, opens anywhere    │    │
│  │─────────────────────────────│    │
│  │ ░░ Excel (.xlsx) ░░░░░░░░░ │    │
│  │   Formatted with categories │    │
│  │─────────────────────────────│    │
│  │ ░░ Custom Template ░░░░░░░ │    │
│  │   Use your own Excel layout │    │
│  │   📎 Select template...     │    │
│  │─────────────────────────────│    │
│  │ ░░ JSON ░░░░░░░░░░░░░░░░░ │    │
│  │   For developers & backup   │    │
│  └─────────────────────────────┘    │
│                                     │
│  Destination                        │
│  ┌─────────────────────────────┐    │
│  │ 📱 Save to Device           │    │
│  │─────────────────────────────│    │
│  │ ☁️  Upload to Google Drive   │    │
│  │─────────────────────────────│    │
│  │ 📤 Share...                 │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │         Export              │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

### 11.8 Settings Screen (redesigned)

```
┌─────────────────────────────────────┐
│ ← ──── Settings                     │
│                                     │
│  BUDGET                             │ ← Section header, 13sp, TextTertiary
│  ┌─────────────────────────────┐    │
│  │ Monthly Budget      ₹25,000 │    │
│  │─────────────────────────────│    │
│  │ Daily Soft Limit    ₹1,000  │    │
│  │─────────────────────────────│    │
│  │ Reflection Threshold ₹500   │    │
│  └─────────────────────────────┘    │
│                                     │
│  DETECTION                          │
│  ┌─────────────────────────────┐    │
│  │ SMS Detection       [ON]    │    │ ← Toggle switches
│  │─────────────────────────────│    │
│  │ Notification Access [ON]    │    │
│  │─────────────────────────────│    │
│  │ Monitored Apps      5 apps  │    │ ← Opens app picker
│  └─────────────────────────────┘    │
│                                     │
│  REMINDERS                          │
│  ┌─────────────────────────────┐    │
│  │ End-of-Day Reminder [ON]    │    │
│  │─────────────────────────────│    │
│  │ Reminder Time       9:00 PM │    │ ← Time picker
│  │─────────────────────────────│    │
│  │ 3-Day Follow-up     [ON]    │    │
│  │─────────────────────────────│    │
│  │ Weekly Report       [ON]    │    │
│  │─────────────────────────────│    │
│  │ Report Day          Sunday  │    │
│  └─────────────────────────────┘    │
│                                     │
│  CLOUD                              │
│  ┌─────────────────────────────┐    │
│  │ Google Drive Backup [OFF]   │    │
│  │─────────────────────────────│    │
│  │ Auto Backup         Weekly  │    │
│  └─────────────────────────────┘    │
│                                     │
│  DATA                               │
│  ┌─────────────────────────────┐    │
│  │ Export Data          →      │    │
│  │─────────────────────────────│    │
│  │ Import Data          →      │    │
│  │─────────────────────────────│    │
│  │ Manage Templates     →      │    │
│  │─────────────────────────────│    │
│  │ Merchant Rules       →      │    │ ← View/edit learned rules
│  └─────────────────────────────┘    │
│                                     │
│  ABOUT                              │
│  ┌─────────────────────────────┐    │
│  │ Version           2.0.0     │    │
│  │─────────────────────────────│    │
│  │ Open Source Licenses →      │    │
│  └─────────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

---

## 12. Implementation Roadmap

### Phase 1 — Foundation Rebuild (Weeks 1–2)

**Goal:** Restructure the codebase, build the design system, fix architecture.

| Task | Priority | Effort |
|------|----------|--------|
| Restructure packages as per Section 3 | HIGH | 1 day |
| Implement `GlassCard`, `GlassBottomBar`, `GlassButton`, `GlassTextField` | HIGH | 2 days |
| Build `GradientMeshBackground` | MEDIUM | 0.5 day |
| Set up new color tokens, typography, spacing | HIGH | 0.5 day |
| Create `Tab` enum and bottom navigation | HIGH | 1 day |
| Extract `IntentPayNavGraph.kt` from `MainActivity` | HIGH | 0.5 day |
| Add `AnimatedCounter` component | MEDIUM | 0.5 day |
| Add `SpendingRing` donut chart | MEDIUM | 1 day |
| Redesign `DashboardScreen` with new design system | HIGH | 2 days |
| Add animation specs and haptics | LOW | 0.5 day |

### Phase 2 — Merchant Memory & Smart Categorization (Weeks 3–4)

**Goal:** The app remembers your merchants and auto-categorizes.

| Task | Priority | Effort |
|------|----------|--------|
| Create `MerchantRuleEntity` + `MerchantRuleDao` | HIGH | 0.5 day |
| Room DB migration (version bump, new tables) | HIGH | 0.5 day |
| Implement `MerchantMemory.kt` | HIGH | 1 day |
| Implement `KeywordRules.kt` (built-in heuristics) | HIGH | 1 day |
| Implement `CategoryResolver.kt` (orchestrates memory + keywords) | HIGH | 0.5 day |
| Update `SmsReceiver` to use `UnifiedTransactionDetector` | HIGH | 1 day |
| Add `normalizedMerchant` and `needsReview` fields to `TransactionEntity` | HIGH | 0.5 day |
| Update Reflection screen — learn merchant→category on save | HIGH | 1 day |
| Add "Skip for now" to Reflection screen | HIGH | 0.5 day |
| Build Merchant Rules management screen in Settings | MEDIUM | 1 day |
| Enhance `Category` enum with emojis and colors | MEDIUM | 0.5 day |

### Phase 3 — Notification Listener (Weeks 5–6)

**Goal:** Detect payments from GPay, PhonePe, etc. via notifications.

| Task | Priority | Effort |
|------|----------|--------|
| Implement `PaymentNotificationListener` service | HIGH | 2 days |
| Implement `NotificationParser` for each UPI app | HIGH | 2 days |
| Build notification permission onboarding flow | HIGH | 1 day |
| Implement `DeduplicationEngine` (SMS + notification merge) | HIGH | 1 day |
| Add `source` field to TransactionEntity | MEDIUM | 0.5 day |
| Settings UI for monitored apps toggle | MEDIUM | 1 day |
| Test with real GPay/PhonePe notifications | HIGH | 1 day |

### Phase 4 — Reminder System (Weeks 7–8)

**Goal:** End-of-day and 3-day reminders for uncategorized transactions.

| Task | Priority | Effort |
|------|----------|--------|
| Implement `EndOfDayReminderWorker` | HIGH | 1 day |
| Implement `ThreeDayReminderWorker` | HIGH | 0.5 day |
| Implement `WorkerScheduler` (schedule/reschedule workers) | HIGH | 1 day |
| Build Batch Categorize screen (swipeable cards) | HIGH | 2 days |
| Create notification channels and rich notifications | MEDIUM | 1 day |
| Settings UI for reminder time, toggles | MEDIUM | 0.5 day |
| Add `reminderCount` tracking | LOW | 0.5 day |

### Phase 5 — Reports & Insights (Weeks 9–10)

**Goal:** Weekly reports and spending insights.

| Task | Priority | Effort |
|------|----------|--------|
| Implement `GenerateReportUseCase` | HIGH | 1 day |
| Implement `WeeklyReportWorker` | HIGH | 1 day |
| Build `InsightsScreen` with charts | HIGH | 2 days |
| Build `WeeklyReportScreen` | MEDIUM | 1.5 days |
| Implement `GetSpendingInsightsUseCase` | MEDIUM | 1 day |
| Add category trend comparison logic | MEDIUM | 1 day |
| Weekly bar chart (Compose Canvas) | MEDIUM | 1 day |
| Rich notification with weekly summary | LOW | 0.5 day |

### Phase 6 — Export & Cloud (Weeks 11–12)

**Goal:** Multi-format export with custom template support.

| Task | Priority | Effort |
|------|----------|--------|
| Implement `CsvExporter` | HIGH | 0.5 day |
| Implement `JsonExporter` | HIGH | 0.5 day |
| Implement `ExcelExporter` (Apache POI) | HIGH | 1.5 days |
| Implement `TemplateParser` (custom template engine) | HIGH | 2 days |
| Build `ExportScreen` UI | HIGH | 1 day |
| Implement `GoogleDriveSync` | MEDIUM | 2 days |
| Implement `GoogleDriveSyncWorker` (auto-backup) | MEDIUM | 1 day |
| Settings UI for cloud backup | LOW | 0.5 day |
| Import/restore from backup | LOW | 1 day |

### Phase 7 — History & Polish (Weeks 13–14)

**Goal:** Full transaction history, search, and final polish.

| Task | Priority | Effort |
|------|----------|--------|
| Build `HistoryScreen` with grouped list | HIGH | 1.5 days |
| Search and filter functionality | HIGH | 1 day |
| Transaction detail/edit bottom sheet | MEDIUM | 1 day |
| Onboarding flow (first-time permission setup) | MEDIUM | 1.5 days |
| Edge-to-edge support and system bar handling | MEDIUM | 0.5 day |
| Performance optimization (lazy loading, pagination) | MEDIUM | 1 day |
| Accessibility (content descriptions, talkback) | LOW | 1 day |
| App icon and store listing assets | LOW | 0.5 day |

---

## 13. Tech Stack & Dependencies

All open source, zero cost.

### Core

| Library | Purpose | License |
|---------|---------|---------|
| Kotlin 2.2+ | Language | Apache 2.0 |
| Jetpack Compose (BOM) | UI framework | Apache 2.0 |
| Room 2.6+ | Local database | Apache 2.0 |
| WorkManager 2.9+ | Background scheduling | Apache 2.0 |
| Navigation Compose | Screen navigation | Apache 2.0 |
| DataStore Preferences | Settings storage | Apache 2.0 |
| kotlinx.serialization | JSON serialization | Apache 2.0 |

### Detection & Parsing

| Library | Purpose | License |
|---------|---------|---------|
| Android Telephony API | SMS reception | Built-in |
| NotificationListenerService | App notification capture | Built-in |

### Export

| Library | Purpose | License |
|---------|---------|---------|
| Apache POI (poi-ooxml) | Excel read/write | Apache 2.0 |
| Android Print Framework | PDF generation | Built-in |

### Cloud

| Library | Purpose | License |
|---------|---------|---------|
| Google Drive API v3 | Cloud backup | Apache 2.0 |
| Google Auth (Credential Manager) | Authentication | Apache 2.0 |

### UI Enhancement

| Library | Purpose | License |
|---------|---------|---------|
| Cloudy | Blur backport for < Android 12 | Apache 2.0 |
| Coil | Image loading (if needed) | Apache 2.0 |
| Lottie Compose | Splash/loading animations | Apache 2.0 |

### Gradle Dependencies

```kotlin
// app/build.gradle.kts
dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.06.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Room
    val roomVersion = "2.7.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")

    // Apache POI (Excel)
    implementation("org.apache.poi:poi-ooxml:5.3.0")

    // Google Drive
    implementation("com.google.api-client:google-api-client-android:2.6.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20240521-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")

    // Blur
    implementation("com.github.skydoves:cloudy:0.2.0")

    // Splash
    implementation("androidx.core:core-splashscreen:1.0.1")
}
```

---

## 14. Testing Strategy

### Unit Tests

| What | How |
|------|-----|
| `SmsParser` | Feed real bank SMS strings, assert correct amount/merchant/ref extraction |
| `NotificationParser` | Feed notification text from GPay/PhonePe, assert correct parsing |
| `MerchantMemory` | Test rule creation, cache behavior, keyword matching |
| `DeduplicationEngine` | Test same-amount-same-window dedup, UPI ref dedup |
| `CategoryResolver` | Test keyword rules, user rules, priority ordering |
| `TemplateParser` | Test placeholder detection, data filling, formula preservation |
| All UseCases | Test business logic in isolation with mock repositories |

### Integration Tests

| What | How |
|------|-----|
| Room DAOs | In-memory database, test all queries return correct results |
| WorkManager | Test worker scheduling, execution, and rescheduling |
| Full pipeline | SMS → Parser → Dedup → Categorize → Insert → Verify DB state |

### UI Tests

| What | How |
|------|-----|
| Dashboard | Verify spending cards render, amounts are correct |
| Reflection | Verify overlay appears for high-value transactions |
| Batch Categorize | Verify swipe behavior, category selection, save |
| Export | Verify file generation for each format |

### Manual Testing Checklist

- [ ] Simulate bank SMS via Android emulator extended controls
- [ ] Test with real GPay/PhonePe payments on physical device
- [ ] Verify deduplication when both SMS and notification fire
- [ ] Test reminder notifications at scheduled times
- [ ] Export with custom template and verify formatting
- [ ] Google Drive backup and restore cycle
- [ ] Test on Android 10, 12, 13, 14 for blur compatibility
- [ ] Test with 1000+ transactions for performance

---

## Summary

Intent Pay has a strong foundation. The path to its full potential is:

1. **Glass design system** — make it look premium and feel native
2. **Merchant memory** — the single biggest UX upgrade (categorize once, auto forever)
3. **Notification listener** — faster and more reliable than SMS alone
4. **Gentle reminders** — don't let uncategorized transactions pile up
5. **Weekly reports** — turn raw data into actionable insight
6. **Flexible export** — your data, your format, your choice

14 weeks of focused work. Zero budget. Maximum impact.
