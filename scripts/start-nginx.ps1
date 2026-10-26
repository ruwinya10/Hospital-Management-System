param([ValidateRange(1,65535)][int]$Port=80)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$nginxHome=Join-Path $projectRoot '.local\nginx-1.31.7'
$nginxExe=Join-Path $nginxHome 'nginx.exe'
$dist=Join-Path $projectRoot 'frontend\dist'
if(!(Test-Path -LiteralPath $nginxExe)){throw 'Nginx is missing. Download nginx-1.31.7.zip from https://nginx.org/download/nginx-1.31.7.zip and extract it into .local.'}
if(!(Test-Path -LiteralPath (Join-Path $dist 'index.html'))){throw 'Frontend build is missing. Run scripts/deploy-nginx.ps1 first.'}
$logs=Join-Path $projectRoot '.local\logs'
New-Item -ItemType Directory -Force -Path $logs | Out-Null
$prefix=$nginxHome.Replace('\','/')+'/'
$configPath=Join-Path $projectRoot '.local\nginx-hms.conf'
$mime=(Join-Path $nginxHome 'conf\mime.types').Replace('\','/')
$config=(Get-Content -LiteralPath (Join-Path $projectRoot 'nginx\nginx.windows.conf') -Raw).Replace('__MIME_TYPES__',$mime).Replace('__FRONTEND_ROOT__',$dist.Replace('\','/')).Replace('__PORT__',[string]$Port)
[System.IO.File]::WriteAllText($configPath,$config,[System.Text.UTF8Encoding]::new($false))
& $nginxExe -p $prefix -c $configPath -t
if($LASTEXITCODE -ne 0){throw 'Nginx configuration check failed.'}
function Test-Backend {
 try{return (Invoke-RestMethod 'http://127.0.0.1:8080/actuator/health' -TimeoutSec 2).status -eq 'UP'}catch{return $false}
}
if(!(Test-Backend)){
 if(Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue){throw 'Port 8080 is occupied, but the backend is not healthy. Check the backend logs before continuing.'}
 $backendScript=Join-Path $PSScriptRoot 'start-backend.ps1'
 $origins=if($Port -eq 80){'http://localhost,http://127.0.0.1'}else{"http://localhost:$Port,http://127.0.0.1:$Port"}
 $shell=(Get-Process -Id $PID).Path
 $backendProcess=Start-Process -FilePath $shell -ArgumentList @('-NoProfile','-File',"`"$backendScript`"",'-FrontendOrigin',"`"$origins`"") -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $logs 'backend.out.log') -RedirectStandardError (Join-Path $logs 'backend.err.log') -PassThru
 $deadline=(Get-Date).AddSeconds(90)
 while(!(Test-Backend)){
  if($backendProcess.HasExited){throw "Backend startup failed. See $logs\backend.out.log and backend.err.log"}
  if((Get-Date) -gt $deadline){throw "Backend startup timed out. See $logs\backend.out.log"}
  Start-Sleep -Milliseconds 500
 }
}
$pidFile=Join-Path $nginxHome 'logs\nginx.pid'
$running=$false
if((Test-Path -LiteralPath $pidFile) -and (Get-Item -LiteralPath $pidFile).Length -gt 0){$savedId=Get-Content -LiteralPath $pidFile -TotalCount 1;if($savedId -and $savedId -match '^\d+$'){$savedProcess=Get-Process -Id ([int]$savedId) -ErrorAction SilentlyContinue;$running=$savedProcess -and $savedProcess.Path -eq $nginxExe}}
if($running){& $nginxExe -p $prefix -c $configPath -s reload;if($LASTEXITCODE -ne 0){throw 'Nginx reload failed.'}}
else{
 if(Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue){throw "Port $Port is already in use. Choose another port with -Port."}
 Start-Process -FilePath $nginxExe -ArgumentList @('-p',"`"$prefix`"",'-c',"`"$configPath`"") -WorkingDirectory $nginxHome -WindowStyle Hidden | Out-Null
}
$url=if($Port -eq 80){'http://localhost'}else{"http://localhost:$Port"}
Write-Host "HMS is served by Nginx at $url"
Write-Host 'Nginx is bound to this computer only. Backend and database remain on their existing local ports.'


