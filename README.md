# Expense OS

Expense OS is an Android-first AI personal finance companion for emerging markets, starting with Pakistan.

The product is not positioned as another manual expense tracker. Its wedge is automatic transaction capture from SMS, notifications, receipts, and voice, then plain-language behavioral insights that help users understand why their money disappears.

## MVP Promise

> Your money, automatically understood.

Within the first session, a user should be able to:

1. Grant SMS/notification access.
2. Import recent JazzCash, Easypaisa, bank, Foodpanda, Careem, and Daraz transactions.
3. Add a cash expense by voice.
4. Scan one receipt.
5. See a smart monthly timeline and one meaningful AI insight.

## MVP Modules

- Automatic transaction extraction from SMS and notifications.
- Voice expense logging for cash-heavy spending.
- Receipt OCR with merchant, amount, category, tax, and date extraction.
- AI-assisted categorization and duplicate detection.
- Smart monthly timeline with category groups and weekly mood.
- Monthly Rewind with spending patterns, leaks, and behavior summaries.
- Financial Health Score based on savings ratio, overspending, recurring costs, and impulse signals.

## Primary Market

Pakistan-first:

- Easypaisa
- JazzCash
- HBL
- Meezan Bank
- Bank Alfalah
- UBL
- Foodpanda
- Careem
- Daraz
- Urdu and Roman Urdu support
- Cash-first expense behavior

## Stack

- Android: Kotlin + Jetpack Compose
- Local storage: Room
- OCR: ML Kit Text Recognition
- On-device categorization: lightweight rules + optional Gemini Nano where available
- Backend: Node.js or Go
- Database: PostgreSQL
- Auth and notifications: Firebase Auth + Firebase Cloud Messaging
- AI assistant: OpenAI API for premium/cloud insights

## File Map

- [settings.gradle.kts](settings.gradle.kts) - Android Gradle project entry.
- [app/build.gradle.kts](app/build.gradle.kts) - Android app module configuration.
- [app/src/main/java/com/expenseos/app/MainActivity.kt](app/src/main/java/com/expenseos/app/MainActivity.kt) - Compose app launcher.
- [docs/prd.md](docs/prd.md) - Product requirements and MVP scope.
- [docs/architecture.md](docs/architecture.md) - Android, backend, AI, and sync architecture.
- [docs/data-model.md](docs/data-model.md) - Core entities and relationships.
- [docs/parsing-spec.md](docs/parsing-spec.md) - SMS, notification, voice, and receipt parsing rules.
- [docs/ai-assistant.md](docs/ai-assistant.md) - Assistant behavior, guardrails, and example prompts.
- [docs/roadmap.md](docs/roadmap.md) - Phased delivery plan and success metrics.
- [docs/revenue.md](docs/revenue.md) - Freemium model and packaging.

## Android Scaffold

The first Android scaffold includes:

- Kotlin + Jetpack Compose app module.
- Material 3 UI theme.
- MVP home screen with Financial Health, Timeline, Inbox, and Rewind tabs.
- Domain models for categories, transaction candidates, sources, directions, and status.
- SMS and voice transaction parser stubs with Pakistan-first examples.
- Sample repository data that demonstrates the first product loop.

## Running Locally

Open this folder in Android Studio, let Gradle sync, then run the `app` configuration on an emulator or Android device.

This environment does not currently have Java or Gradle on PATH, so the scaffold was created but not compiled here.
