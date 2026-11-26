# IntentPay — Engineering Status Report

**Reviewer stance:** senior Android engineer, strict, no sugar-coating.
**Repo:** https://github.com/Justinvcj/Intent_Pay
**Branch reviewed:** `main` @ `c66d6e1` (Phase 6)
**APK on disk:** `IntentPay_v1.apk` (~45 MB, uncommitted, untracked)

---

## 1. What this project actually is

A single-module native Android app (Kotlin + Jetpack Compose + Room) that tries to be an **offline, privacy-first UPI expense tracker for Indian users**. The core bet: instead of asking the user to log spending, the app passively observes UPI transactions via three parallel channels, categorizes them locally, and nudges the user to "reflect" on large spends.

**Three detection channels in parallel (redundant, but intentional):**

| Channel | File | Trigger | Needs |
|---|---|---|---|
| 1. Bank SMS | `SmsReceiver` + `SmsParser` | `SMS_RECEIVED` broadcast | `RECEIVE_SMS`/`READ_SMS` runtime |
| 2. UPI app notifications | `PaymentNotificationListener` + `NotificationParser` | Status-bar notification from GPay/PhonePe/Paytm/BHIM | `BIND_NOTIFICATION_LISTENER_SERVICE` user grant in Settings |
| 3. Accessibility scraping | `UpiAccessibilityService` | Window text in UPI apps | `BIND_ACCESSIBILITY_SERVICE` user grant in Settings |

All three funnel into `UnifiedTransactionDetector` → Room (`transactions`, `merchant_rules`, `budget_config`). A "reflection" overlay (`ReflectionActivity`) appears for debits above a configurable threshold. A separate `UpiInterceptActivity` is shown when the accessibility service detects the user leaving a UPI app.

**Layered beyond the core:** manual UPI payment entry (`ManualPaymentScreen` + `upi://` intent handoff), ML-Kit QR scanner, Google Sheets sync via `WorkManager`, CSV/JSON SAF export, daily reminder worker, 3-screen onboarding, animated splash, glassmorphism/"liquid glass" UI theme.

**Stack:** Kotlin 2.2.10, Compose BOM 2024.02, Room 2.6.1, WorkManager 2.9, CameraX 1.3, ML-Kit Barcode 17.2, Google Sheets v4, DataStore, Gson. `minSdk=26`, `targetSdk=34`, `compileSdk=34`, Java 17.

---

## 2. What is DONE (verified in source)

### Data / domain
- Room DB v2 with 3 entities (`TransactionEntity`, `BudgetEntity`, `MerchantRuleEntity`), 3 DAOs, Flow-based queries, singleton pattern, `fallbackToDestructiveMigration`.
- `TransactionRepository` exposes all transactions, today/month aggregates (`SUM`), uncategorized list and count, `deduplicateAndInsert` with a 2-minute amount-match window.
- `MerchantMemory`: 3-tier resolver (in-memory cache → DB rule → keyword fallback) with 8 keyword buckets (~70 India-specific merchants: Zomato, Swiggy, Uber, Amazon, Jio…).
- `ExportManager` generates CSV (with correct quote escaping) and pretty-printed JSON.
- `DataStoreManager` persists `is_onboarding_complete`.

### Detection
- SMS receiver with `goAsync()` + coroutines, parses Rs./INR amounts, UPI ref (12-digit), crude merchant extraction, debit/credit discrimination.
- Notification listener for 4 UPI apps, parses "paid/sent/debited/credited/received" + ₹/Rs./INR amounts.
- Accessibility service scans the UPI apps' view tree for `₹…` and "Paying/To …" text, launches `UpiInterceptActivity` on window-leave.
- Dedup by amount + 2-minute window across channels.

### UI (Compose)
- Navigation graph with 7 destinations (dashboard, analytics, history, settings, manual_payment, qr_scanner, pending_review).
- Dashboard: monthly budget card, today/month totals, pending-review pill, transaction list.
- Analytics: category pie/ring + category list, filtered to current month.
- History: grouped by month via `SimpleDateFormat("MMMM yyyy")` + sticky headers.
- Pending Review: swipeable categorizer that writes back to `MerchantMemory`.
- Reflection flow: full-screen activity with category chips + optional purpose, triggers a one-off `SyncWorker` on finish.
- Settings: budget limits, Google sign-in (Sheets scope), Sheet ID field, CSV/JSON export via SAF (`CreateDocument`).
- Onboarding: 3-page `HorizontalPager`.
- Animated splash with spring scale/alpha.
- QR scanner: CameraX preview + ML-Kit barcode analyzer with a `scanLocked` debounce.
- Design system: `GlassCard`, `GlassButton`, `GlassTextField`, `MeshBackground`, iOS-style palette.

