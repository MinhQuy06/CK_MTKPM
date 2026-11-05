# Strategy pricing execution ledger

Repository: MinhQuy06/CK_MTKPM; checkout: /Users/scara2906/Downloads/MAUTHIETKE/CK_MTKPM; branch thaihoa.

User instruction (2026-11-05): execute #8–#17 sequentially; after each completed ticket, commit and push to thaihoa; never open a PR. This overrides earlier no-push text in the spec/tickets.

## Plan and interfaces

1. #8: finish Standard quote API/UI/tests; editable monochrome Class/Sequence and offline PNG; preserve old booking payload until #14.
2. #9: BenefitRequest entity/repository, authenticated patient request/list API + UI, additive MySQL SQL upgrade, security/validation tests.
3. #10: admin list/detail/decision API + UI, immutable BenefitDecision history; request row lock; approved validity dates.
4. #11: admin revoke and patient renewal via new request; preserve history; enforce transition guards.
5. #12: Insurance Strategy and patient/date eligibility API; quote uses principal patient ID and eligibility, inclusive Vietnam dates.
6. #13: VIP Strategy through same registry; exact 90% example and no stacking; browser options from eligibility.
7. #14: Standard booking snapshot fields + migration + DTO/UI; server revalidation, legacy compatibility, transaction/after-commit and MySQL tests; booking UML pair.
8. #15: discounted booking locks eligible request rows while revalidating; snapshot benefit reference; revoke/booking race tests.
9. #16: stale price recovery with explicit reconfirmation preserving symptoms; API/browser evidence.
10. #17: distinct lost-benefit/lost-slot recovery, complete real browser flow, final UML/report review.

Shared interfaces: PricingContext.calculate(key, fee) → PricingAmounts; quote response carries schedule/key/base/rate/final/currency. Benefit service uses authenticated patient user ID, approved status, and validity of today plus schedule date. Booking snapshot consumes the same pricing and eligibility logic. No shared mutable selected strategy.

## Verification

Primary seam: HTTP APIs with real Spring/H2/MockMvc JWT; browser smoke for rendered user flows. Focused Strategy and asynchronous quote tests are already authorized by the spec. MySQL integration verifies real locks and additive schema changes. Maven uses Java 21 and the installed VS Code Maven when no mvn executable exists.

## Progress

- Initial state: #8 code is uncommitted work from the earlier attempt, not accepted as complete. HEAD 2be71ae; remote thaihoa synchronized before work.
- Ruling: keep working in the user-selected checkout on thaihoa; no new worktree. Preserve existing committed documentation. Exclude generated backend/target and node_modules from commits.
- Ruling: execute inline with tests and one fresh review at the end, since the tickets already have approved scope and order; do not ask for another plan approval.

- #8 complete: 51 Maven tests + 9 Node tests + real Chrome quote smoke; UTC regression RED→GREEN. Two native draw.io PNGs exported offline and visually inspected. No reservation/booking POST during quote.

- #9 complete: patient request/list/detail API and UI; ownership and self-approval guards; 60 Maven + 9 Node tests and real Chrome smoke passed. Additive upgrade 001 supplied.

- #10 complete: Admin review/decision UI/API and append-only decision history; request pessimistic lock; 65 Maven + 9 Node tests and real Chrome approval smoke passed, including conflicting-decision concurrency.

- #11 complete: admin-only revocation, derived expiry state, linked new renewal requests, unchanged original history; 69 Maven + 9 Node tests and Chrome full benefit lifecycle passed.

- #12 complete: operative Insurance Strategy, authenticated patient/date eligibility, pricing-options API/UI; today and appointment-day inclusive boundaries, revoked/other-patient denial tested; 79 Maven + 9 Node and Chrome insurance quote smoke passed.

- #13 complete: VipPricingStrategy registered without changing Context algorithm; 81 Maven + 9 Node tests and fresh-fixture Chrome VIP quote passed. Initial browser timeout after host sleep/clock leaps cleared on a clean fixture rerun; no product change made to mask it.

