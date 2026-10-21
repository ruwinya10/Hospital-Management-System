# Reports & Analytics

Open **Reports** in the sidebar. It replaces the Documents tab; existing document records and APIs are retained.

| Category | Authorized roles |
|---|---|
| Patient | Administrator, Doctor, Nurse, Receptionist |
| Appointment | Administrator, Doctor, Receptionist |
| Revenue | Administrator, Accountant |
| Pharmacy | Administrator, Pharmacist |
| Laboratory | Administrator, Doctor, Laboratory Staff |
| Staff | Administrator |

Patients do not have login accounts; hospital staff maintain their records. The API checks these permissions independently of the interface. These are module permissions: staff are not restricted to individual assigned patients because staff-to-patient assignments are not stored by this scaffold. Reports omit clinical notes/results, patient contact details and staff leave reasons.

## Generate and export

Choose a category, select dates and optional IDs/status/search, then click **Generate report**. Quick presets include Today, This week, This month, This year and All dates. Trends can group by day, week (Monday start), month or year. Dates use Asia/Colombo; the end date is inclusive. Unsupported filters are rejected. Blank or invalid event dates are excluded, archived records are excluded, and missing/invalid numbers contribute zero.

On-screen tables show 20 rows per page. Print and downloadable PDF/CSV include all matching rows, chart data, summary values, generation time, filters and definitions. CSV is UTF-8 with a BOM for Excel and neutralizes spreadsheet formula prefixes. PDF is a direct download; CSV can be opened in Excel. Print uses the browser print dialog. No external reporting service receives hospital records.

## Calculation rules

- **Patient:** Registration dates use creation timestamps. Admission history contains valid stays overlapping the selected period; inpatient counts are distinct patient IDs in those stays. Outpatient counts require medical records with a visit date and `visitType=OUTPATIENT`. Unknown visit types are not inferred. Age/gender groups use the filtered registration cohort, with age at the selected end date. Department filters require the corresponding department link.
- **Appointment:** Counts use the current appointment date and stored status. Department is taken from the appointment or its linked doctor. Cancellation rate is cancelled / all matching appointments. Rescheduling counts require `RESCHEDULED`; previous appointment dates are not reconstructed.
- **Revenue:** Collected income is the sum of positive payments with ACTIVE, COMPLETED, PAID or APPROVED status and a valid payment date in the selected period. A PAID bill alone is not proof of a payment. Services are allocated proportionally by linked bill charges, using the larger of bill total and summed charges as denominator. Unlinked/unallocated amounts remain Unattributed. Outstanding amounts concern bills issued within the period, less all payments through the end date. Blank bill dates fall back to creation dates. Monetary values use the hospital's accounting currency; a currency is not assumed. Refund accounting is not implemented.
- **Pharmacy:** Inventory is a current snapshot. Date/patient filters affect movements and sales only. Low stock is quantity <= reorder level; expired is before today; soon-to-expire is today through 30 days from today. Dispensing sales are quantity x price captured when logged; they are not proof of payment and are not added again to Revenue. History starts when movements are logged; previous manual quantity changes are not reconstructed.
- **Laboratory:** Requests use `requestedAt` or creation date when blank. Completed-test turnaround uses explicit `requestedAt` and `completedAt` timestamps, never last edit time. Laboratory revenue uses the payment allocation rule; it is omitted for doctor/department/status/text filters because those cannot reliably be attributed to payments. Clinical result text is excluded.
- **Staff:** Employee totals are the current roster, filtered by department, employee ID, name and status. Date filters apply to new hires, attendance and leave overlap. Present/absent totals are unique employee-days. Missing attendance is not treated as absence; use an explicit ABSENT attendance record. Roles use employee `jobTitle`, not account roles. Leave records require valid start/end dates.

## Fields and workflow

The standard forms now include patient department, medical-record visit type, laboratory request/completion timestamps, and bill date. Existing records remain intact; edit them to supply missing information where known. Appointment/lab/attendance status choices now include RESCHEDULED, PROCESSING, PRESENT and ABSENT.

For Pharmacy, create a medicine with its initial stock and expiry date in **Pharmacy Item**. Afterwards use **Reports > Pharmacy > Record stock movement** for RECEIPT, DISPENSE, ADJUST_IN or ADJUST_OUT. Each movement updates stock in a database transaction with a row lock and records who entered it. Dispensing cannot exceed available stock or use an expired/undated medicine. Direct quantity changes on existing medicines are rejected so new changes have history. The initial stock is an opening balance, not a reconstructed movement.

## Verification

- Backend tests: Maven `test` (report calculations, role matrix, dates, missing data and stock movements).
- Frontend tests: in `frontend`, run `npm.cmd test` (CSV escaping/formula protection, complete PDF pagination and empty reports).
- Frontend production build: `npm.cmd run build`.

The current scaffold stores details as JSON and loads category source records from MySQL to aggregate in Java. Large production datasets will need normalized relationships, indexed date queries and database-side aggregation. This module does not establish clinical safety or production readiness.
