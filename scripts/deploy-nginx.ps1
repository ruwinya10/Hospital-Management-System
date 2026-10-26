param([ValidateRange(1,65535)][int]$Port=80)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$javaHome='C:\Program Files\Java\jdk-23'
$maven=Join-Path $projectRoot '.local\apache-maven-3.9.9\bin\mvn.cmd'
if(!(Test-Path -LiteralPath $maven)){throw 'Local Maven installation is missing.'}
Write-Host 'Building the frontend...'
& npm.cmd run build --prefix frontend
if($LASTEXITCODE -ne 0){throw 'Frontend build failed.'}
$listener=Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if($listener){
 $backend=Get-CimInstance Win32_Process -Filter "ProcessId=$($listener.OwningProcess)"
 $jar=Join-Path $projectRoot 'backend\target\hms-1.0.0.jar'
 if($backend.CommandLine -notlike '*-jar backend/target/hms-1.0.0.jar*' -and $backend.CommandLine -notlike "*-jar *$jar*"){throw 'Port 8080 belongs to another application. Stop it or choose a backend port manually.'}
 Write-Host 'Stopping the old HMS backend so its JAR can be rebuilt...'
 Stop-Process -Id $listener.OwningProcess -ErrorAction Stop
}
$env:JAVA_HOME=$javaHome
$env:PATH="$javaHome\bin;$env:PATH"
Write-Host 'Packaging the backend (tests skipped)...'
& $maven -B -ntp -o -f backend/pom.xml "-Dmaven.repo.local=$projectRoot\.local\m2" package -DskipTests
if($LASTEXITCODE -ne 0){throw 'Backend build failed. Existing data has not been changed.'}
& (Join-Path $PSScriptRoot 'start-nginx.ps1') -Port $Port
