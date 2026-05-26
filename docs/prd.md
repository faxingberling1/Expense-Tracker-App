# Product Requirements: Expense OS MVP

## Product Positioning

Expense OS is an AI-powered personal finance companion that automatically captures spending and explains money behavior in plain language.

It should feel closer to a personal money coach than accounting software.

## Target Users

### Primary

Young professionals, freelancers, students, and small household managers in Pakistan who:

- Use cash, Easypaisa, JazzCash, and bank cards.
- Receive useful transaction information through SMS and app notifications.
- Want to know where money went without maintaining spreadsheets.
- Feel guilt or confusion around spending.

### Secondary

Couples, families, roommates, and small business operators who need shared visibility later.

## Core User Problem

Most users do not fail at budgeting because they lack charts. They fail because expense capture is annoying, spending is emotionally loaded, and existing apps feel like bookkeeping.

Expense OS solves this by reducing logging effort and turning transactions into understandable behavior.

## MVP Goals

1. Capture at least 70% of a user's monthly transactions automatically.
2. Let users add a cash transaction in under 5 seconds.
3. Give users at least one meaningful insight within the first session.
4. Make monthly spending emotionally legible through a timeline and rewind.
5. Create a repeat-use loop with score, trends, and habit nudges.

## MVP Feature Scope

### 1. Onboarding

Required:

- Phone or Google sign-in.
- Currency defaults to PKR.
- Permission education for SMS, notifications, microphone, and camera.
- User chooses preferred language: English, Urdu, or Roman Urdu.
- User selects top providers they use: JazzCash, Easypaisa, HBL, Meezan, Foodpanda, Careem, Daraz, other banks.

Not in MVP:

- Full bank account linking.
- Investment portfolio setup.

### 2. Transaction Inbox

The inbox holds extracted transactions before confirmation.

States:

- Suggested
- Confirmed
- Ignored
- Duplicate
- Needs review

Each extracted transaction should include:

- Merchant or counterparty
- Amount
- Direction: expense, income, transfer, refund, unknown
- Date and time
- Source: SMS, notification, receipt, voice, manual
- Category
- Confidence score
- Raw source snippet

### 3. SMS and Notification Extraction

The app should parse transaction messages from:

- Easypaisa
- JazzCash
- Major banks
- Food delivery
- Ride hailing
- Ecommerce

The MVP should start with rule-based templates plus fallback AI categorization.

### 4. Voice Expense Logging

Examples:

- "Spent 1200 on fuel."
- "Kal 2500 groceries pe kharch kiye."
- "Received 15000 from Ali."

The app should create a transaction draft with amount, merchant/category, direction, and date.

### 5. Receipt Scanner

User takes or uploads a receipt image.

The app extracts:

- Merchant
- Total amount
- Tax if available
- Date
- Line items when readable
- Suggested category

MVP success does not require perfect line-item extraction. Total and merchant accuracy matter more.

### 6. Smart Timeline

Timeline groups spending by:

- Day
- Category
- Merchant
- Source
- Emotional pattern where available

The UI should avoid spreadsheet density. The first screen should show:

- This month's spend
- Remaining budget estimate
- Top 3 category changes
- Recent confirmed and suggested transactions

### 7. Monthly Rewind

Generated at month end or on demand.

Cards:

- Biggest expense
- Most repeated merchant
- Fastest-growing category
- Subscription or recurring leak
- High-risk spending day/time
- Best saving behavior
- One plain-language coaching suggestion

### 8. Financial Health Score

Score range: 0-100.

Initial inputs:

- Savings ratio
- Budget adherence
- Subscription load
- Impulse spending signals
- Income volatility
- Recurring debt or loan repayments
- Number of unresolved transaction drafts

The score must explain itself. A user should see why it changed.

## Out of Scope for MVP

- Direct bank integrations.
- Investment tracking.
- Loan marketplace.
- Tax filing.
- Family shared spaces.
- Geo-aware prompts.
- Full offline LLM assistant.

## Success Metrics

- Activation: user confirms 5 transactions in first 24 hours.
- Automation: 70% of transactions come from SMS, notification, receipt, or voice.
- Retention: user opens app at least 3 times in first week.
- Insight value: user taps or saves at least one Rewind/insight card.
- Accuracy: 90% amount extraction accuracy for supported SMS templates.

