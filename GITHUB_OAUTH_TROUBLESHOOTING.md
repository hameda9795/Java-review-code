# GitHub OAuth Connection Troubleshooting Guide

## Problem
Authorization failed with error: "Failed to get GitHub access token"

## Root Cause
The GitHub OAuth credentials (Client ID and Client Secret) are not configured as environment variables.

## Solution Steps

### Step 1: Get Your GitHub OAuth Credentials

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Check if you already have an OAuth App for DevMentor AI
   - If yes, copy the **Client ID** and generate a new **Client Secret**
   - If no, create a new OAuth App:
     - Click **"New OAuth App"**
     - **Application name**: `DevMentor AI Local`
     - **Homepage URL**: `http://localhost:3000`
     - **Authorization callback URL**: `http://localhost:3000/github/callback` (IMPORTANT!)
     - Click **"Register application"**
     - Copy the **Client ID**
     - Click **"Generate a new client secret"** and copy it immediately

### Step 2: Configure Backend Environment Variables

**Option A: Using the Setup Script (Easiest)**

Run the interactive setup script:
```powershell
. .\set-env.ps1
```

This will prompt you for your credentials and set them for the current session.

**Option B: Manual Setup**

In PowerShell (before starting the backend):
```powershell
$env:GITHUB_CLIENT_ID="Ov23liYourActualClientID"
$env:GITHUB_CLIENT_SECRET="your_actual_client_secret_from_github"
```

**Option C: Create a .env file (For persistence)**

Create a file named `.env` in the project root:
```env
GITHUB_CLIENT_ID=Ov23liYourActualClientID
GITHUB_CLIENT_SECRET=your_actual_client_secret_from_github
```

Then load it before starting:
```powershell
Get-Content .env | ForEach-Object {
    $name, $value = $_.split('=')
    Set-Content env:\$name $value
}
```

### Step 3: Configure Frontend Environment Variables

1. Navigate to the frontend directory:
   ```powershell
   cd frontend
   ```

2. Create `.env.local` file:
   ```powershell
   Copy-Item .env.example .env.local
   ```

3. Edit `frontend\.env.local` and add your GitHub Client ID:
   ```env
   NEXT_PUBLIC_GITHUB_CLIENT_ID=Ov23liYourActualClientID
   NEXT_PUBLIC_API_URL=http://localhost:8080/api
   ```

### Step 4: Verify Environment Variables

Before starting the backend, verify the variables are set:
```powershell
if ($env:GITHUB_CLIENT_ID) { 
    Write-Host "✓ GITHUB_CLIENT_ID is set: $($env:GITHUB_CLIENT_ID.Substring(0, 10))..." 
} else { 
    Write-Host "✗ GITHUB_CLIENT_ID is NOT set" -ForegroundColor Red
}

if ($env:GITHUB_CLIENT_SECRET) { 
    Write-Host "✓ GITHUB_CLIENT_SECRET is set (length: $($env:GITHUB_CLIENT_SECRET.Length))" 
} else { 
    Write-Host "✗ GITHUB_CLIENT_SECRET is NOT set" -ForegroundColor Red
}
```

### Step 5: Start the Application

**Backend:**
```powershell
./mvnw spring-boot:run
```

Look for this log message during startup:
```
=== GitHub Client Initialized ===
Client ID: Ov23liB2CG...
Client Secret: SET (length: XX)
```

**Frontend:**
```powershell
cd frontend
npm run dev
```

### Step 6: Test the Connection

1. Open browser at `http://localhost:3000`
2. Login or register
3. Go to **Settings** page
4. Click **"Connect GitHub"**
5. Authorize the app on GitHub
6. You should be redirected back successfully

## Enhanced Error Logging

I've added detailed logging to help debug OAuth issues. When you try to connect now, check the backend logs for:

```
=== Requesting GitHub access token ===
URL: https://github.com/login/oauth/access_token
Code: 0672e0ac2d...
Client ID: Ov23liB2CG...
Sending request to GitHub...
GitHub response status: 200 OK
```

If you see an error, it will show:
- `GitHub OAuth error: [error code]`
- `Error description: [detailed description]`
- `Error URI: [link to GitHub docs]`

## Common Issues

### Issue 1: "bad_verification_code"
**Cause**: The authorization code was already used or expired  
**Solution**: Try the connection process again (codes expire after 10 minutes)

### Issue 2: "incorrect_client_credentials"
**Cause**: Client ID or Secret is wrong  
**Solution**: 
- Verify you copied the credentials correctly
- Generate a new client secret if needed
- Make sure there are no extra spaces

### Issue 3: "redirect_uri_mismatch"
**Cause**: Callback URL doesn't match GitHub OAuth app settings  
**Solution**: 
- Verify your GitHub OAuth app callback URL is: `http://localhost:3000/github/callback`
- Check there are no trailing slashes or typos

### Issue 4: Environment variables not persisting
**Cause**: PowerShell session closed or new terminal opened  
**Solution**: Run the setup script again in the same terminal where you'll start the backend

## Verification Checklist

- [ ] GitHub OAuth App created with correct callback URL
- [ ] Client ID copied correctly
- [ ] Client Secret generated and copied
- [ ] Backend environment variables set (`$env:GITHUB_CLIENT_ID` and `$env:GITHUB_CLIENT_SECRET`)
- [ ] Frontend `.env.local` file created with `NEXT_PUBLIC_GITHUB_CLIENT_ID`
- [ ] Backend shows correct Client ID during startup
- [ ] Both backend and frontend are running
- [ ] Testing from `http://localhost:3000` (not `127.0.0.1`)

## Next Steps

After setting up the environment variables:

1. Restart the backend: `./mvnw spring-boot:run`
2. Try connecting GitHub again
3. Check the backend logs for the detailed OAuth flow
4. If still failing, share the log output that shows:
   - "=== Requesting GitHub access token ==="
   - The GitHub response

## Need Help?

If you're still having issues:
1. Check the logs in `logs/devmentor-ai.log`
2. Look for lines containing "GitHub OAuth" or "access token"
3. Share the error description from GitHub's response
