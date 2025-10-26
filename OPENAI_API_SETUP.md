# OpenAI API Key Setup Guide

## Quick Setup

### Step 1: Get Your OpenAI API Key

1. Visit https://platform.openai.com/api-keys
2. Sign in (or create an account if you don't have one)
3. Click **"Create new secret key"**
4. Give it a name (e.g., "DevMentor AI")
5. Copy the key immediately (starts with `sk-...`)
   - ⚠️ **Important**: You won't be able to see it again!

### Step 2: Set the Environment Variable

In your PowerShell terminal (in the project root):

```powershell
$env:OPENAI_API_KEY="sk-your-actual-api-key-here"
```

Replace `sk-your-actual-api-key-here` with your actual API key.

### Step 3: Verify It's Set

```powershell
if ($env:OPENAI_API_KEY) {
    Write-Host "✓ OPENAI_API_KEY is set (length: $($env:OPENAI_API_KEY.Length))" -ForegroundColor Green
} else {
    Write-Host "✗ OPENAI_API_KEY is NOT set" -ForegroundColor Red
}
```

### Step 4: Start the Backend

With the environment variable set in the same terminal:

```powershell
./mvnw spring-boot:run
```

## Complete Environment Setup

For convenience, here's how to set all required environment variables:

```powershell
# GitHub OAuth
$env:GITHUB_CLIENT_ID="your_github_client_id"
$env:GITHUB_CLIENT_SECRET="your_github_client_secret"

# OpenAI API
$env:OPENAI_API_KEY="sk-your-openai-api-key"

# Then start the backend
./mvnw spring-boot:run
```

## Alternative: Use the Interactive Setup Script

You can also use the provided setup script:

```powershell
. .\set-env.ps1
```

Then manually add the OpenAI key:
```powershell
$env:OPENAI_API_KEY="sk-your-openai-api-key"
```

## Configuration Details

The application uses these OpenAI settings (from `application.yml`):

```yaml
ai:
  openai:
    api-key: ${OPENAI_API_KEY:your-openai-api-key}
    model: gpt-4o                    # GPT-4 Optimized model
    temperature: 0.3                 # Low temperature for consistent code reviews
    max-tokens: 4000                 # Maximum tokens per request

  embedding:
    model: text-embedding-3-small    # For embeddings (if needed)
    dimension: 1536
```

## Troubleshooting

### Error: "Incorrect API key provided"

This means the API key is either:
- Not set as an environment variable
- Set incorrectly
- Invalid or expired

**Solution**: 
1. Generate a new API key at https://platform.openai.com/api-keys
2. Set it in your terminal
3. Restart the backend in the same terminal

### Error: "Insufficient quota"

This means your OpenAI account doesn't have enough credits.

**Solution**:
1. Go to https://platform.openai.com/account/billing
2. Add credits to your account
3. Check your usage limits

### Environment variable not persisting

Environment variables set with `$env:` only last for the current PowerShell session.

**Solutions**:
1. **Use the same terminal** - Don't close the terminal where you set the variables
2. **Create a startup script** - Make a `start.ps1` file:
   ```powershell
   # start.ps1
   $env:GITHUB_CLIENT_ID="your_id"
   $env:GITHUB_CLIENT_SECRET="your_secret"
   $env:OPENAI_API_KEY="sk-your-key"
   
   Write-Host "Environment variables set!" -ForegroundColor Green
   ./mvnw spring-boot:run
   ```
   
   Then run: `.\start.ps1`

3. **Set permanently** (Windows System Environment Variables):
   - Search for "Environment Variables" in Windows
   - Add to "User variables" or "System variables"
   - Restart PowerShell

## Cost Considerations

### GPT-4 Pricing (as of Oct 2025)

The application uses `gpt-4o` model:
- **Input**: ~$5 per 1M tokens
- **Output**: ~$15 per 1M tokens

### Estimated Costs

For a typical code review:
- Small project (5 files, ~500 lines): ~$0.02 - $0.05
- Medium project (20 files, ~2000 lines): ~$0.10 - $0.20
- Large project (50 files, ~5000 lines): ~$0.30 - $0.50

**Note**: Actual costs depend on code complexity and review depth.

## Security Best Practices

### ⚠️ Never commit API keys to Git!

Make sure your API keys are in `.gitignore`:

```gitignore
# Environment files
.env
.env.local
.env.*.local

# Startup scripts with secrets
start.ps1
setup-secrets.ps1
```

### Use different keys for development and production

- Development: Use a key with spending limits
- Production: Use a separate key with monitoring

### Monitor your usage

Check your OpenAI usage dashboard regularly:
- https://platform.openai.com/account/usage

Set up spending limits to avoid surprises.

## Verification Checklist

Before starting the application:

- [ ] OpenAI account created
- [ ] API key generated
- [ ] API key set as environment variable
- [ ] Credits added to OpenAI account (if needed)
- [ ] Environment variable verified in terminal
- [ ] Backend started in same terminal
- [ ] Test code review created successfully

## Example: Full Startup Sequence

```powershell
# 1. Navigate to project
cd "C:\Users\31623\Documents\My-Projecten\SP-review code"

# 2. Set environment variables
$env:GITHUB_CLIENT_ID="Ov23liYourClientID"
$env:GITHUB_CLIENT_SECRET="your_github_secret"
$env:OPENAI_API_KEY="sk-your-openai-key"

# 3. Verify
Write-Host "GitHub Client ID: " -NoNewline
Write-Host ($env:GITHUB_CLIENT_ID -ne $null ? "✓ Set" : "✗ Not set") -ForegroundColor ($env:GITHUB_CLIENT_ID -ne $null ? "Green" : "Red")

Write-Host "GitHub Client Secret: " -NoNewline
Write-Host ($env:GITHUB_CLIENT_SECRET -ne $null ? "✓ Set" : "✗ Not set") -ForegroundColor ($env:GITHUB_CLIENT_SECRET -ne $null ? "Green" : "Red")

Write-Host "OpenAI API Key: " -NoNewline
Write-Host ($env:OPENAI_API_KEY -ne $null ? "✓ Set" : "✗ Not set") -ForegroundColor ($env:OPENAI_API_KEY -ne $null ? "Green" : "Red")

# 4. Start backend
./mvnw spring-boot:run

# In another terminal:
cd frontend
npm run dev
```

## Getting Help

If you encounter issues:

1. Check the logs in `logs/devmentor-ai.log`
2. Look for OpenAI-related errors
3. Verify your API key at https://platform.openai.com/api-keys
4. Check your account status and credits

## Next Steps

After setting up the OpenAI API key:

1. ✅ GitHub OAuth configured
2. ✅ OpenAI API key set
3. ✅ Backend running
4. 🔄 Test creating a code review
5. 🔄 Monitor OpenAI usage and costs