### Infra
- `WorkManager` periodic `ReminderWorker` (24 h, `KEEP` policy), one-off `SyncWorker` kicked after reflection save.
- `GoogleSheetsService` appends rows to a dynamic yearly sheet tab ("2026 Daily Exp") with a running-balance formula.

### Housekeeping
- Default budget seeded on first launch (₹10,000 monthly / ₹500 daily / ₹200 reflection).
- Transaction list collapses duplicates across SMS + notification + accessibility within 2 min.

---

## 3. What is BROKEN or dangerous

Grouped by severity. Line refs are from the current HEAD.

### 🔴 Critical — will crash or fail build

1. **`ReflectionActivity` is not declared in `AndroidManifest.xml`.** `SmsReceiver.kt:38` builds an explicit Intent to `com.intentpay.ui.reflection.ReflectionActivity` and calls `startActivity`. Any debit SMS over the threshold will throw `ActivityNotFoundException` and the reflection-overlay feature — the app's flagship friction mechanism — never actually works via the SMS path. This is the single most important bug.
2. **Release build is unbuildable as configured.** [app/build.gradle.kts:21](app/build.gradle.kts:21) sets `isMinifyEnabled = true` and references `proguard-rules.pro`, but no `app/proguard-rules.pro` exists on disk. R8 will fail. Even if the file is added, Gson, Google API Client, Sheets v4, and Room all need keep rules that aren't present.
3. **AGP version is implausible.** `build.gradle.kts` pins `com.android.application` to `9.2.1` — Android Gradle Plugin 9.2.1 does not exist as a stable release. Either this is a typo for `8.2.1` or Gradle sync is already failing. Combined with KSP `2.3.2` vs Kotlin `2.2.10`, the toolchain triple is inconsistent (KSP tags track Kotlin versions; `2.3.x` KSP needs Kotlin 2.3.x).
4. **No Notification-Listener / Accessibility permission flow.** Neither onboarding nor Settings contains a button that opens `ACTION_NOTIFICATION_LISTENER_SETTINGS` or `ACTION_ACCESSIBILITY_SETTINGS`. Without the user manually discovering these system screens, 2 of the 3 detection channels are dark. The app ships looking "passive" but is in fact a black box on first install.
5. **No SMS runtime permission request.** `RECEIVE_SMS` / `READ_SMS` are declared but nothing in `MainActivity` or onboarding calls `ActivityCompat.requestPermissions`. On a fresh install, SmsReceiver will never fire because the permission is denied by default (API 23+).

### 🟠 High — feature-silently-broken

6. **`MerchantMemory.learn` is called with un-normalized keys.** `PendingReviewViewModel.kt:35` and `UpiInterceptActivity.kt:120` pass the raw merchant string (e.g. `"Zomato Ltd."`) to `learn()`, which inserts it as the `merchantPattern` verbatim. But `resolve()` normalizes before lookup (`[^a-z0-9]`-stripped lowercase), so the saved rule will **never match again**. Learning is write-only. Normalize before `learn()` or normalize inside `learn()`.
7. **Reflection threshold only enforced on the SMS path.** `SmsReceiver.kt:35` gates `ReflectionActivity` by `thresholdAmount`. `PaymentNotificationListener` and `UpiAccessibilityService` do not. Big-ticket payments detected via notification/accessibility silently skip the "reflect" step — which was the whole product thesis.
8. **`UpiAccessibilityService` lastAmount is unreliable.** It scrapes any text containing `₹`, keeps the last match, and fires on window-exit. Wallet balance, cashback banner, or "Auto-pay" copy on the same screen can overwrite the real amount. No confirmation that the payment actually succeeded (success screen vs cancel sheet).
9. **Dedup collapses legitimate repeats.** `deduplicateAndInsert` matches on amount + ±2 min only. Two ₹100 chais in the same minute become one row. Should include merchant/UPI-ref/source in the match key.
10. **`SyncWorker` has no `NetworkType.CONNECTED` constraint.** Fired one-off after every reflection save. Will retry-storm on airplane mode.
11. **Google Sheets flow is semantically confused.** `BudgetEntity.googleDriveFolderId` is actually used as a **spreadsheet ID** (`GoogleSheetsService.appendTransactions`). The field name, UI label ("Google Drive Folder ID"), and runtime usage disagree. `SyncWorker` early-returns `success()` when unconfigured — fine — but there's no validation that the sheet or the `"<year> Daily Exp"` tab exists; append on a missing tab throws.
12. **`ReminderWorker` ignores configured reminder time.** `BudgetEntity.endOfDayReminderHour/Minute` exist, but `ReminderWorker` just enqueues a 24 h periodic worker with no initial delay. Reminder fires at install-time + N·24h, not 9 PM local. The "3-day" and "weekly report" flags (`enableThreeDayReminder`, `enableWeeklyReport`, `weeklyReportDay`) are declared but **never read anywhere**.
13. ~~**`ExportTransactionsUseCase` is dead code and broken.** Uses `Environment.getExternalStoragePublicDirectory` which is a no-op on `targetSdk≥30` for non-media files, and `WRITE_EXTERNAL_STORAGE` is maxSdk=28. Settings already uses SAF via `CreateDocument` — delete this class.~~ **Fixed:** class deleted in PR #27.
14. **Accessibility service launches `UpiInterceptActivity` even on payment cancel.** No distinction between "user backed out" and "payment completed". Will log phantom transactions whenever the user opens GPay to check balance.
15. **No tests.** `app/src/test` and `app/src/androidTest` directories don't exist despite JUnit, Espresso, and Compose UI-Test being declared in `dependencies`. Zero coverage of regex parsing, categorization, dedup — the exact pieces most likely to misfire on real SMS corpora.

