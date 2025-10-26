# GitHub OAuth Integration Setup Guide

This guide will help you set up GitHub OAuth integration for the DevMentor AI application.

## Overview

The GitHub integration allows users to:
- Authenticate via their GitHub account
- Connect GitHub to existing accounts
- Analyze entire GitHub repositories
- Access private repositories (with proper permissions)

## Prerequisites

1. A GitHub account
2. The application running locally (backend on port 8080, frontend on port 3000)

## Step 1: Create a GitHub OAuth App

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click **"New OAuth App"** or **"Register a new application"**
3. Fill in the application details:
   - **Application name**: `DevMentor AI Local` (or your preferred name)
   - **Homepage URL**: `http://localhost:3000`
   - **Authorization callback URL**: `http://localhost:3000/github/callback`
   - **Application description**: (optional)
4. Click **"Register application"**
5. Copy the **Client ID** - you'll need this for both frontend and backend
6. Click **"Generate a new client secret"** and copy the **Client Secret** - you'll need this for the backend

⚠️ **Important**: Never commit your client secret to version control!

## Step 2: Configure Backend

### Option A: Using Environment Variables (Recommended)

In PowerShell:
```powershell
$env:GITHUB_CLIENT_ID="your_github_client_id"
$env:GITHUB_CLIENT_SECRET="your_github_client_secret"
```

In Bash/Linux:
```bash
export GITHUB_CLIENT_ID="your_github_client_id"
export GITHUB_CLIENT_SECRET="your_github_client_secret"
```

### Option B: Update application.yml

⚠️ **Not recommended for production** - only for local testing

Edit `src/main/resources/application.yml`:

```yaml
github:
  oauth:
    client-id: your_github_client_id
    client-secret: your_github_client_secret
```

## Step 3: Configure Frontend

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Create a `.env.local` file (copy from `.env.example`):
   ```bash
   cp .env.example .env.local
   ```

3. Edit `.env.local` and add your GitHub Client ID:
   ```env
   NEXT_PUBLIC_GITHUB_CLIENT_ID=your_github_client_id
   NEXT_PUBLIC_API_URL=http://localhost:8080/api
   ```

## Step 4: Restart the Application

### Backend
```powershell
# Stop the current backend if running (Ctrl+C)
# Then restart with:
./mvnw spring-boot:run
```

### Frontend
```powershell
# In the frontend directory
npm run dev
```

## Step 5: Test the Integration

1. Open your browser and navigate to `http://localhost:3000`
2. Log in to your account (or create a new one)
3. Go to **Settings** page
4. Click **"Connect GitHub"** button
5. You'll be redirected to GitHub to authorize the application
6. After authorization, you'll be redirected back to the settings page
7. You should see "GitHub account connected" message

## Usage

Once connected, you can:

1. **Create a review from GitHub**:
   - Go to the "New Review" page
   - Enter a GitHub repository (format: `owner/repo`, e.g., `facebook/react`)
   - The system will automatically fetch and analyze the Java files

2. **Analyze repositories**:
   - The backend will fetch up to 20 Java files from the repository
   - You can analyze both public and private repositories (if you have access)

## Troubleshooting

### "Failed to get GitHub access token"
- Verify your `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` are correct
- Check that the callback URL in your GitHub OAuth app matches: `http://localhost:3000/github/callback`

### "GitHub account not connected" error when analyzing repositories
- Make sure you've connected your GitHub account in Settings
- Try disconnecting and reconnecting your GitHub account

### Redirect issues
- Ensure your GitHub OAuth app's callback URL is exactly: `http://localhost:3000/github/callback`
- Check that frontend is running on port 3000

### CORS errors
- Verify the backend's CORS configuration allows `http://localhost:3000`
- Check `SecurityConfig.java` includes the correct origins

## Security Notes

1. **Client Secret**: Never expose your GitHub Client Secret in frontend code or commit it to version control
2. **Access Tokens**: User access tokens are stored encrypted in the database
3. **Scopes**: The application requests these GitHub scopes:
   - `user:email` - Access user email
   - `read:user` - Read user profile
   - `repo` - Access repositories (both public and private)

## Production Deployment

For production deployment:

1. Update the GitHub OAuth App with production URLs:
   - **Homepage URL**: `https://yourdomain.com`
   - **Callback URL**: `https://yourdomain.com/github/callback`

2. Set environment variables on your production server:
   ```bash
   GITHUB_CLIENT_ID=your_production_client_id
   GITHUB_CLIENT_SECRET=your_production_client_secret
   ```

3. Update frontend environment variables:
   ```env
   NEXT_PUBLIC_GITHUB_CLIENT_ID=your_production_client_id
   NEXT_PUBLIC_API_URL=https://api.yourdomain.com/api
   ```

## API Endpoints

The following GitHub-related endpoints are available:

- `POST /api/github/oauth/callback` - Handle OAuth callback (public)
- `GET /api/github/repos/{owner}/{repo}` - Get repository info
- `POST /api/github/repos/{owner}/{repo}/analyze` - Analyze repository
- `POST /api/github/repos/{owner}/{repo}/files` - Get specific files
- `DELETE /api/github/disconnect` - Disconnect GitHub account

All endpoints except the callback require authentication.
