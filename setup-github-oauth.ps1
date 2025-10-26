# GitHub OAuth Setup Script for PowerShell
# Run this script to set up your environment variables

# Replace these with your actual GitHub OAuth app credentials
$GITHUB_CLIENT_ID = "your_github_client_id_here"
$GITHUB_CLIENT_SECRET = "your_github_client_secret_here"

# Set environment variables for current session
$env:GITHUB_CLIENT_ID = $GITHUB_CLIENT_ID
$env:GITHUB_CLIENT_SECRET = $GITHUB_CLIENT_SECRET

Write-Host "GitHub OAuth environment variables set successfully!" -ForegroundColor Green
Write-Host "GITHUB_CLIENT_ID: $GITHUB_CLIENT_ID" -ForegroundColor Cyan
Write-Host "GITHUB_CLIENT_SECRET: $($GITHUB_CLIENT_SECRET.Substring(0, 4))..." -ForegroundColor Cyan
Write-Host ""
Write-Host "Note: These variables are only set for the current PowerShell session." -ForegroundColor Yellow
Write-Host "You'll need to run this script again if you close this terminal." -ForegroundColor Yellow
