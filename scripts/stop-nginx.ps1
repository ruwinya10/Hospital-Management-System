$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
$nginxHome=Join-Path $projectRoot '.local\nginx-1.31.7'
$nginxExe=Join-Path $nginxHome 'nginx.exe'
$pidFile=Join-Path $nginxHome 'logs\nginx.pid'
if(!(Test-Path -LiteralPath $pidFile)){Write-Host 'Project Nginx is not running.';exit 0}
$savedId=Get-Content -LiteralPath $pidFile -TotalCount 1
if(!$savedId){Write-Host 'Project Nginx is not running.';exit 0}
if($savedId -notmatch '^\d+$'){throw 'Invalid Nginx PID file.'}
$process=Get-Process -Id ([int]$savedId) -ErrorAction SilentlyContinue
if(!$process -or $process.Path -ne $nginxExe){throw 'The saved PID does not belong to this project Nginx instance.'}
& $nginxExe -p ($nginxHome.Replace('\','/')+'/') -c (Join-Path $projectRoot '.local\nginx-hms.conf') -s quit
if($LASTEXITCODE -ne 0){throw 'Nginx could not stop gracefully.'}
Write-Host 'Nginx is stopping. The backend and MySQL are left running.'


