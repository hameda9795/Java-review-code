# GitHub Repository Dropdown Implementation

## Summary
Implemented a modern dropdown-based repository selector to replace the manual text input method for selecting GitHub repositories in the "Create New Review" section.

## Changes Made

### Backend Changes

#### 1. GitHubClient.java
**Location**: `src/main/java/com/devmentor/infrastructure/github/GitHubClient.java`

**Added Method**: `getUserRepositories(String accessToken)`
- Fetches up to 100 repositories from GitHub API
- Sorted by recently updated
- Returns list of GitHubRepository objects with full metadata

```java
public java.util.List<GitHubRepository> getUserRepositories(String accessToken)
```

#### 2. GitHubService.java
**Location**: `src/main/java/com/devmentor/application/service/GitHubService.java`

**Updated Method**: `getUserRepositories(UUID userId)`
- Validates user has GitHub connected
- Calls GitHubClient to fetch repositories
- Returns list of repositories

#### 3. GitHubController.java
**Location**: `src/main/java/com/devmentor/interfaces/rest/controller/GitHubController.java`

**New Endpoint**: `GET /api/github/repositories`
- Returns authenticated user's GitHub repositories
- Requires X-User-Id header
- Returns JSON array of repository objects

### Frontend Changes

#### 1. GitHub API Client
**Location**: `frontend/src/lib/api/github.ts`

**Added Method**: `getRepositories()`
- Calls the new `/api/github/repositories` endpoint
- Uses apiClient for automatic authentication

```typescript
getRepositories: async () => {
  const response = await apiClient.get('/github/repositories');
  return response.data;
}
```

#### 2. New Dropdown Component
**Location**: `frontend/src/components/ui/github-repo-dropdown.tsx`

**Features**:
- ✅ Automatic repository loading on mount
- ✅ Searchable dropdown with real-time filtering
- ✅ Displays repository metadata (name, description, language, privacy)
- ✅ Visual badges for language and private repos
- ✅ Loading states for both fetching repos and analyzing
- ✅ Click-outside-to-close functionality
- ✅ Responsive design with fixed positioning
- ✅ Error handling with user-friendly messages
- ✅ GitHub connection status check

**Key Features**:
- Search by name, full name, or description
- Shows up to 100 repositories
- Language badges (e.g., Java, Python)
- Private/Public indicators
- One-click selection
- Integrated "Analyze Repository" button

#### 3. Updated New Review Page
**Location**: `frontend/src/app/dashboard/reviews/new/page.tsx`

**Changed**:
- Replaced `GitHubRepoSelector` with `GitHubRepoDropdown`
- Maintains same props interface (`onFilesLoaded`)
- No changes to parent component logic required

## User Experience Improvements

### Before
- Manual text input: `owner/repo` or full GitHub URL
- Error-prone (typos, incorrect format)
- No visibility of available repositories
- Required knowing exact repository names

### After
- Visual dropdown menu with all accessible repositories
- Search/filter functionality
- See repository details before selecting
- One-click selection
- Clear visual feedback (language, privacy status)
- Professional, modern UI

## Technical Details

### API Flow
1. User opens "Create New Review" page
2. Component checks if GitHub is connected
3. Automatically fetches user's repositories
4. User searches/selects repository from dropdown
5. Clicks "Analyze Repository" button
6. Backend fetches Java files from selected repository
7. Files loaded into review form

### Error Handling
- GitHub not connected → Shows message with link to settings
- No repositories found → Shows refresh button
- Repository fetch fails → Shows error toast with details
- Repository analysis fails → Shows specific error message

### Performance Considerations
- Repositories cached in component state
- Search filtering done client-side
- Dropdown closes on selection or outside click
- Loading states prevent multiple simultaneous requests

## Testing Checklist

Before using:
1. ✅ Ensure GitHub OAuth credentials are set (see GITHUB_OAUTH_TROUBLESHOOTING.md)
2. ✅ Start backend: `./mvnw spring-boot:run`
3. ✅ Start frontend: `cd frontend && npm run dev`
4. ✅ Connect GitHub account in Settings
5. ✅ Navigate to "Create New Review"

Test scenarios:
- [ ] Dropdown loads repositories automatically
- [ ] Search filters repositories correctly
- [ ] Selecting a repository updates the display
- [ ] "Analyze Repository" fetches Java files
- [ ] Error handling works for private repos without access
- [ ] Visual indicators (language, private) display correctly

## Files Modified

### Backend
- `src/main/java/com/devmentor/infrastructure/github/GitHubClient.java` - Added getUserRepositories()
- `src/main/java/com/devmentor/application/service/GitHubService.java` - Updated getUserRepositories()
- `src/main/java/com/devmentor/interfaces/rest/controller/GitHubController.java` - Added /repositories endpoint

### Frontend
- `frontend/src/lib/api/github.ts` - Added getRepositories() method
- `frontend/src/components/ui/github-repo-dropdown.tsx` - **NEW FILE** - Dropdown component
- `frontend/src/app/dashboard/reviews/new/page.tsx` - Updated to use new component

## Next Steps

To use the new feature:

1. **Set GitHub OAuth credentials** (if not already done):
   ```powershell
   $env:GITHUB_CLIENT_ID="your_client_id"
   $env:GITHUB_CLIENT_SECRET="your_client_secret"
   ```

2. **Start the backend**:
   ```powershell
   ./mvnw spring-boot:run
   ```

3. **Start the frontend** (in separate terminal):
   ```powershell
   cd frontend
   npm run dev
   ```

4. **Test the feature**:
   - Login at http://localhost:3000
   - Connect GitHub account in Settings
   - Go to "Create New Review"
   - Select "GitHub Repository" tab
   - See your repositories in the dropdown
   - Select and analyze!

## Notes

- The old `GitHubRepoSelector` component is still available if needed
- Maximum 100 repositories displayed (GitHub API default)
- Repositories sorted by recently updated
- Private repositories shown if user has access
- All error messages are user-friendly and actionable
