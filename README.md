# Expense Tracker (Spring Boot 3 + PostgreSQL + Thymeleaf)

A full-stack personal expense recording web application built with Spring Boot 3, PostgreSQL persistence, and Thymeleaf with Bootstrap 5. 

It is architected with a **dual-controller pattern** (`ExpenseViewController` for the web UI and `ExpenseRestController` for JSON API), making it ready for an Android mobile application or a future frontend migration without changing backend logic.

---

## Features

- **Expense Entry:** Record expenses with Date, Description, Amount, **Currency** (defaults to Euro `€`), Category, and Payment Method.
- **Multi-Currency Support:** Choose between `EUR` (€, default), `USD` ($), `GBP` (£), `CHF`, `JPY` (¥), `CAD` (CA$), and `AUD` (AU$).
- **Categorization & Filtering by Month:** Filter expenses by month with instant totals and breakdowns.
- **Summary Metrics:**
  - Total amount spent in the selected month (formatted in Euro by default or multi-currency summary).
  - Total number of transactions.
  - Expense breakdown by Currency.
  - Expense breakdown by Category (`Groceries`, `Food & Dining`, `Utilities`, `Transportation`, `Entertainment`, `Healthcare`, `Shopping`, `Other`).
  - Expense breakdown by Payment Method (`Cash`, `Credit Card`, `Debit Card`, `Gift Card`, `Bank Transfer`, `Other`).
- **Internationalization (i18n):** Native support for English and **Greek (Ελληνικά)** with on-the-fly language switching, cookie persistence (`EXPENSES_LOCALE`), localized calendar month headings, validation messages, and categories/payment methods/currencies.
- **Data Persistence:** Backed by PostgreSQL (`docker-compose.yml` included) with automated migration scripts.
- **Mobile-Ready REST API:** Full JSON CRUD and summary endpoints under `/api/v1/expenses` with CORS enabled and localized metadata support.

---

## Tech Stack

- **Java:** 17 or higher
- **Spring Boot:** 3.3.4 (Spring Web, Spring Data JPA, Spring Validation, Thymeleaf)
- **Database:** PostgreSQL 16
- **UI:** Thymeleaf, Bootstrap 5.3, Bootstrap Icons
- **Build Tool:** Maven

---

## Project Structure

```
expenses/
├── docker-compose.yml              # PostgreSQL local container
├── pom.xml                         # Maven dependencies & build configuration
├── src/
│   ├── main/
│   │   ├── java/com/example/expenses/
│   │   │   ├── ExpensesApplication.java
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java               # Cross-Origin configuration for Android / frontend clients
│   │   │   │   └── I18nConfig.java               # LocaleResolver (cookie) & LocaleChangeInterceptor (?lang=)
│   │   │   ├── controller/
│   │   │   │   ├── ExpenseRestController.java    # REST API for Android/JSON clients (/api/v1/expenses)
│   │   │   │   └── ExpenseViewController.java    # Thymeleaf Web UI controller (/expenses)
│   │   │   ├── dto/
│   │   │   │   ├── ExpenseRequestDto.java        # Form/API input validation DTO (localized messages)
│   │   │   │   ├── ExpenseResponseDto.java       # Clean API output DTO
│   │   │   │   └── MonthlySummaryDto.java        # Monthly calculation metrics & currency breakdowns
│   │   │   ├── model/
│   │   │   │   ├── Category.java                 # Expense Category enum
│   │   │   │   ├── Currency.java                 # Currency enum (EUR default, USD, GBP, etc.)
│   │   │   │   ├── PaymentMethod.java            # Payment Method enum
│   │   │   │   └── Expense.java                  # JPA Entity mapped to PostgreSQL
│   │   │   ├── repository/
│   │   │   │   └── ExpenseRepository.java        # Spring Data JPA repository
│   │   │   └── service/
│   │   │       └── ExpenseService.java           # Business logic & summary aggregations
│   │   └── resources/
│   │       ├── application.properties            # PostgreSQL, logging, server & i18n settings
│   │       ├── messages.properties               # Default / English translation messages bundle
│   │       ├── messages_en.properties            # English translation messages bundle
│   │       ├── messages_el.properties            # Greek (Ελληνικά) translation messages bundle
│   │       ├── db/
│   │       │   └── migration/
│   │       │       └── migrate_expenses_currency.sql  # SQL migration for existing records
│   │       └── templates/
│   │           └── index.html                    # Responsive Bootstrap 5 UI with language switcher
│   └── test/
│       └── java/com/example/expenses/
│           ├── ExpenseServiceTest.java           # Service layer unit tests
│           ├── ExpenseViewControllerTest.java    # Web UI MVC & localization tests (EN & EL)
│           ├── ExpenseRestControllerTest.java    # REST API & localized metadata tests
│           └── I18nTest.java                     # Message bundle resolution unit tests
└── README.md
```

