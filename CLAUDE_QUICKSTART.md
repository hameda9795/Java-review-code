# Claude Sonnet 4.5 - Quick Start

## 🚀 3-Step Setup

### 1. Get Your API Key
Visit: https://console.anthropic.com/settings/keys
- Create account (if needed)
- Generate new API key
- Copy it (starts with `sk-ant-api03-`)

### 2. Set Environment Variable

**Windows PowerShell:**
```powershell
$env:ANTHROPIC_API_KEY="sk-ant-api03-YOUR-KEY-HERE"
```

**Windows CMD:**
```cmd
set ANTHROPIC_API_KEY=sk-ant-api03-YOUR-KEY-HERE
```

**Linux/Mac:**
```bash
export ANTHROPIC_API_KEY="sk-ant-api03-YOUR-KEY-HERE"
```

### 3. Run Application
```bash
mvnw clean install
mvnw spring-boot:run
```

---

## ✅ Verify It Works

**Check logs for:**
```
INFO - Initializing AnthropicChatModel with model: claude-sonnet-4-20250514
```

**Test with:**
```bash
# Go to Swagger UI
http://localhost:8080/swagger-ui.html

# Create a test review
POST /api/reviews
```

---

## 💰 Pricing

**Claude Sonnet 4:**
- Input: $3 per 1M tokens
- Output: $15 per 1M tokens

**Typical Code Review:**
- ~50K input tokens + ~10K output tokens
- **Cost: $0.30 per review**

**Free Tier Limits:**
- 50 requests/minute
- $5 free credit on signup
- ~16 reviews with free credit

---

## 🔥 Why Claude?

✅ **2x larger context** (200K vs 100K tokens)
✅ **40% cheaper** than GPT-4
✅ **Better at code** analysis
✅ **More consistent** JSON output
✅ **Excellent instruction** following

---

## 🆘 Troubleshooting

**Error: "Invalid API key"**
→ Check format: must start with `sk-ant-api03-`

**Error: "Rate limit exceeded"**
→ Wait 60 seconds or upgrade to paid tier

**Error: "Bean creation failed"**
→ Run `mvnw clean install` to update dependencies

---

## 📚 Full Documentation

See `CLAUDE_MIGRATION.md` for complete details.

---

**Status: Ready to use Claude Sonnet 4.5! 🎉**
