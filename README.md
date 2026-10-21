# Hospital Management System — Starter implementation

Stack: React 19 / Vite / Tailwind CSS, Java 21 / Spring Boot 3.4 / Spring Security JWT / Spring Data JPA, MySQL 8, optional Nginx.

## IMPORTANT SCOPE AND SAFETY
This is an **unverified development scaffold**, NOT a finished or production-ready hospital system. It provides CRUD screens and APIs for the principal modules, a simple dashboard, role-based access, authentication, password change, soft archive, basic audit entries, and sample backup scripts. **Do not store real patient data.**

**Missing / incomplete requirements** include: proper relational foreign keys and constraints between clinical entities; patient-document binary upload/download; prescription-to-pharmacy dispensing workflow; invoice calculation and receipt PDF/printing; appointment conflict detection and calendar view; actual today-specific appointments and revenue calculations;  admissions bed occupancy rules; large-dataset reporting optimization; audit read/download coverage; daily scheduled backups, weekly full backups, restore verification, HA/failover and disaster recovery; per-patient and per-doctor record-level authorization; session idle-time handling (JWT fixed 30-minute expiry instead); thorough input validation; encrypted clinical fields and comprehensive security testing. Current UI uses generic field-driven CRUD and is not a clinically safe EMR.

## Local setup
1. Install Java 21, Maven 3.9+, MySQL 8+, Node.js 20+.
2. Run `database/create.sql` in MySQL; change the development password and configure matching DB credentials.
3. Set environment variables `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (random secret at least 32 characters), and `HMS_ADMIN_PASSWORD` (12+ characters). Optional `DB_URL` and `FRONTEND_ORIGIN`.
4. In `backend`, run `mvn spring-boot:run`. First startup creates roles and a seeded `admin` account with your `HMS_ADMIN_PASSWORD`.
5. In `frontend`, run `npm install` and `npm run dev`; open http://localhost:5173. Vite proxies `/api` to Spring Boot on port 8080.
6. Log in as `admin`. Create other staff accounts under Users. Admin cannot be registered publicly.
7. For Nginx deployment, build frontend with `npm run build`, copy `dist/*` to `/var/www/hms/`, and adapt `nginx/hms.conf` for your server. Enable HTTPS/TLS and restrict access before any real deployment.

## Implementation details
All operational records are JPA entities in separate tables, but currently store module-specific properties as JSON in `details` rather than normalized columns. `name`, `status`, and timestamps are regular columns. IDs referenced in `details` are not foreign keys and are not validated. Soft delete changes status to ARCHIVED. CRUD access is module-role based, not record-level.

## Next engineering milestones
1. Normalize patient/doctor/appointment/admission/lab/billing relationships, with foreign keys and transactions.
2. Implement real clinical workflows, calculations, conflict rules, reporting and alerts.
3. Implement secure document upload and file access with malware checks.
4. Add record-level permissions, audit trails for sensitive reads, encryption, tests and regulatory/privacy review.
5. Implement scheduled backups, weekly full backup, restore drills, monitoring, HA, and disaster recovery.

## Backups
`scripts/backup.sh` is a manual MySQL dump with 30-day cleanup. Schedule it using cron/Task Scheduler for daily runs; configure separate weekly full backups and offsite retention. `scripts/restore.sh` restores a chosen dump (destructive; use only after testing on a staging database). **Neither scheduling nor backup verification is implemented.**

## Staff accounts
All accounts belong to hospital staff. Administrators create accounts under **Users**, choosing a staff role and an initial password. Staff can change their own passwords under **Profile settings**. Patient records are maintained by staff; patients do not have login accounts or a patient portal. Existing patient-only accounts are blocked from authentication.

## Reports
See [Reports & Analytics](REPORTS.md) for category permissions, calculation definitions, stock movements, filters and exports.