---

## Quick Start Guide

### 1. Prerequisites

Make sure you have installed:
- **Java 17 or higher** (`sudo apt install openjdk-17-jdk` on Ubuntu/Debian)
- **Maven** (`sudo apt install maven` on Ubuntu/Debian)
- **Docker & Docker Compose** (or a local PostgreSQL instance)

### 2. Start PostgreSQL

Run the bundled Docker Compose file from the project directory:

```bash
docker compose up -d
```

This starts PostgreSQL on port `5432` with:
- **Database:** `expensedb`
- **Username:** `postgres`
- **Password:** `postgres`

> If using an existing PostgreSQL installation, update credentials in `src/main/resources/application.properties`.

### 3. Build & Run the Application

```bash
# Compile and run with Maven:
mvn spring-boot:run
```

Once started, open your browser and navigate to:
**[http://localhost:8080/expenses](http://localhost:8080/expenses)**

---

## Android / REST API Reference

When developing an Android client (using Retrofit, Ktor, Volley, or OkHttp), use the following endpoints:

### 1. Create an Expense
- **Endpoint:** `POST /api/v1/expenses`
- **Headers:** `Content-Type: application/json`
- **Request Body:**
  ```json
  {
    "date": "2026-10-10",
    "description": "Coffee and sandwich",
    "amount": 7.50,
    "category": "FOOD_AND_DINING",
    "paymentMethod": "CREDIT_CARD",
    "currency": "EUR"
  }
  ```
- **Response:** `201 Created` with created `ExpenseResponseDto`.

**Sample cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-10-10",
    "description": "Coffee and sandwich",
    "amount": 7.50,
    "category": "FOOD_AND_DINING",
    "paymentMethod": "CREDIT_CARD",
    "currency": "EUR"
  }'
