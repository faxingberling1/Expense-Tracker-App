# Parsing Specification

## Parsing Principles

1. Extract amount first.
2. Extract direction second.
3. Extract merchant/counterparty third.
4. Extract date and account references when available.
5. Assign category with confidence.
6. Preserve the raw snippet for user review.
7. Never silently confirm low-confidence entries.

## Transaction Candidate Output

All parsers should produce this normalized object:

```json
{
  "amount": 1200,
  "currency": "PKR",
  "direction": "expense",
  "merchant": "Shell",
  "category": "Fuel",
  "occurredAt": "2026-05-26T18:30:00+05:00",
  "source": "sms",
  "confidence": 0.92,
  "rawText": "..."
}
```

## SMS Patterns

### Easypaisa

Common signals:

- "sent"
- "received"
- "paid"
- "cash in"
- "cash out"
- "balance"
- "fee"
- "transaction id"

Example interpretation:

```text
You have sent Rs. 1,200 to Ali Khan.
```

Output:

```json
{
  "amount": 1200,
  "direction": "expense",
  "merchant": "Ali Khan",
  "category": "Transfers",
  "source": "sms"
}
```

### JazzCash

Common signals:

- "payment of Rs"
- "sent to"
- "received from"
- "merchant payment"
- "bill payment"

### Bank Card

Common signals:

- "debited"
- "credited"
- "purchase"
- "POS"
- "ATM"
- "IBFT"
- "available balance"

Rules:

- "debited", "purchase", "POS", "withdrawn" usually means expense.
- "credited", "received", "salary" usually means income.
- "IBFT" may be transfer unless merchant context exists.

## Notification Patterns

Notification parsers should use package name and text.

Useful package groups:

- Foodpanda: food/dining
- Careem: transport
- Daraz: shopping
- Easypaisa/JazzCash: wallet transaction
- Bank apps: card/bank transaction

## Voice Input

Supported examples:

```text
Spent 1200 on fuel.
Kal 2500 groceries pe kharch kiye.
Received 15000 from Ali.
Paid 800 for lunch.
```

Voice extraction should support:

- English
- Urdu
- Roman Urdu
- Mixed English/Roman Urdu

Key Roman Urdu terms:

- kharch = expense
- diya = paid/gave
- mila = received
- bheja = sent
- petrol/fuel = fuel
- khana/lunch/dinner = food
- kiraya = rent or fare depending context

## Receipt OCR

Use ML Kit to detect text blocks.

Extraction priority:

1. Total amount labels: total, grand total, net amount, payable, amount due.
2. Merchant from top text block.
3. Date from receipt header/footer.
4. Tax labels: GST, sales tax, tax.
5. Line items if confidence is acceptable.

If multiple totals are found, prefer:

- Grand total
- Net total
- Amount paid
- Largest amount near bottom

## Deduplication

Create a dedupe key from:

- Rounded amount
- Direction
- Merchant tokens
- Source reference ID when available
- Timestamp bucket

Two candidates are likely duplicates if:

- Amount matches exactly.
- Direction matches.
- Merchant or provider overlaps.
- Occurred within 10 minutes for digital transactions.
- Occurred within 24 hours for receipt plus SMS/card transaction.

## Confidence Handling

High confidence: 0.85+

- Show as ready to confirm.
- May auto-confirm if user enabled automation for that source.

Medium confidence: 0.60-0.84

- Show in inbox with highlighted uncertain fields.

Low confidence: below 0.60

- Keep as needs review.
- Do not affect score or analytics until confirmed.