### 🟡 Medium — hygiene, UX, correctness drift

16. `IntentPayApplication` builds a fresh `TransactionRepository` in `ReminderWorker.doWork()` instead of reusing the app singleton. Harmless today but splits the ownership story.
17. `ExitInsets`/edge-to-edge not enabled; Compose nav bar sits behind system bar on gesture-nav devices.
18. `android:allowBackup="false"` but no `android:dataExtractionRules` (required declaration on API 31+; absence is a lint warning).
19. Launcher icon is `@drawable/intent_pay_logo` (a JPG) rather than a proper adaptive `@mipmap/ic_launcher`. Works but ignores Android 8+ adaptive-icon guidelines; also two near-duplicate assets exist on disk (`intent_pay_logo.jpg`, `intentpay_logo.xml`).
20. `IntentPay_v1.apk` and `IntentPay.apk` (~90 MB combined) are committed/untracked blobs in the repo root — don't ship binaries in git. Add `*.apk` to `.gitignore` and move releases to GitHub Releases.
21. `node_modules/` and `Jithu Daily Expense Sheet  (1).xlsx` in repo root. `.gitignore` is 148 bytes, almost certainly insufficient.
22. `SmsParser.parseUpiMessage` uses `lowercase()` on the message body then runs a `(?i)(?:to)\\s+…` regex on `messageBody` (original casing) — inconsistent. Merchant regex is `[a-zA-Z0-9@.-]+` which stops at the first space, so real merchant names like "DOMINOS PIZZA" get captured as "DOMINOS" only.
23. `NotificationParser.parse` trusts only `android.title`/`android.text`; many banks/UPI apps write the amount into `android.bigText` or `android.summaryText` extras. Expect ~30–50 % miss rate in the field.
24. `AnalyticsViewModel.loadMonthlyData` resubscribes via `collectLatest { … }` inside `init {}`. Fine today; combine with `stateIn` like `DashboardViewModel` already does — consistency matters.
25. `scrapeNodeInfo` recurses the whole accessibility tree every event with no depth cap and no recycling of `AccessibilityNodeInfo` — minor battery/ANR risk on heavy screens.
26. `IntentPayApplication` seeds a default budget via `CoroutineScope(Dispatchers.IO).launch`, unscoped — leaks until process death. Use `ProcessLifecycleOwner.get().lifecycleScope` or an `applicationScope` with `SupervisorJob`.
27. No feature flag or build variant for debug vs release logging; `Log.d` in `NotificationParser` ships in release.
28. No Crashlytics/Sentry/anything. Field bugs are invisible.
29. README claims "Rule-Based Classification via Room DB" — accurate — and "Behavioral Reflection Overlay" — currently non-functional per bug #1.
30. `ARCHITECTURE.md` is 1,874 lines dated "August 2026 – v2.0". It describes features (weekly reports, 3-day re-prompts, template-driven exports, custom categories CRUD, insights screen) that are not implemented in code. Doc and code are badly out of sync.

