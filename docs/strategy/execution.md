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