- Ruling: new clients use POST /appointments/confirm with a server-only quoteRequired marker, while legacy POST remains compatible. This prevents stripping all quote fields from downgrading the strict endpoint; cost is one additive endpoint and the client route update.
- Ruling: discount snapshot uses DECIMAL(19,18) and rejects more than 18 fractional digits rather than silently rounding a configured rate; cost is an explicit precision limit for unusually precise policies.
- #14 complete: server-calculated Standard snapshots, strict quote comparison, legacy compatibility, after-commit email capture, slot races; 95 H2/MySQL tests + 10 Node + real Chrome booking passed; 4 MySQL upgrades preserve old appointment fee/null metadata. Four editable monochrome UML/PNGs verified.

- Ruling: baseline tracked backend/target resources were restored with newer timestamps and Maven kept stale api.js pointing to localhost:8080 (source uses same-origin). Remove generated target files from Git and run clean builds; cost: checkout requires the normal Maven build instead of relying on tracked stale output.

- #15 clean-build check exposed a duplicate DoctorRepository lock declaration introduced during this ticket (the baseline already had it). Removed the duplicate and reused the existing method; clean rebuild is the verification authority, not cached incremental output.

- Ruling: lock existing Doctor row as well as Schedule before pricing, so an in-flight fee update cannot leave a booking using a pre-wait fee; cost is brief serialization of bookings for one doctor while the transaction runs.
- #15 complete: locked benefit revalidation, snapshot grant reference, Insurance/VIP booking, synchronized revocation and fee updates; CLEAN build 107 H2/MySQL tests (0 failed/skipped) + 10 Node tests passed. Five MySQL upgrades and real Chrome two-policy booking/readback passed; later revocation leaves historical fees intact.

- #16 complete: 111 H2/MySQL tests, 10 Node tests and Chrome reconfirmation passed. Real fee changes twice plus cached old-policy rate scenario; no automatic POST, old/new price shown, symptoms retained, explicit final booking snapshot. Sequence and SOLID/explanation updated. Browser regression initially failed waiting for the absent reconfirmation notice, then passed.

- Ruling: retain numeric discountRate for compatibility and add discountRateExact string for browser confirmation, because JavaScript Number cannot preserve all 18 fractional digits. Cost: one additional response field; older numeric-only clients can still receive PRICE_CHANGED for unusually precise rates.

- #17 timezone regression: Chrome at 00:30 Vietnam in Honolulu timezone defaulted to today instead of Vietnam tomorrow (RED). Use Vietnam calendar date plus UTC day arithmetic; align synthetic fixture with same zone.

- Final review (fresh gpt-6-astra): two Important findings confirmed by RED tests: deleted slot has generic 404/no recovery; edited slot time still accepts old quote. One fix pass adds coded missing-slot errors and strict date/start/end binding under Schedule lock; legacy route retained.
- Final: minor (deferred): no direct test of deliberately rolling back after notification event registration. Existing tests observe no email on earlier rejections and real AFTER_COMMIT on success; delivery code unchanged.
- Final: Ruling: existing cancellation/completion timezone behavior stays outside this quote/confirmation change — it does not decide the new Strategy price — cost: those older flows retain their existing timezone rules.
- Final: Ruling: do not add account-activation serialization to pricing locks — no demonstrated changed-flow regression and outside fee/grant work — cost: no new guarantee for an account being disabled during booking.
- Final: Ruling: keep existing Rabbit delivery without outbox — delivery redesign is explicitly excluded — cost: this work does not guarantee eventual email after broker failure.

- Final: fixed deleted-slot recovery — deletedQuotedSlotReturnsRecoveryCodeWithoutBookingOrEmail API and browser-deleted-slot RED→GREEN; full clean suite 118/118.
- Final: fixed unconfirmed moved slot — shiftedQuotedSlotRequiresFreshSelectionBeforeBooking and strictConfirmationCannotStripQuotedSlotTiming RED→GREEN; browser edited-slot and quote-slot-change confirm explicit reselection; full clean suite 118/118.
- #17 complete: 118 H2/MySQL tests (0 failures/errors/skips), 11 Node tests, all 16 isolated Chrome scripts and 5 MySQL upgrade migrations passed. Exactly 4 editable mxGraph draw.io +4 offline PNG verified monochrome and visually inspected; frontend/static copies identical. Final fresh review material findings fixed in one pass; deferred Minor documented. All ten tickets complete.
