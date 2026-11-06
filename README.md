# 💰 BudgetTrackerApp - Personal Finance & Expense Tracker for Android

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.2.1-green.svg?logo=android)](https://developer.android.com/studio/releases/gradle-plugin)
[![Compose BOM](https://img.shields.io/badge/Compose_BOM-2025.12.00-purple.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-2.7.2-orange.svg)](https://developer.android.com/training/data-storage/room)
[![Target SDK](https://img.shields.io/badge/Target_SDK-36-brightgreen.svg)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/Min_SDK-24-blue.svg)](https://developer.android.com)

A modern, offline-first, privacy-focused Android personal finance and expense tracking application built using **100% Kotlin**, **Jetpack Compose (Material 3)**, **Room Persistence**, and **Clean Architecture (MVVM + UDF)**.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Screens & User Experience](#-screens--user-experience)
- [Technology Stack](#-technology-stack)
- [Project Architecture](#-project-architecture)
- [Folder Structure](#-folder-structure)
- [Data Storage & Database Schema](#-data-storage--database-schema)
- [Security & Privacy Features](#-security--privacy-features)
- [Multi-Currency & Exchange Rates](#-multi-currency--exchange-rates)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Building the Project](#building-the-project)
  - [Running Unit Tests](#running-unit-tests)
- [Architecture Details](#-architecture-details)
- [Contributing & License](#-contributing--license)

---

## 🌟 Overview

Managing personal finances shouldn't be complicated, slow, or invasive. **BudgetTrackerApp** gives users full control over their expenses, incomes, and budgets with zero cloud dependency for their sensitive financial data.

The application stores all financial data locally using an encrypted SQLite database via Room, allows users to view transactions across multiple world currencies with real-time exchange rates, locks down access using native biometric authentication (Fingerprint, Face Unlock, or Device PIN), and empowers users to export their financial data as CSV at any time.

---

## 🚀 Key Features

- **📊 Comprehensive Expense & Income Tracking**: Record and categorize daily transactions with titles, custom amounts, date pickers, notes, and currency tags.
- **🎯 Category-Level Monthly Budgets**: Set spending caps on expense categories and monitor real-time progress bars with color-coded alerts (Normal -> Warning at 80% -> Exceeded at 100%+).
- **📈 Yearly Analytical Charts**: Visual bar chart breakdown representing 12 months of income vs. expense performance, net savings rates, and highest spend analysis.
- **🌍 Multi-Currency with Live Market Rates**: View accounts in 9 international currencies (`USD`, `EUR`, `GBP`, `INR`, `JPY`, `CAD`, `AUD`, `CHF`, `CNY`). Live rates are periodically fetched from the open exchange rates API with offline fallback.
- **🔒 Biometric Security & App Lock**: Secure financial records using AndroidX BiometricPrompt. Supports Fingerprint, Face ID, or system credential PIN/pattern fallback.
- **👁️ Privacy Shield Mode**: Toggle privacy mode anytime with one tap from the header to mask account totals and sensitive monetary amounts (`••••`).
- **📤 Data Portability (CSV Export)**: Instantly generate and share clean CSV exports of all historical financial records via Android's native Share Sheet.
- **🎨 Dynamic Material 3 Theming**: Full support for Light and Dark modes with smooth transitions and Android 12+ dynamic color harmonization.
- **📱 Responsive & Adaptive UI**: Built with Jetpack Compose `NavigationSuiteScaffold` ensuring seamless layouts across smartphones, foldables, and tablets.

---

## 📱 Screens & User Experience

### 1. Splash & Biometric Authentication
- **Animated Splash Screen**: Custom animation scaling and fading smoothly into the main application.
- **Biometric Lock Screen**: If enabled in settings, an authentication overlay intercepts app launch, requiring biometric or device credential verification before revealing financial data.

### 2. Daily Expense Screen (`DailyExpenseScreen`)
- Date picker / day stepper with quick navigation (Yesterday, Today, Tomorrow, or any custom date).
- Daily Summary Card showing total Income, Expenses, and Net Balance for the selected day.
- Scrollable list of transactions showing category badge, description, amount, note, and time.
- Swipe or tap to Edit or Delete with an Undo Snackbar.
- Modal Bottom Sheet to add new income or expense with category selector and numeric keyboard.

### 3. Monthly Expense Screen (`MonthlyExpenseScreen`)
- Month/Year selector dialog.
- Monthly Summary Card with Total Inflow, Total Outflow, Net Savings, and Savings Rate percentage.
- **Category Budget Breakdown**: Interactive cards showing allocated budget vs. spent amount, remaining allowance, and percentage progress indicator.
- Set/Adjust category budget limits via a dedicated bottom sheet.
- Complete monthly transaction history.

### 4. Yearly Overview Screen (`YearlyExpenseScreen`)
- Annual financial totals (Total Earned, Total Spent, Total Saved).
- Custom 12-month comparative Bar Chart (`YearlyBarChart`) with side-by-side Income and Expense bars.
- Highest spending month indicator and yearly savings rate.

### 5. Personalisation & Settings Screen (`PersonalisationScreen`)
- **Preferred Currency**: Choose your primary viewing currency with instant recalculation of all financial balances.
- **Live Exchange Rate Engine**: Status indicator showing last update timestamp, with a button to trigger on-demand rate synchronization against live exchange rates.
- **Biometric Security Settings**: Enable or disable fingerprint/face authentication on app launch.
- **Privacy Mode**: Toggle monetary amount masking across the entire application.
- **Budget Manager**: Overview and configuration of all category spending caps.
- **Data Export**: One-tap CSV generation and sharing.

---

## 🛠️ Technology Stack

| Component | Library / Framework | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | `2.2.10` | First-class modern language with coroutines |
| **UI Framework** | Jetpack Compose | BOM `2025.12.00` | Declarative UI toolkit |
| **Design System** | Material 3 | `1.3.x` | Material You components, colors, and typography |
| **Navigation** | Adaptive Navigation Suite | Compose Material 3 | Adaptive navigation bar / rail across form factors |
| **Database** | Room | `2.7.2` | SQLite object mapping and reactive queries via Flow |
| **Annotation Processor**| KSP (Kotlin Symbol Processing) | `2.2.10-2.0.2`| High-performance code generation for Room |
| **Architecture** | AndroidX ViewModel & Lifecycle | `2.8.7` | UI state preservation across configuration changes |
| **Security** | AndroidX Biometric | `1.2.0-alpha05` | Hardware-backed biometric authentication |
| **Splash Screen** | AndroidX Core SplashScreen | `1.0.1` | Android 12+ backwards-compatible splash API |
| **Concurrency** | Kotlin Coroutines & Flow | `1.9.0` | Asynchronous programming & reactive state streams |
| **Testing** | JUnit 4 + Coroutines Test | `4.13.2` / `1.9.0`| Unit tests for repositories, viewmodels, and utilities |

---

## 📐 Project Architecture

BudgetTrackerApp follows **Clean Architecture** and **Unidirectional Data Flow (UDF)** under the **MVVM** pattern:

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                   │
│   Jetpack Compose UI Screens & Shared Components       │
│   (Daily, Monthly, Yearly, Personalisation, Security)  │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────┴────────────────────────────┐
│                    VIEWMODEL LAYER                     │
│   DailyExpenseViewModel, MonthlyExpenseViewModel,      │
│   YearlyExpenseViewModel                               │
└───────────────────────────▲────────────────────────────┘
                            │ Domain Calls / Flow
┌───────────────────────────┴────────────────────────────┐
│                   REPOSITORY LAYER                     │
│   TransactionRepository / OnlineExchangeRateRepository │
└─────────────▲───────────────────────────▲──────────────┘
              │                           │
┌─────────────┴─────────────┐ ┌───────────┴──────────────┐
│        LOCAL DATABASE     │ │      NETWORK / PREFS     │
│   Room Database (SQLite)  │ │   Open Exchange Rates API│
│   Transaction, Category,  │ │   SharedPreferences      │
│   Budget DAOs & Entities  │ │   (Currency, Biometrics) │
└───────────────────────────┘ └──────────────────────────┘
```

For comprehensive details on state flows, schema diagrams, and threading model, see [ARCHITECTURE.md](ARCHITECTURE.md).

---

## 📂 Folder Structure

```
BudgetTrackerApp/
├── app/
│   ├── build.gradle.kts                 # App-level build config, SDK targets, dependencies
│   ├── proguard-rules.pro               # ProGuard / R8 rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml      # App permissions (INTERNET) & MainActivity declaration
│       │   ├── java/com/budgettracker/app/
│       │   │   ├── MainActivity.kt      # Main entry point & NavigationSuiteScaffold container
│       │   │   ├── data/
│       │   │   │   ├── converter/       # Room TypeConverters (LocalDate, TransactionType)
│       │   │   │   ├── dao/             # Room Data Access Objects (TransactionDao, CategoryDao, BudgetDao)
│       │   │   │   ├── database/        # Room Database configuration & initial category seeding
│       │   │   │   ├── model/           # Entities (Transaction, Category, Budget) & Enums (AppCurrency, TransactionType)
│       │   │   │   ├── preferences/     # CurrencyPreferenceManager, SecurityPreferenceManager
│       │   │   │   └── repository/      # TransactionRepository interface, Implementation & OnlineExchangeRateRepository
│       │   │   ├── security/            # BiometricAuthManager & BiometricCapability
│       │   │   ├── ui/
│       │   │   │   ├── components/      # Reusable UI (AppHeader, CategoryAvatar, FlagBadge, CurrencyDropdown, etc.)
│       │   │   │   ├── daily/           # Daily expense screen, viewmodel, add/edit transaction sheets
│       │   │   │   ├── monthly/         # Monthly expense screen, budget cards, month-year picker
│       │   │   │   ├── yearly/          # Yearly screen, annual summary, comparative bar chart
│       │   │   │   ├── personalisation/ # Personalisation screen (currency, biometrics, budgets, CSV export)
│       │   │   │   ├── security/        # Biometric lock overlay & security toggle cards
│       │   │   │   ├── splash/          # Animated splash screen
│       │   │   │   └── theme/           # Material 3 colors, shapes, typography, and theme definitions
│       │   │   └── util/                # Utilities: CurrencyUtils, CurrencyExchangeRates, DataExportUtils, DateUtils
│       │   └── res/                     # Vector drawables, flags, icons, mipmaps, strings, themes
│       └── test/java/com/budgettracker/app/
│           ├── data/                    # Unit tests for Room converters and repositories
│           ├── security/                # Biometric capability unit tests
│           ├── ui/                      # ViewModels unit tests (Daily, Monthly, Yearly)
│           └── util/                    # Unit tests for currency conversion, export, and dates
├── gradle/
│   ├── libs.versions.toml               # Centralized Gradle Version Catalog
│   └── wrapper/                         # Gradle wrapper binaries & configuration
├── build.gradle.kts                     # Root build configuration
├── settings.gradle.kts                  # Repository resolution & project structure
├── ARCHITECTURE.md                      # Exhaustive architectural documentation
└── README.md                            # Project documentation
```

---

## 🗄️ Data Storage & Database Schema

All transactional data is stored in the local SQLite database `budget_tracker.db` managed by Room:

### Precision Financial Representation
To prevent floating-point rounding errors (e.g. `0.1 + 0.2 = 0.30000000000000004`), all financial figures are strictly stored as **64-bit Long integers representing minor currency units (cents)**.
- Example: `$12.50` is stored as `1250L`.
- `CurrencyUtils.formatAmount(1250L, AppCurrency.USD)` formats this safely to `"$12.50"`.

### Database Tables:
1. **`transactions`**:
   - `id` (Primary Key, Auto-increment)
   - `title` (String)
   - `amount` (Long in cents)
   - `type` (`EXPENSE` or `INCOME`)
   - `categoryId` (Foreign key to Category)
   - `date` (`LocalDate` ISO-8601)
   - `year` & `month` (Indexed for instant monthly/yearly aggregations)
   - `note` (Optional description)
   - `currencyCode` (Original currency of the transaction)
   - `createdAtMillis` (Timestamp)
2. **`categories`**:
   - Pre-seeded with 11 default expense categories and 5 default income categories.
   - Includes custom vector icon mapping and distinct hex color values.
3. **`budgets`**:
   - Unique category-level monthly spending limits.

---

## 🔐 Security & Privacy Features

1. **Hardware-Backed Biometrics**:
   - Integrated with `androidx.biometric.BiometricPrompt`.
   - Checks device hardware capability (`BIOMETRIC_STRONG`, `BIOMETRIC_WEAK`, `DEVICE_CREDENTIAL`).
   - Seamlessly falls back to device Lock Screen PIN / Pattern if biometric sensors are not enrolled.
2. **Privacy Mode (`••••`)**:
   - Conceals financial values across headers, lists, and summary cards.
   - Ideal for viewing expenses in public transit, offices, or shared spaces.
3. **Zero Third-Party Telemetry**:
   - No tracking SDKs, no third-party analytics, no remote database syncing.
   - All financial records stay 100% on the user's physical device.

---

## 💱 Multi-Currency & Exchange Rates

BudgetTrackerApp supports 9 major international currencies:

| Code | Symbol | Currency Name | Country / Zone | Flag |
| :---: | :---: | :--- | :--- | :---: |
| **USD** | `$` | United States Dollar | United States | 🇺🇸 |
| **EUR** | `€` | Euro | European Union | 🇪🇺 |
| **GBP** | `£` | British Pound | United Kingdom | 🇬🇧 |
| **INR** | `₹` | Indian Rupee | India | 🇮🇳 |
| **JPY** | `¥` | Japanese Yen | Japan | 🇯🇵 |
| **CAD** | `CA$`| Canadian Dollar | Canada | 🇨🇦 |
| **AUD** | `AU$`| Australian Dollar | Australia | 🇦🇺 |
| **CHF** | `CHF`| Swiss Franc | Switzerland | 🇨🇭 |
| **CNY** | `¥` | Chinese Yuan | China | 🇨🇳 |

### Exchange Rate Pipeline:
- **Baseline Rates**: Ships with built-in baseline rates for complete offline availability.
- **Background Sync**: Fetches fresh market rates upon app start via the Open Exchange Rates API (`https://open.er-api.com/v6/latest/USD`).
- **Dynamic Conversion**: When viewing totals in another currency, each transaction is automatically converted using the active conversion rate:
  $$\text{Converted Amount} = \left(\frac{\text{Amount}}{\text{Rate}_{\text{source}}}\right) \times \text{Rate}_{\text{target}}$$

---

## 🏁 Getting Started

### Prerequisites
- **Android Studio**: Android Studio Ladybug (2024.2.1) or newer recommended.
- **JDK**: Java Development Kit 17 or Java 21 configured in Android Studio.
- **Android SDK**: Android SDK API Level 36 (target), minimum API Level 24 (Android 7.0+).

### Building the Project

Clone the repository and build the debug APK using the Gradle wrapper:

```bash
# Clone the repository
git clone https://github.com/vineeths07/EpensesApp.git
cd EpensesApp

# Build debug APK
./gradlew assembleDebug
```

The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Running Unit Tests

The test suite validates database converters, repository logic, ViewModels, currency calculations, date math, and data export:

```bash
./gradlew test
```

To run a specific test suite:
```bash
./gradlew testDebugUnitTest --tests "com.budgettracker.app.util.CurrencyUtilsTest"
```

---

## 📚 Architecture Details

For a detailed technical specification of the design patterns, Room DAO queries, StateFlow architecture, coroutine dispatchers, and Compose UI hierarchy, please refer to the accompanying [ARCHITECTURE.md](ARCHITECTURE.md) document.

---

## 📄 License & Attribution

Distributed under the Apache License 2.0. Built with ❤️ for clean, secure, and private personal finance management.
