# Architecture

## System Overview

Expense OS should be offline-capable first, cloud-enhanced second.

The Android app owns fast capture, local parsing, local storage, and immediate insights. The backend handles account sync, premium AI, backup, family spaces, and long-running analytics.

## Android App

Recommended structure:

```text
app/
  core/
    database/
    permissions/
    security/
    sync/
    ui/
  features/
    onboarding/
    transactions/
    timeline/
    receiptScanner/
    voiceCapture/
    rewind/
    healthScore/
    assistant/
  ai/
    categorization/
    extraction/
    prompts/
  integrations/
    sms/
    notifications/
    mlkit/
```

## Android Libraries

- Kotlin
- Jetpack Compose
- Room
- WorkManager
- DataStore
- ML Kit Text Recognition
- Firebase Auth
- Firebase Cloud Messaging
- Hilt or Koin for dependency injection

## Local-First Flow

1. SMS/notification/voice/receipt event enters the app.
2. Local parser creates a transaction candidate.
3. Deduplication checks source, amount, timestamp, merchant, and reference IDs.
4. Candidate appears in Transaction Inbox.
5. User confirms or edits.
6. Confirmed transaction updates timeline, score, and insights.
7. Sync job sends encrypted transaction data to backend when enabled.

## Backend Services

Recommended services:

- Auth profile service
- Transaction sync service
- AI insights service
- Rewind generation service
- Subscription detection service
- Family space service, post-MVP

## Backend Stack

Either Node.js or Go is fine.

Choose Node.js if:

- You want faster iteration.
- You expect richer AI orchestration.
- Your team is already JavaScript/TypeScript friendly.

Choose Go if:

- You prioritize lower infrastructure cost.
- You expect high-volume ingestion and background jobs.

Recommended default: TypeScript Node.js with PostgreSQL for MVP.

## Database

Use PostgreSQL with:

- Users
- Accounts
- Transactions
- Transaction candidates
- Categories
- Merchants
- Budgets
- Goals
- Insights
- Rewinds
- Subscriptions

## Sync

The app should never require cloud sync to be useful.

Sync behavior:

- Local writes happen immediately.
- Backend accepts idempotent upserts.
- Each transaction has a stable local UUID.
- Server returns canonical IDs and conflict metadata.
- Conflicts prefer user-confirmed edits over inferred data.

## AI Layer

### On Device

Use for:

- OCR via ML Kit.
- Rule-based SMS parsing.
- Lightweight categorization.
- Voice transcription where Android APIs are available.

### Cloud

Use for:

- Natural-language financial questions.
- Monthly Rewind narration.
- Complex categorization fallback.
- Pattern detection.
- Goal coaching.

## Privacy and Security

Required:

- Clear permission education.
- Local encryption for sensitive transaction data.
- User-controlled cloud backup.
- Raw SMS snippets stored only when needed for debugging or user review.
- Delete/export account controls.

Avoid:

- Selling financial data.
- Training shared models on identifiable user transactions without explicit consent.
- Showing judgmental or shame-heavy copy.

