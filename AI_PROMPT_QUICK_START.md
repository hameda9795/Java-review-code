# AI Prompt Feature - Quick Reference

## 🎯 What It Does

Generates a comprehensive, AI-ready prompt from code review findings that can be pasted into Claude, ChatGPT, or other AI assistants to fix all identified issues automatically.

---

## 🚀 How to Use

### Step 1: Complete a Code Review
- Upload your code files
- Wait for AI analysis to complete
- Review will have findings organized by severity

### Step 2: Generate the Prompt
1. Go to the review details page
2. Click on the **"Findings"** tab
3. Look for the **"AI Code Fixing Prompt"** card at the top
4. Click **"Generate AI Fixing Prompt"** button

### Step 3: Use the Prompt
**Option A - Copy & Paste:**
1. Click **"Copy to Clipboard"**
2. Open Claude or ChatGPT
3. Paste the entire prompt
4. Let AI generate fixes

**Option B - Download:**
1. Click **"Download .md"**
2. Save the file for later
3. Share with team or use offline

---

## 📊 Prompt Structure

The generated prompt includes:

### 1. Project Context
- Review ID and project name
- Files analyzed and lines of code
- Quality scores (Security, Performance, etc.)

### 2. Issues Summary
- Total findings count
- Breakdown by severity (Critical, High, Medium, Low, Info)

### 3. Core Principles
- Fix, don't just comment
- Maintain functionality
- Follow best practices
- Prioritize by severity

### 4. Detailed Issues (for each finding)
- Issue number and title
- Category and severity
- File location (path:line)
- Problem description
- Current code snippet
- Suggested solution
- Impact assessment
- Actionable instructions

### 5. Final Instructions
- Deliverables checklist
- Quality verification steps
- Success criteria

---

## 🎨 UI Components

### Statistics Display
Shows at-a-glance metrics:
- **Total Issues**: Overall count
- **Critical** (Red): Must fix immediately
- **High** (Orange): Should fix before production
- **Medium** (Yellow): Important improvements
- **Low** (Green): Nice to have

### Prompt Preview
- Scrollable markdown preview
- Character count badge
- Syntax-highlighted code blocks
- Copy/download buttons

### Actions
- **Copy to Clipboard**: One-click copy with confirmation
- **Download .md**: Save as Markdown file
- **Regenerate**: Create a fresh prompt

---

## 💡 Best Practices

### When to Use
✅ After completing a code review
✅ When you have multiple findings to fix
✅ When you want AI assistance with fixes
✅ To share fix instructions with team

### How to Get Best Results
1. **Review the prompt** before sending to AI
2. **Start with critical issues** (they're prioritized first)
3. **Test AI fixes** before committing to production
4. **Iterate**: Run a new review after applying fixes

### Tips
- The prompt is optimized for Claude Sonnet, but works with any AI
- Longer prompts (more findings) may need premium AI tier
- Save downloaded prompts for documentation
- Share prompts with junior developers for learning

---

## 🔧 Technical Details

### Backend API
**Endpoint:** `GET /api/reviews/{reviewId}/generate-prompt`

**Response:**
```json
{
  "prompt": "# Code Review Issue Resolution Request...",
  "totalFindings": 36,
  "criticalCount": 5,
  "highCount": 13,
  "mediumCount": 13,
  "lowCount": 5,
  "reviewId": "uuid",
  "generatedAt": "2025-10-26T10:30:00",
  "instructions": "Copy this prompt and paste..."
}
```

### Frontend Component
**Location:** `src/components/review/ai-prompt-generator.tsx`

**Props:**
- `reviewId: string` - The review to generate prompt for

**Features:**
- Lazy loading (generates on demand)
- Error handling with user-friendly messages
- Responsive design
- Dark mode support

---

## 📋 Example Workflow

```
1. Upload Code → 2. AI Review → 3. View Findings
                                        ↓
                                4. Generate Prompt
                                        ↓
                        ┌───────────────┴───────────────┐
                        ↓                               ↓
                5a. Copy & Paste                 5b. Download
                to Claude/ChatGPT                for Later
                        ↓                               ↓
                6. AI Generates Fixes           7. Share with Team
                        ↓
                8. Apply Fixes to Code
                        ↓
                9. Run New Review
                        ↓
                10. Quality Score Improved! ✅
```

---

## 🎯 Success Metrics

After using the AI-generated fixes, you should see:

- ✅ **Higher Quality Score**: Target 80+/100
- ✅ **Fewer Findings**: Especially critical/high severity
- ✅ **Better Security Score**: Vulnerabilities eliminated
- ✅ **Improved Maintainability**: Code smells reduced
- ✅ **Production Ready**: All critical issues resolved

---

## ❓ Troubleshooting

### Prompt Too Long for AI
**Solution:** Focus on critical/high issues first, fix those, then re-review

### Copy to Clipboard Doesn't Work
**Solution:** Use the Download button instead, or manually select and copy

### Generated Prompt Seems Incomplete
**Solution:** Click "Regenerate Prompt" to create a fresh one

### AI Doesn't Understand Prompt
**Solution:** Make sure you're using Claude Sonnet 4 or GPT-4 (newer models work best)

---

## 🌟 Pro Tips

1. **Batch Fix by Category**: Ask AI to fix all security issues first, then performance, etc.
2. **Learn from Fixes**: Study the AI's solutions to improve your own coding
3. **Document Changes**: Save the prompt as proof of systematic code improvement
4. **Iterate**: Re-review after fixes to track progress
5. **Share Knowledge**: Use prompts as teaching materials for team

---

## 🚦 Quick Start Checklist

- [ ] Complete a code review
- [ ] Go to Findings tab
- [ ] Click "Generate AI Fixing Prompt"
- [ ] Review the statistics
- [ ] Copy or download the prompt
- [ ] Paste into Claude or ChatGPT
- [ ] Apply the AI-generated fixes
- [ ] Run a new review to verify improvements

---

**Ready to fix your code with AI? Start generating prompts now! 🚀**
