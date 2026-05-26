# Data Model

## User

```text
id
phone_number
email
display_name
preferred_language
currency_code
country_code
created_at
updated_at
```

## Transaction

```text
id
user_id
local_id
amount
currency_code
direction
category_id
merchant_id
account_id
source
occurred_at
confirmed_at
notes
raw_source_id
confidence
created_at
updated_at
deleted_at
```

Direction values:

- expense
- income
- transfer
- refund
- unknown

Source values:

- sms
- notification
- receipt
- voice
- manual
- import

## Transaction Candidate

```text
id
user_id
amount
currency_code
direction
suggested_category_id
suggested_merchant_id
source
raw_text
raw_image_uri
occurred_at
confidence
status
dedupe_key
created_at
updated_at
```

Status values:

- suggested
- confirmed
- ignored
- duplicate
- needs_review

## Merchant

```text
id
user_id
canonical_name
display_name
merchant_type
normalized_tokens
created_at
updated_at
```

## Category

```text
id
user_id
name
parent_category_id
icon
color
is_system
created_at
updated_at
```

Default categories:

- Food
- Groceries
- Transport
- Fuel
- Shopping
- Bills
- Subscriptions
- Family
- Health
- Education
- Business
- Transfers
- Income
- Other

## Budget

```text
id
user_id
category_id
period
limit_amount
suggested_amount
created_by
created_at
updated_at
```

Created by values:

- user
- ai
- system

## Insight

```text
id
user_id
type
title
body
severity
related_transaction_ids
related_category_ids
period_start
period_end
created_at
expires_at
```

Insight types:

- overspend
- subscription_leak
- payday_effect
- late_night_spending
- savings_progress
- recurring_merchant
- category_spike
- goal_warning

## Financial Health Snapshot

```text
id
user_id
score
savings_ratio_score
budget_score
subscription_score
impulse_score
debt_score
data_quality_score
period_start
period_end
explanation
created_at
```

## Rewind

```text
id
user_id
period_start
period_end
summary
cards_json
share_image_uri
created_at
```

