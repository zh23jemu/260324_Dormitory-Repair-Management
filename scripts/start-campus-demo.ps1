param(
    [switch]$NoOpenBrowser
)

# Campus demo starter.
# This script starts only the two Vite frontends in demo/mock mode.
# It does not start Spring Boot, MySQL, or any backend service.

$ErrorActionPreference = "Stop"

$RootDir = Resolve-Path (Join-Path $PSScriptRoot "..")
$ExampleRoot = Join-Path $RootDir "Campus logistics warranty system"
$AdminDir = Join-Path $ExampleRoot "campus-logistics-warranty-admin"
$UserDir = Join-Path $ExampleRoot "campus-logistics-warranty-user"

$AdminPort = 8081
$UserPort = 8082

function Assert-DirectoryExists {
    param(
        [string]$Path,
        [string]$Name
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Container)) {
        throw "$Name directory not found: $Path"
    }
}

function Assert-CommandExists {
    param(
        [string]$CommandName
    )

    if (-not (Get-Command $CommandName -ErrorAction SilentlyContinue)) {
        throw "Command not found: $CommandName. Please install Node.js and make sure npm is available in PowerShell."
    }
}

function Ensure-NodeModules {
    param(
        [string]$ProjectDir,
        [string]$ProjectName
    )

    $NodeModulesDir = Join-Path $ProjectDir "node_modules"

    if (-not (Test-Path -LiteralPath $NodeModulesDir -PathType Container)) {
        Write-Host "[$ProjectName] node_modules not found. Running npm install..." -ForegroundColor Yellow
        Push-Location $ProjectDir
        try {
            npm install
        }
        finally {
            Pop-Location
        }
    }
}

function Start-DemoFrontend {
    param(
        [string]$ProjectDir,
        [string]$ProjectName,
        [int]$Port
    )

    $Command = @"
`$Host.UI.RawUI.WindowTitle = '$ProjectName - Demo Mode'
Set-Location -LiteralPath '$ProjectDir'
`$env:VITE_DEMO_MODE = 'true'
`$env:VITE_APP_PORT = '$Port'
Write-Host '$ProjectName demo mode starting on port $Port' -ForegroundColor Cyan
npm run dev
"@

    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-Command", $Command
    )
}

Assert-DirectoryExists -Path $ExampleRoot -Name "Example root"
Assert-DirectoryExists -Path $AdminDir -Name "Admin frontend"
Assert-DirectoryExists -Path $UserDir -Name "User frontend"
Assert-CommandExists -CommandName "npm"

Ensure-NodeModules -ProjectDir $AdminDir -ProjectName "Admin"
Ensure-NodeModules -ProjectDir $UserDir -ProjectName "User"

Start-DemoFrontend -ProjectDir $AdminDir -ProjectName "Campus Demo Admin" -Port $AdminPort
Start-DemoFrontend -ProjectDir $UserDir -ProjectName "Campus Demo User" -Port $UserPort

$AdminUrl = "http://localhost:$AdminPort"
$UserUrl = "http://localhost:$UserPort"

Write-Host ""
Write-Host "Campus demo mode started." -ForegroundColor Green
Write-Host "Admin: $AdminUrl"
Write-Host "User : $UserUrl"
Write-Host ""
Write-Host "This script uses frontend mock data only. Close the two new PowerShell windows to stop the demo."

if (-not $NoOpenBrowser) {
    Start-Process $AdminUrl
    Start-Process $UserUrl
}
