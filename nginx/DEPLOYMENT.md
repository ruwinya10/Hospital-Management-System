# Local Nginx deployment (Windows)

The app is served at **http://localhost**. Nginx serves the production files from `frontend/dist` and forwards `/api/` to Spring Boot on `127.0.0.1:8080`. MySQL continues to use the existing local database on port 3307. The Vite development server is not required for this deployment.

## Commands

From the project root in PowerShell:

```powershell
# Build the latest frontend/backend and start the deployment (no test suites)
.\scripts\deploy-nginx.ps1

# Start an already built deployment after restarting the computer
.\scripts\start-nginx.ps1

# Stop Nginx; backend and MySQL remain running
.\scripts\stop-nginx.ps1
```

If port 80 is occupied, pass `-Port 8081` to the deploy/start script and open http://localhost:8081. Run the deploy command after code changes; merely starting Nginx does not rebuild the application. The scripts identify the existing HMS process before stopping it for a rebuild and leave the database intact.

Nginx is installed under `.local/nginx-1.31.7` from the official nginx.org Windows ZIP. The source configuration is `nginx/nginx.windows.conf`; the start script substitutes this machine's paths and writes `.local/nginx-hms.conf`. Nginx logs are in `.local/nginx-1.31.7/logs`; background backend logs are in `.local/logs`.

The startup script starts the backend if it is not running. The backend startup script starts the existing MySQL instance if necessary. Nginx and backend run as background applications, not Windows services; use the start script after reboot. Local Nginx configuration is checked with `nginx -t` before starting or reloading.

This deployment binds Nginx to **127.0.0.1 only**. It is accessible from this computer, not a public website or other computers on the network. Public hosting requires a server/domain and an HTTPS deployment; these local scripts do not configure those. The existing `nginx/hms.conf` remains the separate Linux deployment example.