```

### 2. Get Expenses by Month
- **Endpoint:** `GET /api/v1/expenses?year=2026&month=10`
- **Response:** `200 OK` (Array of expenses for the given month, sorted latest first).

**Sample cURL:**
```bash
curl http://localhost:8080/api/v1/expenses?year=2026&month=10
```

### 3. Get Monthly Summary & Breakdown
- **Endpoint:** `GET /api/v1/expenses/summary?year=2026&month=10`
- **Response:** `200 OK`
  ```json
  {
    "year": 2026,
    "month": 10,
    "totalAmount": 70.50,
    "count": 2,
    "categoryBreakdown": {
      "GROCERIES": 45.50,
      "FOOD_AND_DINING": 25.00
    },
    "paymentMethodBreakdown": {
      "CREDIT_CARD": 45.50,
      "CASH": 25.00
    },
    "currencyBreakdown": {
      "EUR": 70.50
    }
  }
  ```

**Sample cURL:**
```bash
curl http://localhost:8080/api/v1/expenses/summary?year=2026&month=10
```

### 4. Get Metadata (Categories, Payment Methods & Currencies)
- **Endpoint:** `GET /api/v1/expenses/metadata`
- **Headers:** `Accept-Language: el` (optional, for localized names in Greek or English)
- **Response:** `200 OK` returning all supported categories, payment methods, supported currencies, default currency, and `localizedName`:
  ```json
  {
    "defaultCurrency": "EUR",
    "currencies": [
      { "key": "EUR", "symbol": "€", "displayName": "Euro (EUR)", "localizedName": "Ευρώ (EUR)" },
      { "key": "USD", "symbol": "$", "displayName": "US Dollar (USD)", "localizedName": "Δολάριο ΗΠΑ (USD)" },
      { "key": "GBP", "symbol": "£", "displayName": "British Pound (GBP)", "localizedName": "Βρετανική Λίρα (GBP)" }
    ],
    "categories": [
      { "key": "GROCERIES", "displayName": "Groceries", "localizedName": "Παντοπωλείο / Σούπερ Μάρκετ" },
      { "key": "FOOD_AND_DINING", "displayName": "Food & Dining", "localizedName": "Φαγητό & Εστίαση" }
    ],
    "paymentMethods": [
      { "key": "CREDIT_CARD", "displayName": "Credit Card", "localizedName": "Πιστωτική Κάρτα" },
      { "key": "CASH", "displayName": "Cash", "localizedName": "Μετρητά" }
    ]
  }
  ```

**Sample cURL (in Greek):**
```bash
curl -H "Accept-Language: el" http://localhost:8080/api/v1/expenses/metadata
```

### 5. Delete an Expense
- **Endpoint:** `DELETE /api/v1/expenses/{id}`
- **Response:** `204 No Content`

**Sample cURL:**
```bash
curl -X DELETE http://localhost:8080/api/v1/expenses/1
```

---

## Internationalization (i18n) & Language Support

The application natively supports **English** and **Greek (Ελληνικά)**:

### 1. Web UI Language Switcher
- A language dropdown in the top-right navbar lets users toggle between **🇬🇧 English** and **🇬🇷 Ελληνικά** with a single click.
- The switcher automatically preserves the user's currently selected month (e.g. `/expenses?month=2026-10&lang=el`).
- The choice is saved into a persistent browser cookie (`EXPENSES_LOCALE` valid for 30 days), remembering the user's preferred language across visits.

### 2. Fully Localized Components
- **Navbar & Page Title:** "Expense Tracker" / "Διαχείριση Εξόδων".
- **Month Calendar Heading:** Dynamic month name based on locale ("Οκτώβριος 2026" / "October 2026").
- **Dashboard Metrics:** "Total Expenses" / "ΣΥΝΟΛΙΚΑ ΕΞΟΔΑ", "Total Transactions" / "ΣΥΝΟΛΟ ΣΥΝΑΛΛΑΓΩΝ", etc.
- **Form Controls & Dropdowns:** All categories (e.g. "Groceries" / "Παντοπωλείο / Σούπερ Μάρκετ"), payment methods (e.g. "Credit Card" / "Πιστωτική Κάρτα"), currencies, and placeholders.
- **Form Validation Errors:** Immediate localized feedback on missing or invalid fields.
- **Flash Alerts:** Action confirmation messages on creation or deletion.
- **Records Table & Actions:** Headers, badges, empty state indicators, and delete confirmation dialogs.

### 3. Resource Bundles
Translations are maintained in standard Spring Boot resource bundles:
- `src/main/resources/messages.properties` (Default / English)
- `src/main/resources/messages_en.properties` (English)
- `src/main/resources/messages_el.properties` (Greek - UTF-8 encoded)

---

## Database Migrations

If you have existing expense records created before currency support was added:

A migration script is provided at:  
[`src/main/resources/db/migration/migrate_expenses_currency.sql`](src/main/resources/db/migration/migrate_expenses_currency.sql)

### How to execute the migration:

```bash
# If using the Docker PostgreSQL container:
docker exec -i expenses-postgres psql -U postgres -d expensedb < src/main/resources/db/migration/migrate_expenses_currency.sql

# If using local psql directly:
psql -h localhost -U postgres -d expensedb -f src/main/resources/db/migration/migrate_expenses_currency.sql
```

The script:
1. Adds the `currency` column (`VARCHAR(10)`) if it does not already exist.
2. Backfills all existing records where `currency IS NULL` to `'EUR'`.
3. Sets `'EUR'` as the default column value.
4. Enforces a `NOT NULL` constraint.

