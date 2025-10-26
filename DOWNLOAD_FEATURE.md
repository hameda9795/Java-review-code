# Download Review Reports Feature

## Overview
The download feature allows users to export comprehensive code review reports in multiple formats:
- **Markdown (.md)** - For documentation and version control
- **HTML (.html)** - For viewing in browsers with beautiful styling
- **CSV (.csv)** - For importing into spreadsheets like Excel

## Implementation

### Backend Components

#### 1. ReportGeneratorService
**Location:** `src/main/java/com/devmentor/application/codereview/ReportGeneratorService.java`

**Features:**
- **Markdown Reports**: Complete technical documentation with:
  - Overview section (title, status, reviewer, dates)
  - Quality scores table (overall, security, performance, maintainability, best practices)
  - Statistics (files analyzed, lines of code, analysis duration)
  - Findings summary by severity and category
  - Detailed findings with code snippets, suggested fixes, and explanations
  - Recommendations based on findings

- **HTML Reports**: Professional, styled reports with:
  - Gradient header design
  - Responsive card-based layout
  - Color-coded severity badges (Critical=red, High=orange, Medium=yellow, Low=green)
  - Syntax-highlighted code blocks
  - Score grid with visual indicators
  - Print-friendly styling

- **CSV Reports**: Spreadsheet-compatible exports with:
  - Category, Severity, Title, Description
  - File Path, Line Number
  - Resolution Status, Impact Score
  - Properly escaped values for CSV compatibility

#### 2. ReviewController Endpoints
**Location:** `src/main/java/com/devmentor/interfaces/rest/controller/ReviewController.java`

**New Endpoints:**

```java
GET /api/reviews/{reviewId}/download/markdown
GET /api/reviews/{reviewId}/download/html
GET /api/reviews/{reviewId}/download/csv
```

**Features:**
- Proper Content-Type headers for each format
- Content-Disposition headers for file downloads
- Sanitized filenames (removes special characters, converts spaces to underscores)
- Returns 404 if review not found

### Frontend Components

#### 1. API Client Integration
**Location:** `frontend/src/lib/api/reviews.ts`

**New Methods:**
```typescript
downloadMarkdown(id: string): Promise<void>
downloadHtml(id: string): Promise<void>
downloadCsv(id: string): Promise<void>
```

**Features:**
- Uses blob response type for binary downloads
- Creates temporary download links
- Automatically triggers browser download
- Cleans up memory after download

#### 2. Download Button UI
**Location:** `frontend/src/app/dashboard/reviews/[id]/page.tsx`

**Features:**
- Dropdown menu with 3 format options
- Loading state with spinner
- Toast notifications for success/failure
- Click-outside to close menu
- Professional icons (FileText from lucide-react)

**UI Components:**
```tsx
<Button variant="outline" onClick={toggleMenu}>
  <Download /> Download
</Button>
<DropdownMenu>
  - Markdown (.md)
  - HTML (.html)
  - CSV (.csv)
</DropdownMenu>
```

## Report Contents

### Markdown Report Structure
```markdown
# Code Review Report
---

## Overview
- Title, Description, Status
- Reviewer, Created/Completed dates

## Quality Score
| Metric | Score |
- Overall (with grade)
- Security, Performance, Maintainability
- Best Practices, Test Coverage

## Statistics
- Files/Lines analyzed
- Total findings
- Analysis duration, AI model

## Findings Summary
### By Severity
- CRITICAL, HIGH, MEDIUM, LOW, INFO counts

### By Category
- Categories with counts

## Detailed Findings
For each finding:
1. Title, Severity, Category, Status
2. File path and line number
3. Description
4. Code snippet (if available)
5. Suggested fix (if available)
6. Explanation (why it matters)
7. Resources URL
8. Impact score

## Recommendations
- Critical/High priority actions
- Score improvement suggestions
```

### HTML Report Features
- **Responsive Design**: Works on desktop and mobile
- **Professional Styling**: 
  - Gradient header (purple to indigo)
  - Card-based sections with shadows
  - Score cards with large numbers
  - Color-coded severity badges
- **Code Highlighting**: Dark-themed code blocks
- **Printable**: Clean layout for printing

### CSV Report Columns
```
Category, Severity, Title, Description, File Path, Line Number, Is Resolved, Impact Score
```

## Usage

### From Frontend
```typescript
// Download Markdown
await reviewsApi.downloadMarkdown(reviewId);

// Download HTML
await reviewsApi.downloadHtml(reviewId);

// Download CSV
await reviewsApi.downloadCsv(reviewId);
```

### From API
```bash
# Markdown
curl -H "X-User-Id: {userId}" \
  http://localhost:8080/api/reviews/{reviewId}/download/markdown \
  -o review.md

# HTML
curl -H "X-User-Id: {userId}" \
  http://localhost:8080/api/reviews/{reviewId}/download/html \
  -o review.html

# CSV
curl -H "X-User-Id: {userId}" \
  http://localhost:8080/api/reviews/{reviewId}/download/csv \
  -o findings.csv
```

## Benefits

### For Users
- **Documentation**: Markdown format integrates with documentation systems
- **Presentation**: HTML format for sharing with stakeholders
- **Analysis**: CSV format for data analysis in Excel/Google Sheets
- **Offline Access**: All formats work without internet connection
- **Version Control**: Markdown files can be tracked in Git

### For Teams
- **Knowledge Sharing**: Easy to share review results
- **Compliance**: Exportable audit trail
- **Reporting**: CSV data for metrics and charts
- **Archival**: Multiple format options for long-term storage

### Technical Benefits
- **No External Dependencies**: Pure Java/TypeScript implementation
- **Memory Efficient**: Streams data directly to response
- **Scalable**: Works with reviews of any size
- **Secure**: Uses existing authentication/authorization

## File Naming Convention
```
{sanitized_title}_review.md
{sanitized_title}_review.html
{sanitized_title}_findings.csv
```

Example: `spring_boot_api_review.md`

## Future Enhancements
- [ ] PDF export with advanced formatting
- [ ] Export scheduling (daily/weekly automated reports)
- [ ] Custom report templates
- [ ] Batch export (multiple reviews)
- [ ] Email delivery option
- [ ] Report comparison (before/after)

## Testing
To test the feature:
1. Create a code review with findings
2. Navigate to review details page
3. Click "Download" button
4. Select desired format
5. File should download automatically
6. Verify content matches review data

## Notes
- Downloads use the browser's native download mechanism
- No server-side file storage required
- All reports generated on-demand
- UTF-8 encoding for international characters
- Proper escaping for HTML/CSV special characters
