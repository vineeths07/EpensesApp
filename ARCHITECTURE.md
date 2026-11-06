# 🏛️ BudgetTrackerApp - System Architecture & Technical Specification

This document provides a comprehensive architectural and engineering breakdown of **BudgetTrackerApp**. It details how the application is designed, structured, and implemented across every layer—from presentation and state management to local persistence, multi-currency processing, and hardware biometric security.

---

## 📑 Table of Contents

- [1. Architectural Overview & Design Principles](#1-architectural-overview--design-principles)
- [2. System Architecture Diagram](#2-system-architecture-diagram)
- [3. Presentation Layer (Jetpack Compose & MVVM)](#3-presentation-layer-jetpack-compose--mvvm)
  - [3.1 Main Container & Adaptive Navigation](#31-main-container--adaptive-navigation)
  - [3.2 Screen Architecture & State Hoisting](#32-screen-architecture--state-hoisting)
  - [3.3 Reusable Component Design System](#33-reusable-component-design-system)
- [4. Domain & Business Logic Layer](#4-domain--business-logic-layer)
  - [4.1 Minor-Unit Precision Financial Arithmetic](#41-minor-unit-precision-financial-arithmetic)
  - [4.2 Multi-Currency Conversion Engine](#42-multi-currency-conversion-engine)
  - [4.3 Budget Computation & Alert Thresholds](#43-budget-computation--alert-thresholds)
  - [4.4 Data Export Pipeline](#44-data-export-pipeline)
- [5. Data & Persistence Layer](#5-data--persistence-layer)
  - [5.1 Room Database Configuration](#51-room-database-configuration)
  - [5.2 Database Entities & Relationships](#52-database-entities--relationships)
  - [5.3 Data Access Objects (DAOs)](#53-data-access-objects-daos)
  - [5.4 Repository Implementation & Flow Pipelines](#54-repository-implementation--flow-pipelines)
  - [5.5 Online Exchange Rate Synchronization](#55-online-exchange-rate-synchronization)
  - [5.6 Persistent User Preferences](#56-persistent-user-preferences)
- [6. Security & Privacy Subsystem](#6-security--privacy-subsystem)
  - [6.1 Biometric Authentication Architecture](#61-biometric-authentication-architecture)
  - [6.2 Device Credential Fallback](#62-device-credential-fallback)
  - [6.3 Ephemeral UI Privacy Mode](#63-ephemeral-ui-privacy-mode)
- [7. Concurrency & Threading Model](#7-concurrency--threading-model)
- [8. Testing Strategy & Quality Assurance](#8-testing-strategy--quality-assurance)

---

## 1. Architectural Overview & Design Principles

The application is engineered around four core tenets:

1. **Clean Architecture with Unidirectional Data Flow (UDF)**:
   - State flows strictly downwards from ViewModels to Composable functions via Kotlin `StateFlow`.
   - User interactions flow upwards from Composable functions to ViewModels via explicit lambda callbacks and method calls.
   - Separation of concerns ensures that UI components are decoupled from storage, database schemas, and networking logic.

2. **Offline-First & Local Sovereignty**:
   - The primary source of truth is the local SQLite database managed through Room.
   - The application does not require cloud connectivity or remote database accounts to operate. All transaction, category, and budget records remain strictly on the host device.

3. **Zero Floating-Point Financial Arithmetic**:
   - Financial systems must never rely on standard IEEE 754 floating-point representations (`Float` or `Double`) for balance calculation due to binary precision drift.
   - All monetary amounts in the database, entities, repositories, and state models are stored and calculated as 64-bit `Long` integers representing **minor currency units (cents)**.

4. **Hardware-Backed Privacy & Security**:
   - Access control is fortified using Android's biometric subsystem with seamless fallback to Android Keyguard PIN/Pattern credentials.
   - The UI includes an instant Privacy Mode to conceal financial data from shoulder surfing in public settings.

---

## 2. System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                PRESENTATION LAYER                               │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                MainActivity.kt                                  │
│                NavigationSuiteScaffold (Adaptive Navigation Rail/Bar)           │
│                                       │                                         │
│   ┌────────────────────┬──────────────┴───────┬─────────────────────────────┐   │
│   ▼                    ▼                      ▼                             ▼   │
│ DailyExpenseScreen  MonthlyExpenseScreen  YearlyExpenseScreen  PersonalisationScreen │
│ - AddTransaction   - MonthYearPicker     - YearlyBarChart     - CurrencySelector│
│ - EditTransaction  - CategoryBreakdown   - YearlySummaryCard  - BiometricToggle │
│ - DailySummaryCard - SetBudgetSheet      - SpendingInsights   - CSV Export      │
│   ▲                    ▲                      ▲                     │           │
└───┼────────────────────┼──────────────────────┼─────────────────────┼───────────┘
    │ StateFlow / Events │ StateFlow / Events   │ StateFlow / Events  │ Direct
┌───┴────────────────────┴──────────────────────┴─────────────────────┼───────────┐
│                                 VIEWMODEL LAYER                     │           │
├─────────────────────────────────────────────────────────────────────┼───────────┤
│   DailyExpenseViewModel    MonthlyExpenseViewModel    YearlyExpenseViewModel    │
│   (Selected date state,    (Selected month/year,      (Selected year state,     │
│    daily transactions,      monthly totals, budget     12-month bar chart stats,│
│    in/out calculations)     progress tracking)         savings calculations)    │
└───▲────────────────────▲──────────────────────▲─────────────────────┼───────────┘
    │                    │                      │                     │
    └────────────────────┼──────────────────────┘                     │
                         ▼                                            ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            DOMAIN & REPOSITORY LAYER                            │
├─────────────────────────────────────────────────────────────────────────────────┤
│  TransactionRepository (Interface)                                              │
│  └── TransactionRepositoryImpl (Room queries, coroutine dispatching)            │
│                                                                                 │
│  OnlineExchangeRateRepository                                                   │
│  └── Background API sync (open.er-api.com), local caching, fallback rates       │
│                                                                                 │
│  Domain Utilities:                                                              │
│  ├── CurrencyExchangeRates (Cross-currency math & rate matrices)                │
│  ├── CurrencyUtils (Minor-unit string formatting and parsing)                   │
│  ├── DateUtils (LocalDate formatting, month ranges, day boundaries)             │
│  ├── DataExportUtils (CSV serialization & Android Intent share dispatch)        │
│  └── BiometricAuthManager (BiometricPrompt integration & capability detection) │
└───▲───────────────────────────────────────────────────▲─────────────────────────┘
    │ Reactive Flow Streams                             │ SharedPreferences / Net
┌───┴─────────────────────────────────────────┐ ┌───────┴─────────────────────────┐
│               DATA STORAGE LAYER            │ │       PREFERENCES & NETWORK     │
├─────────────────────────────────────────────┤ ├─────────────────────────────────┤
│  AppDatabase (Room, SQLite, Version 4)      │ │  CurrencyPreferenceManager      │
│  ├── TransactionDao                         │ │  SecurityPreferenceManager      │
│  ├── CategoryDao                            │ │  HttpURLConnection (Live API)   │
│  └── BudgetDao                              │ └─────────────────────────────────┘
│                                             │
│  Entities:                                  │
│  ├── TransactionEntity (Indexed queries)    │
│  ├── CategoryEntity (16 default categories) │
│  └── BudgetEntity (Category-level limits)   │
└─────────────────────────────────────────────┘
```

---

## 3. Presentation Layer (Jetpack Compose & MVVM)

### 3.1 Main Container & Adaptive Navigation

`MainActivity.kt` serves as the single-activity orchestrator for the application:
- **`installSplashScreen()`**: Connects to `androidx.core.splashscreen` to guarantee an instant, smooth native cold-start transition.
- **`enableEdgeToEdge()`**: Configures system status and navigation bars to provide an edge-to-edge layout.
- **`NavigationSuiteScaffold`**: Material 3's modern adaptive navigation primitive. It automatically renders a bottom `NavigationBar` on compact devices (phones in portrait) and switches dynamically to a side `NavigationRail` on medium/expanded devices (tablets, foldables, and landscape phones).
- **Smooth Tab Transitions**: Tab switching is wrapped in `AnimatedContent` using fade-in and fade-out tween transitions (300ms) for high-polish navigation.

### 3.2 Screen Architecture & State Hoisting

Each primary destination is paired with a dedicated lifecycle-aware `ViewModel` implementing Unidirectional Data Flow:

#### 1. Daily View (`DailyExpenseViewModel` & `DailyExpenseScreen`)
- **State Exposed**:
  - `selectedDate: StateFlow<LocalDate>`
  - `transactionsWithCategory: StateFlow<List<TransactionWithCategory>>`
  - `dailySummary: StateFlow<DailySummary>` (`totalIncome`, `totalExpense`, `netBalance`)
  - `allCategories: StateFlow<List<CategoryEntity>>`
- **User Actions Handled**:
  - Date stepping (`previousDay()`, `nextDay()`, or arbitrary date selection via DatePicker).
  - Transaction creation (`addTransaction(...)`) with title, minor-unit amount, category, note, and currency.
  - Transaction editing (`updateTransaction(...)`).
  - Transaction deletion with undo capability (`deleteTransaction(...)` & `restoreTransaction(...)`).

#### 2. Monthly View (`MonthlyExpenseViewModel` & `MonthlyExpenseScreen`)
- **State Exposed**:
  - `selectedYear: StateFlow<Int>` and `selectedMonth: StateFlow<Int>`
  - `monthlySummary: StateFlow<MonthlySummary>` (`totalIncome`, `totalExpense`, `netSavings`, `savingsRate`)
  - `categoryBudgetProgress: StateFlow<List<CategoryBudgetProgress>>`
  - `monthlyTransactions: StateFlow<List<TransactionWithCategory>>`
- **Budget Monitoring Logic**:
  - Automatically joins expense category budgets with total expenditures within the active month.
  - Computes `spentAmount`, `budgetLimit`, `remainingAmount`, `progressPercent`, and sets an `isExceeded` flag.
  - Generates reactive alerts in the UI whenever spending reaches 80% (Warning) or surpasses 100% (Exceeded).

#### 3. Yearly View (`YearlyExpenseViewModel` & `YearlyExpenseScreen`)
- **State Exposed**:
  - `selectedYear: StateFlow<Int>`
  - `yearlySummary: StateFlow<YearlySummary>` (`annualIncome`, `annualExpense`, `annualSavings`, `savingsRate`, `highestExpenseMonth`)
  - `monthlyBreakdowns: StateFlow<List<MonthlyBreakdown>>` (All 12 calendar months with income/expense aggregates)
- **Visualization**:
  - `YearlyBarChart.kt`: A fully custom Canvas-based bar chart rendering comparative side-by-side vertical bars for every month, normalizing height dynamically to the maximum monthly cash flow.

#### 4. Personalisation View (`PersonalisationScreen`)
- Centralized control hub for user preferences, currency management, security policies, and data export.
- Directly interacts with `CurrencyPreferenceManager`, `SecurityPreferenceManager`, `OnlineExchangeRateRepository`, and `TransactionRepository`.

### 3.3 Reusable Component Design System

All screens utilize standardized, accessible UI components located in `com.budgettracker.app.ui.components`:

- **`AppHeader`**: Renders top-bar navigation with current title, quick currency dropdown switcher, privacy mode toggle button, and biometric lock status badge.
- **`CategoryAvatar`**: Vector icon chip inside a rounded container with background color matching the category's distinct brand color.
- **`CountryFlagBadge`**: Vector drawable flag badge paired with Unicode emoji indicators for currency selection.
- **`CurrencyDropdownSelector`**: Interactive menu for switching viewing currencies on the fly across any screen.
- **`TransactionItem`**: Structured card displaying category icon, title, note, date/time, and colored monetary value (`+` Green for Income, `-` Red/Primary for Expense).

---

## 4. Domain & Business Logic Layer

### 4.1 Minor-Unit Precision Financial Arithmetic

Standard floating-point types (`Float`, `Double`) cannot accurately represent decimal fractions in binary (e.g. `1.10 - 1.00 = 0.10000000000000009`). In financial software, this leads to accounting drift.

**BudgetTrackerApp enforces minor-unit storage**:
- All amounts are stored as 64-bit `Long` values representing cents/minor units (e.g., `$10.50` is stored as `1050L`, `¥500` is stored as `500L`).
- `CurrencyUtils.kt` provides mathematical translation:
  - `formatAmount(amountCents: Long, currency: AppCurrency): String`
  - `parseToCents(amountString: String): Long`
  - `centsToDecimalString(amountCents: Long): String`

### 4.2 Multi-Currency Conversion Engine

The multi-currency engine allows users to record transactions in any supported world currency and view aggregated reports in their chosen preferred currency.

- **`CurrencyExchangeRates.kt`**:
  - Stores exchange rates relative to USD (`1.0 USD = X Currency Units`).
  - Implements the cross-currency conversion formula:
    $$\text{Target Cents} = \text{round}\left( \left(\frac{\text{Source Cents}}{\text{Rate}_{\text{source}}}\right) \times \text{Rate}_{\text{target}} \right)$$
  - Ships with hardcoded baseline market exchange rates ensuring **100% functionality without internet access**.
  - Provides runtime updating via `CurrencyExchangeRates.updateRates(...)`.

### 4.3 Budget Computation & Alert Thresholds

Category spending limits are modeled through `CategoryBudgetProgress`:

```kotlin
data class CategoryBudgetProgress(
    val category: CategoryEntity,
    val spentAmount: Long,      // Minor units
    val budgetLimit: Long,      // Minor units
    val remainingAmount: Long,  // budgetLimit - spentAmount
    val progressPercent: Float, // spentAmount / budgetLimit
    val isExceeded: Boolean     // spentAmount > budgetLimit
)
```

UI threshold triggers:
- `progressPercent < 0.80f`: Normal state (Primary theme green/teal).
- `progressPercent in 0.80f..1.00f`: Warning threshold (Amber/Orange).
- `progressPercent > 1.00f`: Exceeded threshold (Red alert with badge).

### 4.4 Data Export Pipeline

Data export is encapsulated inside `DataExportUtils.kt`:
- **`generateCsv(transactions: List<TransactionWithCategory>): String`**: Generates RFC 4180-compliant CSV files with escaped quotation marks, containing columns:
  `ID,Date,Type,Category,Title,Amount,Currency,Note`
- **`shareCsv(context: Context, csvContent: String)`**: Dispatches an Android `ACTION_SEND` intent with MIME type `text/csv`, invoking the system Share Sheet for sharing to Google Drive, Email, WhatsApp, Slack, or local storage.

---

## 5. Data & Persistence Layer

### 5.1 Room Database Configuration

The local database is declared via `AppDatabase.kt`:
- **Version**: `4`
- **Type Converters**: `Converters.kt` translates Java 8 `LocalDate` to ISO-8601 strings (`YYYY-MM-DD`) and `TransactionType` enum to string representations.
- **Initial Seed Data**: Pre-populates the database with 16 default categories (11 Expense, 5 Income) upon initial database creation.

### 5.2 Database Entities & Relationships

#### 1. `TransactionEntity`
```kotlin
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["date"]),
        Index(value = ["year", "month"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long, // minor units (cents)
    val type: TransactionType,
    val categoryId: Long,
    val date: LocalDate,
    val year: Int = date.year,
    val month: Int = date.monthValue,
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val currencyCode: String = AppCurrency.getDefault().code
)
```
*Indices on `date`, `categoryId`, and composite `[year, month]` ensure $O(\log N)$ performance on range queries and aggregation.*

#### 2. `CategoryEntity`
```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val type: TransactionType
)
```

#### 3. `BudgetEntity`
```kotlin
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["categoryId"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Long,
    val currencyCode: String = AppCurrency.getDefault().code
)
```

#### 4. Relational Data Class: `TransactionWithCategory`
```kotlin
data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
```

### 5.3 Data Access Objects (DAOs)

Room DAOs return reactive Kotlin `Flow` streams that automatically emit updated results whenever underlying table data is inserted, updated, or deleted:

- **`TransactionDao`**:
  - `getTransactionsByDate(date: LocalDate): Flow<List<TransactionWithCategory>>`
  - `getTransactionsByMonth(year: Int, month: Int): Flow<List<TransactionWithCategory>>`
  - `getTransactionsByYear(year: Int): Flow<List<TransactionWithCategory>>`
  - `getTotalByTypeAndDate(date: LocalDate, type: TransactionType): Flow<Long?>`
  - `getTotalByTypeAndMonth(year: Int, month: Int, type: TransactionType): Flow<Long?>`
  - `getTotalByTypeAndYear(year: Int, type: TransactionType): Flow<Long?>`
- **`CategoryDao`**:
  - `getAllCategories(): Flow<List<CategoryEntity>>`
  - `getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>`
  - `insertCategories(categories: List<CategoryEntity>)`
- **`BudgetDao`**:
  - `getAllBudgets(): Flow<List<BudgetEntity>>`
  - `getBudgetForCategory(categoryId: Long): Flow<BudgetEntity?>`
  - `insertOrUpdateBudget(budget: BudgetEntity)`
  - `deleteBudget(categoryId: Long)`

### 5.4 Repository Implementation & Flow Pipelines

`TransactionRepositoryImpl.kt` coordinates between DAOs and ViewModels. It executes database modifications asynchronously on `Dispatchers.IO`:

```kotlin
class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TransactionRepository { ... }
```

### 5.5 Online Exchange Rate Synchronization

`OnlineExchangeRateRepository.kt` manages market rate synchronization:
1. Connects to `https://open.er-api.com/v6/latest/USD` using standard `HttpURLConnection`.
2. Sets strict connection and read timeouts (8000ms).
3. Parses JSON exchange rates for the 9 supported currency codes.
4. Persists downloaded rates and timestamp into private `SharedPreferences`.
5. Updates the active in-memory matrix in `CurrencyExchangeRates`.
6. Exposes `isRefreshing: StateFlow<Boolean>` and `lastUpdatedMillis: StateFlow<Long>` for UI state bindings.

### 5.6 Persistent User Preferences

- **`CurrencyPreferenceManager`**: Persists the user's selected viewing currency (`USD`, `EUR`, `INR`, etc.) in `SharedPreferences`, exposing changes as `selectedCurrency: StateFlow<AppCurrency>`.
- **`SecurityPreferenceManager`**: Persists `isBiometricEnabled` and `isPrivacyModeEnabled` state flags with immediate `StateFlow` reactivity.

---

## 6. Security & Privacy Subsystem

### 6.1 Biometric Authentication Architecture

Implemented via `BiometricAuthManager.kt` using `androidx.biometric`:

```
               ┌──────────────────────────────────────────────┐
               │         App Launch / Unlocked Check          │
               └──────────────────────┬───────────────────────┘
                                      │
                       Is Biometric Enabled in Prefs?
                                      │
                        ┌─────────────┴─────────────┐
                        ▼ YES                       ▼ NO
         ┌──────────────────────────────┐  ┌─────────────────────┐
         │ Check Biometric Capability   │  │ Direct App Access   │
         │ - BIOMETRIC_STRONG           │  └─────────────────────┘
         │ - BIOMETRIC_WEAK             │
         │ - DEVICE_CREDENTIAL (PIN)    │
         └──────────────┬───────────────┘
                        ▼
         ┌──────────────────────────────┐
         │ Launch BiometricPrompt       │
         └──────────────┬───────────────┘
                        │
         ┌──────────────┴──────────────┐
         ▼ SUCCESS                     ▼ ERROR / CANCEL
┌──────────────────┐          ┌───────────────────────────┐
│ Unlock App View  │          │ Keep BiometricLockScreen  │
│ (Reveal Data)    │          │ Overlay Active            │
└──────────────────┘          └───────────────────────────┘
```

### 6.2 Device Credential Fallback

By configuring `BiometricManager.Authenticators.DEVICE_CREDENTIAL`, the app allows users without active biometric enrollments (or on devices without biometric sensors) to authenticate using their lock screen PIN, Pattern, or Password.

### 6.3 Ephemeral UI Privacy Mode

When Privacy Mode is enabled:
- Monetary amounts in headers, cards, and list rows are replaced with `••••`.
- The user can toggle this with a single click from the header.
- The toggle preserves layout stability while completely shielding financial values from nearby viewers.

---

## 7. Concurrency & Threading Model

- **UI Thread (`Dispatchers.Main`)**: Handles Jetpack Compose composition, recomposition, layout measurement, and gesture events.
- **Worker Thread (`Dispatchers.IO`)**: Handles Room database queries, SQLite writes, type conversions, SharedPreferences I/O, and HTTP network requests.
- **Coroutines & Flow**: ViewModels utilize `viewModelScope` to launch jobs and collect database Flows, transforming them into `StateFlow` using `SharingStarted.WhileSubscribed(5000)` to optimize system resources when screens are in the background.

---

## 8. Testing Strategy & Quality Assurance

The application includes unit tests verifying correctness across all non-UI layers:

| Test Class | Focus Area |
| :--- | :--- |
| **`ConvertersTest`** | Validates Room `LocalDate` and `TransactionType` serialization / deserialization |
| **`TransactionRepositoryTest`** | Tests repository CRUD operations and aggregations |
| **`DailyExpenseViewModelTest`** | Tests date navigation, adding, updating, and deleting transactions |
| **`MonthlyExpenseViewModelTest`** | Tests monthly aggregation, savings rate computation, and budget progress logic |
| **`YearlyExpenseViewModelTest`** | Tests 12-month bar chart normalization and annual summary calculations |
| **`CurrencyExchangeRatesTest`** | Tests precision conversion logic, baseline rates, and update mechanisms |
| **`CurrencyUtilsTest`** | Tests minor unit conversions, string formatting, and parsing edge cases |
| **`DataExportUtilsTest`** | Tests CSV header formatting, row structure, and quote escaping |
| **`DateUtilsTest`** | Tests calendar boundary calculations, month navigation, and formatting |
| **`BiometricCapabilityTest`** | Validates biometric status enums and authentication flow states |

To execute the test suite:
```bash
./gradlew test
```
