# Admin Panel - Complete Guide

## Overview

A complete admin panel has been implemented with the following features:
1. View and manage all users
2. Create special users with custom usage limits
3. Admin dashboard with statistics
4. Full CRUD operations on users
5. Role-based access control

## Setup Instructions

### 1. Database Migration

First, run the SQL migration to add the new columns:

```bash
# Connect to your PostgreSQL database and run:
psql -U your_username -d your_database -f add-admin-panel-fields.sql
```

Or manually execute:
```sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS usage_limit INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_special_user BOOLEAN NOT NULL DEFAULT false;
CREATE INDEX IF NOT EXISTS idx_users_is_special_user ON users(is_special_user);
```

### 2. Create an Admin User

You need to manually set a user as admin in the database:

```sql
-- Set an existing user as admin
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';

-- Or create a new admin user (after they register through the UI)
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

### 3. Start the Application

```bash
# Backend (in project root)
./mvnw spring-boot:run

# Frontend (in frontend directory)
cd frontend
npm run dev
```

## Admin Panel Features

### 1. Admin Dashboard (`/dashboard/admin`)

**Features:**
- Total users count
- Active users count
- Special users count
- Total reviews statistics
- Reviews today/this week/this month
- Quick action cards to navigate to other admin pages

**Access:** Only users with ADMIN role can access

### 2. User Management (`/dashboard/admin/users`)

**Features:**
- View all users in a table
- Search users by username, email, or name
- See user details (role, status, tier, reviews, usage limit)
- Edit user information
- Delete users (except admins)
- Reset user usage count
- Update user role, subscription tier, and special user status

**CRUD Operations:**
- **Create:** Not available on this page (use special users page)
- **Read:** View all users with detailed information
- **Update:** Click edit button to modify user properties
- **Delete:** Click delete button (with confirmation)

**Edit User Dialog:**
- Full Name
- Role (USER/ADMIN)
- Subscription Tier (FREE/PREMIUM)
- Active Status (checkbox)
- Special User Status (checkbox)
- Usage Limit (only for special users)

### 3. Special Users Management (`/dashboard/admin/special-users`)

**Features:**
- View all special users in card layout
- Create new special users with custom usage limits
- See usage progress bars
- Reset usage for special users
- Delete special users

**Create Special User:**
- Username (required)
- Email (required)
- Password (required)
- Full Name (optional)
- Usage Limit (optional - null means unlimited)

**Usage Limit:**
- If set: User can only perform N reviews
- If null/empty: User has unlimited reviews
- Progress bar shows current usage vs limit

## API Endpoints

All admin endpoints require authentication and ADMIN role:

### User Management
- `GET /api/admin/users` - Get all users
- `GET /api/admin/users/special` - Get special users
- `GET /api/admin/users/{userId}` - Get user by ID
- `POST /api/admin/users/special` - Create special user
- `PUT /api/admin/users/{userId}` - Update user
- `DELETE /api/admin/users/{userId}` - Delete user
- `POST /api/admin/users/{userId}/reset-usage` - Reset user usage

### Statistics
- `GET /api/admin/stats` - Get admin dashboard statistics

## Testing the Admin Panel

### Test Scenario 1: Create a Special User

1. Login with admin credentials
2. Navigate to `/dashboard/admin/special-users`
3. Click "Create Special User"
4. Fill in the form:
   - Username: `testspecial`
   - Email: `testspecial@example.com`
   - Password: `Test123!`
   - Usage Limit: `5` (or leave empty for unlimited)
5. Click "Create User"
6. Verify the user appears in the special users list
7. Check the usage progress bar shows 0/5 reviews

### Test Scenario 2: Update a User

1. Navigate to `/dashboard/admin/users`
2. Find a user in the table
3. Click the edit button
4. Modify user properties:
   - Change role to ADMIN or USER
   - Toggle special user status
   - Update usage limit
   - Change subscription tier
5. Click "Save Changes"
6. Verify changes are reflected in the table

### Test Scenario 3: Reset User Usage

1. Find a user with reviewsCount > 0
2. Click the refresh button
3. Confirm the reset action
4. Verify the reviewsCount is set to 0

### Test Scenario 4: Delete a User

1. Find a non-admin user
2. Click the delete button
3. Confirm the deletion
4. Verify the user is removed from the list

### Test Scenario 5: View Admin Dashboard

1. Navigate to `/dashboard/admin`
2. Verify all statistics are displayed correctly:
   - Total Users
   - Active Users
   - Special Users
   - Review statistics
3. Click quick action cards to navigate to other admin pages

## Security Features

1. **Role-Based Access Control:**
   - Only users with ADMIN role can access admin pages
   - Frontend checks user role before rendering admin UI
   - Backend validates ADMIN role on all admin endpoints
   - Non-admin users are redirected if they try to access admin pages

2. **Admin Protection:**
   - Admin users cannot be deleted via the UI
   - This prevents accidentally deleting all admin users

3. **JWT Authentication:**
   - All admin endpoints require valid JWT token
   - Token includes user role information

## Sidebar Navigation

The sidebar dynamically shows admin menu items only for admin users:

**Regular Users See:**
- Dashboard
- Reviews
- New Review
- Settings

**Admin Users See (Additional):**
- --- ADMIN ---
- Admin Dashboard
- Manage Users
- Special Users

## Database Schema

### Users Table (Updated)

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100),
    full_name VARCHAR(100),
    avatar_url VARCHAR(500),
    github_id BIGINT,
    github_username VARCHAR(50),
    github_access_token VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    subscription_tier VARCHAR(20) NOT NULL DEFAULT 'FREE',
    is_active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    is_special_user BOOLEAN NOT NULL DEFAULT false,  -- NEW
    usage_limit INTEGER,                             -- NEW
    reviews_count INTEGER NOT NULL DEFAULT 0,
    total_files_reviewed INTEGER NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Troubleshooting

### Admin Menu Not Showing
- Check that your user role is set to 'ADMIN' in the database
- Clear browser cache and localStorage
- Re-login to refresh the auth token

### 403 Forbidden on Admin Endpoints
- Verify JWT token includes correct role
- Check backend logs for authentication errors
- Ensure user is logged in with admin account

### Special Users Not Saving
- Check backend logs for validation errors
- Verify all required fields are filled
- Ensure username and email are unique

### Usage Limit Not Enforced
- The usage limit is enforced in the `User.canCreateReview()` method
- Check that `isSpecialUser` is set to true
- Verify the `reviewsCount` is being incremented correctly

## Next Steps

1. Test all CRUD operations thoroughly
2. Create a few special users with different limits
3. Test the usage limit enforcement by having special users create reviews
4. Monitor the admin dashboard statistics
5. Test with multiple admin users to ensure proper access control

## Notes

- All data is real and persisted in the database
- No demo data or mock APIs are used
- All CRUD operations work with the actual database
- The admin panel is production-ready
