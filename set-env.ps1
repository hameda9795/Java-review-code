# DevMentor AI - Environment Variables Setup
# Run this script before starting the backend: . .\set-env.ps1

Write-Host "=== DevMentor AI Environment Setup ===" -ForegroundColor Cyan
Write-Host ""

# GitHub OAuth Configuration
Write-Host "Setting up GitHub OAuth credentials..." -ForegroundColor Yellow
Write-Host ""
Write-Host "If you don't have a GitHub OAuth App yet, create one at:" -ForegroundColor Gray
Write-Host "https://github.com/settings/developers" -ForegroundColor Gray
Write-Host ""
Write-Host "Callback URL should be: http://localhost:3000/github/callback" -ForegroundColor Gray
Write-Host ""

# Prompt for GitHub credentials
$clientId = Read-Host "Enter your GitHub Client ID"
$clientSecret = Read-Host "Enter your GitHub Client Secret" -AsSecureString
$clientSecretPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($clientSecret)
)

# Set environment variables
$env:GITHUB_CLIENT_ID = $clientId
$env:GITHUB_CLIENT_SECRET = $clientSecretPlain

Write-Host ""
Write-Host "✓ Environment variables set successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Client ID: $($clientId.Substring(0, [Math]::Min(10, $clientId.Length)))..." -ForegroundColor Gray
Write-Host "Client Secret: ****** (length: $($clientSecretPlain.Length))" -ForegroundColor Gray
Write-Host ""
Write-Host "You can now start the backend with:" -ForegroundColor Cyan
Write-Host "  ./mvnw spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "Note: These variables are only set for this PowerShell session." -ForegroundColor Yellow
Write-Host "You'll need to run this script again if you close the terminal." -ForegroundColor Yellow
