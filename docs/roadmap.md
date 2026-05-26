# Roadmap

## Phase 0: Validation

Duration: 1-2 weeks

Deliverables:

- Clickable prototype of onboarding, inbox, timeline, and rewind.
- 50-100 real anonymized SMS samples across JazzCash, Easypaisa, and banks.
- Manual parser benchmark.
- User interviews with students, freelancers, salaried employees, and household managers.

Validation questions:

- Will users grant SMS/notification permissions if the value is clear?
- Which transaction sources matter most?
- How often do users spend cash?
- Do users understand and trust AI-generated insights?

## Phase 1: MVP

Duration: 8-12 weeks

Build:

- Auth and local profile.
- Room database.
- SMS parser.
- Notification parser.
- Voice expense capture.
- Receipt OCR.
- Transaction Inbox.
- Smart Timeline.
- Monthly Rewind.
- Financial Health Score v1.
- Cloud backup optional.

Success criteria:

- 90% amount extraction accuracy for supported message templates.
- 70% automatic capture for active beta users.
- First useful insight in under 5 minutes.
- Week-one retention above 30% in beta.

## Phase 2: Retention and Premium

Duration: 8-10 weeks

Build:

- AI assistant.
- Subscription detector.
- Spending predictions.
- Smart budget auto-creation.
- Goals and coaching.
- Export to CSV/PDF.
- Premium subscription.

## Phase 3: Network and Automation

Duration: 12+ weeks

Build:

- Family spaces.
- Shared household budgets.
- Advanced Urdu support.
- Bank integrations where feasible.
- Business/freelancer mode.
- Merchant intelligence.

## MVP Launch Checklist

- Privacy policy written in plain language.
- Permission screens tested.
- Delete account flow.
- Export data flow.
- Crash reporting.
- Parser analytics dashboard.
- Beta feedback loop.
- App Store listing screenshots.

## Key Risks

### Permission Trust

Users may hesitate to grant SMS/notification access.

Mitigation:

- Explain exactly what is read.
- Process locally where possible.
- Let users disable sources anytime.

### Parser Accuracy

Pakistan transaction messages vary by provider and bank.

Mitigation:

- Start with top providers.
- Build parser confidence and review flow.
- Collect anonymized examples with consent.

### AI Cost

Cloud AI can become expensive.

Mitigation:

- Use structured summaries.
- Cache insights.
- Keep premium features behind subscription.
- Use local rules for frequent tasks.

### Emotional Tone

Finance apps can easily feel judgmental.

Mitigation:

- Write copy like a coach, not a debt collector.
- Explain tradeoffs.
- Celebrate improvements.