### 🔵 Low — nits

- `MainActivity` imports several symbols it doesn't use (`NavHost`, `composable`, `rememberNavController`, local `viewModel` import).
- Commit history: 6 "Phase X" commits with no granular history, no tags, no CHANGELOG, no PRs.
- No `.github/` directory: no CI, no issue templates, no Dependabot.
- `local.properties` committed? Check — it holds the SDK path; usually gitignored.
- Hard-coded strings everywhere ("Pending Review", "History", "Save Settings") — i18n = zero.
- Reminder notification uses `R.mipmap.ic_launcher_round` as a small icon; Android expects a white-silhouette drawable there — it will render as a solid tinted square on API 26+.

---

## 4. What is MISSING vs ARCHITECTURE.md's stated vision

The architecture doc promises, code does not deliver:

- Custom categories (user-defined with emoji + color).
- Budget alerts at 50 / 80 / 100 % of monthly.
- 3-day re-prompt for still-uncategorized transactions.
- Weekly / monthly report PDF generation.
- Google Drive **folder** sync (currently only Sheets append).
- Template-driven export (the `exportTemplatePath` column in `BudgetEntity` is unused).
- Insights screen (velocity, week-over-week delta, top merchants).
- "Snooze" + "Not a transaction" actions on pending review.
- Biometric lock for app entry.
- Multi-account / multi-currency.

---

## 5. Priority-ordered fix list (what I'd do first)

**Day 1 — unblock the build and the headline feature:**
1. Fix AGP version (`8.2.1` or whatever your Android Studio Giraffe/Hedgehog expects); align KSP to the Kotlin line.
2. Add `<activity android:name=".ui.reflection.ReflectionActivity" android:exported="false"/>` to the manifest.
3. Either add a real `app/proguard-rules.pro` with keep rules for Gson, `com.google.api.*`, `com.google.auth.*`, Room generated code — or set `isMinifyEnabled = false` until you're ready.
4. Normalize the merchant string **inside** `MerchantMemory.learn` (one line fix); back-fill existing bad rows with a one-shot migration.
5. Add a runtime permission request for `RECEIVE_SMS` + `READ_SMS` in onboarding, and a "Grant Notification Access" + "Grant Accessibility Access" button that opens the respective system intents.

**Week 1 — make detection trustworthy:**
6. Move the `> threshold` reflection trigger out of `SmsReceiver` into `UnifiedTransactionDetector` so notification + accessibility paths also trigger it.
7. Tighten dedup key: `(amount, source, upiRef ?: merchant, ±120 s)`.
8. In `UpiAccessibilityService`, only launch intercept when the outgoing window was a "Payment success"-labeled screen; reset `lastAmount` on back-navigation.
9. ~~Delete `ExportTransactionsUseCase` (dead code).~~ Done in PR #27.
10. Fire `SyncWorker` with `Constraints.Builder().setRequiredNetworkType(CONNECTED).build()`.
11. Rename `googleDriveFolderId` → `googleSpreadsheetId` end-to-end; add a one-time migration or just a column rename via Room schema bump.

**Week 2 — tests and CI:**
12. Add `app/src/test/` unit tests for `SmsParser`, `NotificationParser`, `MerchantMemory.resolve`, `KeywordRules.match`, `TransactionRepository.deduplicateAndInsert`, `ExportManager.generateCsv`. Build a corpus of 50 real SMS samples and 50 real notification extras to regression-test against.
13. Add a GitHub Actions workflow: `./gradlew assembleDebug testDebugUnitTest lintDebug`.
14. Add `.gitignore` entries for `*.apk`, `/node_modules/`, `/*.xlsx`, `local.properties`, `.idea/`, `.gradle/`, `build/`.

**Later — align with the architecture doc:**
15. Implement `endOfDayReminderHour`/`Minute` properly (OneTime reminder scheduled to wall-clock time with a 24 h periodic rescheduler, or `AlarmManager.setExactAndAllowWhileIdle`).
16. Weekly report (`enableWeeklyReport`) as a separate `WorkRequest` with the configured day-of-week.
17. User-defined categories.
18. Crash reporting (Sentry free tier) — you ship a notification-listener + accessibility service blind.

---

## 6. One-line verdict

**Promising product thesis, 70 % of the surface area shipped, but the headline friction feature (reflection) is wired to a crashing path, the release build doesn't compile, two of three detection channels have no permission-request UX, and there isn't a single automated test. Treat the current APK as an alpha demo, not a v1.**
