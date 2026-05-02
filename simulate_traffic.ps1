Write-Host "Starting Traffic Simulation..."
Write-Host "=============================="

Write-Host "`n[1] Simulating Normal User Traffic (10 requests, 500ms spaced)..."
$normalUrl = "http://localhost:8080/api/public/data"
for($i=1; $i -le 10; $i++) {
    try {
        $response = Invoke-RestMethod -Uri $normalUrl -Method Get
        Write-Host "Success $i/10"
        Start-Sleep -Milliseconds 500
    } catch {
        Write-Host "Request $i failed: $_"
    }
}

Write-Host "`n[2] Simulating Bot Attack (150 fast requests to protected endpoint)..."
$attackUrl = "http://localhost:8080/api/auth/login"
$body = '{"username":"admin", "password":"password"}'
$blockedCount = 0

for($i=1; $i -le 150; $i++) {
    try {
        $response = Invoke-RestMethod -Uri $attackUrl -Method Post -Body $body -ContentType "application/json"
        Write-Host -NoNewline "."
    } catch {
        $blockedCount++
        if ($blockedCount % 10 -eq 0) {
            Write-Host -NoNewline "x"
        }
    }
}

Write-Host "`n`nSimulation Complete. Blocked $blockedCount requests out of 150 during the attack phase."
Write-Host "Check your React Dashboard to see the Live AI Classification!"
