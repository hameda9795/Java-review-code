# DevMentor AI - Frontend

Modern Next.js 14 frontend for DevMentor AI code review platform.

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- Backend API running on http://localhost:8080

### Installation

```bash
# Install dependencies
npm install

# Set up environment variables
cp .env.local.example .env.local
# Edit .env.local with your configuration

# Run development server
npm run dev
```

The app will be available at http://localhost:3000

### Demo Credentials

```
Username: demo
Password: Demo123!
```

## 📦 Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: shadcn/ui (Radix UI)
- **State Management**: Zustand
- **Data Fetching**: TanStack Query (React Query)
- **HTTP Client**: Axios
- **Icons**: Lucide React

## 🏗️ Project Structure

```
frontend/
├── src/
│   ├── app/                    # Next.js app router pages
│   │   ├── (auth)/            # Auth pages (login, register)
│   │   ├── dashboard/         # Protected dashboard pages
│   │   │   ├── reviews/       # Review management
│   │   │   └── settings/      # User settings
│   │   ├── layout.tsx         # Root layout
│   │   ├── page.tsx           # Landing page
│   │   └── globals.css        # Global styles
│   │
│   ├── components/
│   │   ├── ui/                # shadcn/ui components
│   │   ├── layout/            # Layout components
│   │   └── providers.tsx      # App providers
│   │
│   └── lib/
│       ├── api/               # API client functions
│       ├── store/             # Zustand stores
│       ├── types/             # TypeScript types
│       └── utils.ts           # Utility functions
│
├── public/                    # Static assets
├── .env.local                 # Environment variables
├── next.config.mjs           # Next.js configuration
├── tailwind.config.ts        # Tailwind configuration
└── package.json              # Dependencies
```

## 🎨 Features

### Landing Page
- Beautiful hero section
- Feature highlights
- Pricing plans
- Responsive design

### Authentication
- User registration
- Login with demo credentials
- JWT token management
- Protected routes

### Dashboard
- Overview statistics
- Recent reviews
- Quality metrics
- Responsive charts

### Code Reviews
- Create new reviews
- Upload multiple files
- Manual code paste
- View all reviews
- Review details with findings
- Severity-based categorization
- Resolve findings

### Settings
- Account information
- Subscription tier display
- Upgrade prompts

## 🔧 Configuration

### Environment Variables

Create `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_GITHUB_CLIENT_ID=your-github-client-id
```

### API Configuration

The API client is configured in `src/lib/api/client.ts` with:
- Automatic token injection
- 401 redirect to login
- Request/response interceptors

## 📱 Pages

### Public Pages
- `/` - Landing page
- `/login` - Login page
- `/register` - Registration page

### Protected Pages (Dashboard)
- `/dashboard` - Dashboard overview
- `/dashboard/reviews` - All reviews list
- `/dashboard/reviews/new` - Create new review
- `/dashboard/reviews/[id]` - Review details
- `/dashboard/settings` - User settings

## 🎯 Key Components

### Layout Components
- `Header` - Top navigation with user info
- `Sidebar` - Side navigation menu
- `DashboardLayout` - Protected layout wrapper

### UI Components (shadcn/ui)
- Button, Card, Input, Label
- Badge, Toast notifications
- Fully customizable with Tailwind

### Custom Components
- Stats cards
- Review cards
- Finding cards with syntax highlighting
- File upload interface

## 🔐 Authentication Flow

1. User logs in via `/login`
2. JWT token stored in Zustand + localStorage
3. Token added to API requests automatically
4. Protected routes check authentication
5. Redirect to login if unauthenticated

## 📊 State Management

### Zustand Stores

**AuthStore** (`src/lib/store/authStore.ts`):
- User information
- JWT token
- Login/logout functions
- Persisted to localStorage

### React Query

- All API calls use React Query
- Automatic caching and refetching
- Loading and error states
- Mutations for create/update/delete

## 🎨 Styling

### Tailwind CSS
- Custom color scheme
- Dark mode support
- Responsive utilities
- Component variants

### Design System
- Consistent spacing
- Typography scale
- Color palette
- Shadow system

## 🚀 Development

```bash
# Development server
npm run dev

# Type checking
npm run type-check

# Linting
npm run lint

# Production build
npm run build

# Start production server
npm start
```

## 📦 Building for Production

```bash
# Create optimized build
npm run build

# Test production build locally
npm start
```

## 🔄 API Integration

The frontend integrates with the backend API:

### Auth API (`/api/auth`)
- POST `/register` - User registration
- POST `/login` - User login
- GET `/me` - Get current user

### Reviews API (`/api/reviews`)
- GET `/` - List all reviews
- POST `/` - Create review
- GET `/:id` - Get review details
- DELETE `/:id` - Delete review
- GET `/recent` - Recent reviews
- GET `/stats` - User statistics
- PUT `/:reviewId/findings/:findingId/resolve` - Resolve finding

## 🎯 User Flow

1. **Landing** → Register/Login
2. **Authentication** → Dashboard
3. **Dashboard** → View stats & recent reviews
4. **New Review** → Upload files or paste code
5. **Review Details** → View findings and scores
6. **Resolve Findings** → Mark issues as resolved

## 🐛 Troubleshooting

### API Connection Issues
- Verify backend is running on http://localhost:8080
- Check CORS configuration in backend
- Verify `.env.local` settings

### Authentication Issues
- Clear localStorage
- Check JWT token validity
- Verify backend `/api/auth` endpoints

### Build Errors
- Delete `.next` folder
- Clear node_modules and reinstall
- Check TypeScript errors

## 📝 Notes

- All pages use Next.js 14 App Router
- Client components marked with "use client"
- API calls use React Query for caching
- Forms use controlled components
- Protected routes check authentication on mount

## 🎉 Features Implemented

✅ Complete landing page with features
✅ User authentication (login/register)
✅ Protected dashboard layout
✅ Statistics dashboard
✅ Create new code reviews
✅ Upload multiple files
✅ View all reviews
✅ Review details with findings
✅ Resolve findings
✅ Delete reviews
✅ User settings page
✅ Responsive design
✅ Toast notifications
✅ Loading states
✅ Error handling

## 🔮 Future Enhancements

- GitHub OAuth integration
- Monaco Editor for code viewing
- Dark mode toggle
- Export reports (PDF)
- Real-time review status updates
- Code diff viewer
- Chart visualizations
- Email notifications
- Team collaboration features

---

**Ready to use!** Start the backend, run `npm run dev`, and access http://localhost:3000
