# Render backend + Vercel frontend + Aiven MySQL (free demo)

These files prepare deployment; they do not create provider projects or publish the app automatically. No cloud credentials are stored in this repository. Your local database and Windows Nginx deployment are unchanged. This topology uses Render directly for Java and Vercel for static files, so Nginx is not needed in the cloud.

## 1. Rotate the exposed Aiven password

Reset the `avnadmin` password in your Aiven service before deployment because the previous password was pasted into chat. Do not post the replacement in chat. Enter it directly into Render's Environment settings. Download the **CA certificate** from the Aiven service Overview/Connection information. The online app will use Aiven's `defaultdb` database. No local patient records or local user accounts will be uploaded; the first startup creates the admin account in the fresh cloud database.

## 2. Push the deployment changes to GitHub

Repository: `ruwinya10/Hospital-Management-System`, branch `main`.

Review and commit the new deployment files plus the frontend API configuration changes. Push to the GitHub repository using your normal Git client. Do not force push. `.local`, `.env` secrets, certificate files and generated dependency/build directories are excluded. Keep passwords out of commit messages and repository files.

The root Dockerfile needs the **entire repository as build context** because backend validation packages `frontend/src/formRules.json`. Do not set Render's Root Directory to `backend`.

## 3. Create the Render backend

In Render: **New > Web Service > connect the GitHub repository**.

| Setting | Value |
|---|---|
| Branch | `main` |
| Language / Runtime | Docker |
| Root Directory | Leave blank (repository root) |
| Dockerfile Path | `./Dockerfile` |
| Docker Build Context | `.` (repository root) |
| Instance Type | Free |
| Health Check Path | `/actuator/health` |
| Docker Command | Leave blank |

The optional `render.yaml` Blueprint provides these settings and generates JWT_SECRET. It does not upload the CA certificate or replace the need to enter your own database/admin secrets.

In **Environment**, enter:

| Key | Value |
|---|---|
| `DB_URL` | `jdbc:mysql://YOUR_AIVEN_HOST:19535/defaultdb?sslMode=VERIFY_IDENTITY` (replace host/port using Aiven) |
| `DB_USER` | `avnadmin` (or the service's displayed user) |
| `DB_PASSWORD` | The NEW password after rotating it in Aiven |
| `JWT_SECRET` | A new random secret of at least 32 characters |
| `HMS_ADMIN_PASSWORD` | A new strong admin password, 12–72 characters including a letter and a number |
| `FRONTEND_ORIGIN` | Temporarily `https://frontend-not-configured.invalid`; replace with the actual Vercel production origin in step 5 |
| `SPRING_PROFILES_ACTIVE` | `render` |
| `DB_CA_CERT_FILE` | `/etc/secrets/aiven-ca.pem` |

Use a JDBC URL (`jdbc:mysql://...`), not the `mysql://username:password@...` URI. Do not put passwords into the URL. `sslMode=VERIFY_IDENTITY` uses TLS and verifies the server's certificate and hostname.

In **Environment > Secret Files**, add a file named **aiven-ca.pem** and paste the complete downloaded CA certificate, including BEGIN/END CERTIFICATE lines. The container imports this into a temporary Java truststore at startup; do not commit the certificate or private credentials. You can create the service first, then add the secret file and redeploy if the first startup reports a missing certificate.

For a random JWT secret, run this in your own PowerShell terminal and copy the result directly into Render:

```powershell
$bytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Deploy the backend. Wait for **Live**, then open its `/actuator/health` endpoint. Expected: `{"status":"UP"}`. Copy the actual assigned backend HTTPS URL. If Aiven has an IP allow-list, allow Render's listed outbound addresses; do not disable TLS to work around connection problems.

## 4. Create the Vercel frontend

In Vercel: **Add New > Project > import the same GitHub repository**.

| Setting | Value |
|---|---|
| Framework Preset | Vite |
| Root Directory | `frontend` |
| Build Command | `npm run build` |
| Output Directory | `dist` |
| Install Command | `npm ci` |
| Environment Variable | `VITE_API_BASE_URL=https://YOUR_ACTUAL_RENDER_BACKEND.onrender.com/api` |

Set VITE_API_BASE_URL for Production and any Preview deployments you intend to use. Include `/api` exactly once. It is public configuration, not a password. **Do not add DB_PASSWORD, DB_URL, JWT_SECRET or HMS_ADMIN_PASSWORD to Vercel**; all database access happens in the backend.

Click Deploy and copy the **production** Vercel address. The build deliberately fails on Vercel if the HTTPS API URL is missing or malformed, instead of publishing a frontend that silently calls the wrong server. Local Vite and Nginx continue using `/api` when the variable is not set.

## 5. Connect the exact frontend origin

Back in Render, set FRONTEND_ORIGIN to the actual Vercel production origin, e.g. `https://your-project.vercel.app`, with **no trailing slash and no `/api` path**. Save and deploy. Use the production URL rather than a preview URL. If you later switch to a custom domain, update the allowed origin to that exact domain; do not use a wildcard for all Vercel projects.

Open the Vercel production URL and sign in as **admin** with the HMS_ADMIN_PASSWORD you chose. That Vercel URL is the application address to share. Visitors do not need your computer or VS Code running.

## Changes and limits

Vite embeds VITE_API_BASE_URL at build time: changing it requires redeploying Vercel. Render environment changes also require a redeploy. Free Render instances sleep after inactivity and may take about a minute to wake; use sample data only. Render's local filesystem is ephemeral, while database records are stored in Aiven. This setup does not add persistent document uploads.

The Docker image uses Java 21, runs as an unprivileged user, follows Render's PORT environment variable, and limits JVM/container connection usage for the free instance. Tests are skipped during the image build. A Docker engine is not installed in the current local environment, so the actual image build must be verified by Render's deployment logs.

Official references:
- https://render.com/docs/docker
- https://render.com/docs/configure-environment-variables
- https://vercel.com/docs/frameworks/frontend/vite
- https://vite.dev/guide/env-and-mode
- https://dev.mysql.com/doc/connector-j/en/connector-j-reference-using-ssl.html

