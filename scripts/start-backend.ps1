param([string]$FrontendOrigin='')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$configPath = Join-Path $projectRoot '.local\config.json'
$jarPath = Join-Path $projectRoot 'backend\target\hms-1.0.0.jar'
$javaPath = 'C:\Program Files\Java\jdk-23\bin\java.exe'
if (!(Test-Path -LiteralPath $configPath)) { throw 'Missing .local/config.json.' }
if (!(Test-Path -LiteralPath $jarPath)) { throw 'Build the backend JAR before running this script.' }
if (!(Test-Path -LiteralPath $javaPath)) { throw "Java was not found at $javaPath" }
$config = Get-Content -LiteralPath $configPath -Raw | ConvertFrom-Json
function Test-LocalPort([int]$Port) {
    $client = New-Object System.Net.Sockets.TcpClient
    try { $task = $client.ConnectAsync('127.0.0.1', $Port); return ($task.Wait(500) -and $client.Connected) }
    catch { return $false }
    finally { $client.Dispose() }
}
if ($config.DB_URL -match '^jdbc:mysql://(?:127\.0\.0\.1|localhost):3307/') {
    if (!(Test-LocalPort 3307)) {
        $mysqlBase = 'C:\Program Files\MySQL\MySQL Server 8.0'
        $mysqlExe = Join-Path $mysqlBase 'bin\mysqld.exe'
        $dataPath = Join-Path $projectRoot '.local\mysql-data'
        if (!(Test-Path -LiteralPath $mysqlExe) -or !(Test-Path -LiteralPath (Join-Path $dataPath 'mysql'))) {
            throw 'The existing local MySQL installation/data directory is missing. No new database was created.'
        }
        Write-Host 'Starting the existing local MySQL database on port 3307...'
        $mysqlArgs = @('--no-defaults', "--basedir=`"$mysqlBase`"", "--datadir=`"$dataPath`"", '--port=3307', '--bind-address=127.0.0.1', '--mysqlx=OFF')
        $mysqlProcess = Start-Process -FilePath $mysqlExe -ArgumentList $mysqlArgs -WindowStyle Hidden -PassThru
        $deadline = (Get-Date).AddSeconds(45)
        while (!(Test-LocalPort 3307)) {
            if ($mysqlProcess.HasExited) { throw "MySQL stopped during startup. Check the .err log in $dataPath" }
            if ((Get-Date) -gt $deadline) { throw "MySQL did not become ready. Check the .err log in $dataPath" }
            Start-Sleep -Milliseconds 500
        }
    }
    Write-Host 'MySQL is available on port 3307.'
}
if (Test-LocalPort 8080) { Write-Host 'A server is already listening on port 8080. Stop it before starting another backend.'; exit 0 }
foreach ($entry in $config.PSObject.Properties) {
    if ($entry.Name -ne 'ROOT_PASSWORD') { [Environment]::SetEnvironmentVariable($entry.Name, [string]$entry.Value, 'Process') }
}
if ($FrontendOrigin) { $env:FRONTEND_ORIGIN=$FrontendOrigin }
Write-Host 'Starting HMS backend at http://127.0.0.1:8080 (Ctrl+C stops the backend).'
& $javaPath -jar $jarPath --server.address=127.0.0.1 --server.forward-headers-strategy=framework
exit $LASTEXITCODE

