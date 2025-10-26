# DevMentor AI - Frontend Implementation Guide

This guide provides complete instructions for implementing the Next.js frontend with shadcn/ui.

---

## 🎯 Frontend Overview

**Technology Stack**:
- Next.js 14 (App Router)
- TypeScript
- shadcn/ui (Radix UI + Tailwind CSS)
- React Query (TanStack Query) for data fetching
- Zustand for state management
- Monaco Editor for code display
- Recharts for visualizations

---

## 🚀 Quick Start

### 1. Initialize Next.js Project

```bash
# Create Next.js app
npx create-next-app@latest devmentor-frontend --typescript --tailwind --app --src-dir

cd devmentor-frontend
```

When prompted:
- ✅ Would you like to use TypeScript? **Yes**
- ✅ Would you like to use ESLint? **Yes**
- ✅ Would you like to use Tailwind CSS? **Yes**
- ✅ Would you like to use `src/` directory? **Yes**
- ✅ Would you like to use App Router? **Yes**
- ❌ Would you like to customize the default import alias? **No**

### 2. Install shadcn/ui

```bash
# Initialize shadcn/ui
npx shadcn-ui@latest init
```

Configuration:
- Style: **Default**
- Base color: **Slate**
- CSS variables: **Yes**

### 3. Install Additional Dependencies

```bash
npm install @tanstack/react-query zustand axios @monaco-editor/react recharts lucide-react date-fns
npm install -D @types/node
```

---

## 📁 Project Structure

```
devmentor-frontend/
├── src/
│   ├── app/
│   │   ├── (auth)/
│   │   │   ├── login/
│   │   │   │   └── page.tsx
│   │   │   └── register/
│   │   │       └── page.tsx
│   │   ├── (dashboard)/
│   │   │   ├── layout.tsx
│   │   │   ├── page.tsx
│   │   │   ├── reviews/
│   │   │   │   ├── page.tsx
│   │   │   │   ├── new/
│   │   │   │   │   └── page.tsx
│   │   │   │   └── [id]/
│   │   │   │       └── page.tsx
│   │   │   └── settings/
│   │   │       └── page.tsx
│   │   ├── layout.tsx
│   │   ├── page.tsx
│   │   └── globals.css
│   ├── components/
│   │   ├── ui/ (shadcn components)
│   │   ├── auth/
│   │   │   ├── LoginForm.tsx
│   │   │   └── RegisterForm.tsx
│   │   ├── dashboard/
│   │   │   ├── StatsCard.tsx
│   │   │   ├── RecentReviews.tsx
│   │   │   └── QualityChart.tsx
│   │   ├── review/
│   │   │   ├── ReviewCard.tsx
│   │   │   ├── ReviewForm.tsx
│   │   │   ├── FindingsList.tsx
│   │   │   └── CodeViewer.tsx
│   │   └── layout/
│   │       ├── Header.tsx
│   │       ├── Sidebar.tsx
│   │       └── Footer.tsx
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts
│   │   │   ├── auth.ts
│   │   │   ├── reviews.ts
│   │   │   └── github.ts
│   │   ├── hooks/
│   │   │   ├── useAuth.ts
│   │   │   ├── useReviews.ts
│   │   │   └── useGitHub.ts
│   │   ├── store/
│   │   │   └── authStore.ts
│   │   ├── types/
│   │   │   ├── auth.ts
│   │   │   ├── review.ts
│   │   │   └── github.ts
│   │   └── utils/
│   │       ├── cn.ts
│   │       └── formatters.ts
│   └── middleware.ts
├── public/
├── .env.local
├── next.config.js
├── tailwind.config.ts
└── package.json
```

---

## 🔧 Configuration Files

### 1. Environment Variables (`.env.local`)

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_GITHUB_CLIENT_ID=your-github-client-id
```

### 2. Update `next.config.js`

```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
};

module.exports = nextConfig;
```

---

## 📦 Install shadcn/ui Components

```bash
# Install all necessary components
npx shadcn-ui@latest add button
npx shadcn-ui@latest add card
npx shadcn-ui@latest add input
npx shadcn-ui@latest add label
npx shadcn-ui@latest add form
npx shadcn-ui@latest add toast
npx shadcn-ui@latest add dropdown-menu
npx shadcn-ui@latest add dialog
npx shadcn-ui@latest add badge
npx shadcn-ui@latest add table
npx shadcn-ui@latest add tabs
npx shadcn-ui@latest add alert
npx shadcn-ui@latest add avatar
npx shadcn-ui@latest add separator
npx shadcn-ui@latest add skeleton
```

---

## 💾 Core Implementation

### 1. API Client (`src/lib/api/client.ts`)

```typescript
import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### 2. Auth Store (`src/lib/store/authStore.ts`)

```typescript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: string;
  username: string;
  email: string;
  subscriptionTier: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  setAuth: (user: User, token: string) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      setAuth: (user, token) => {
        set({ user, token });
        localStorage.setItem('token', token);
      },
      logout: () => {
        set({ user: null, token: null });
        localStorage.removeItem('token');
      },
      isAuthenticated: () => !!get().token,
    }),
    {
      name: 'auth-storage',
    }
  )
);
```

### 3. Auth API (`src/lib/api/auth.ts`)

```typescript
import apiClient from './client';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: string;
  username: string;
  email: string;
  subscriptionTier: string;
}

export const authApi = {
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post('/auth/register', data);
    return response.data;
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post('/auth/login', data);
    return response.data;
  },

  getCurrentUser: async (): Promise<AuthResponse> => {
    const response = await apiClient.get('/auth/me');
    return response.data;
  },
};
```

### 4. Reviews API (`src/lib/api/reviews.ts`)

```typescript
import apiClient from './client';

export interface CreateReviewRequest {
  title: string;
  files: Record<string, string>;
}

export interface ReviewResponse {
  id: string;
  title: string;
  status: string;
  createdAt: string;
  completedAt?: string;
  qualityScore?: {
    overallScore: number;
    grade: string;
    securityScore: number;
    performanceScore: number;
    maintainabilityScore: number;
    bestPracticesScore: number;
    testCoverageScore: number;
  };
  findings: Array<{
    id: string;
    title: string;
    severity: string;
    category: string;
    description: string;
    filePath: string;
    lineNumber?: number;
    codeSnippet?: string;
    suggestedFix?: string;
    isResolved: boolean;
  }>;
}

export const reviewsApi = {
  create: async (data: CreateReviewRequest): Promise<ReviewResponse> => {
    const response = await apiClient.post('/reviews', data);
    return response.data;
  },

  getById: async (id: string): Promise<ReviewResponse> => {
    const response = await apiClient.get(`/reviews/${id}`);
    return response.data;
  },

  list: async (): Promise<ReviewResponse[]> => {
    const response = await apiClient.get('/reviews');
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/reviews/${id}`);
  },

  getSummary: async (id: string): Promise<string> => {
    const response = await apiClient.get(`/reviews/${id}/summary`);
    return response.data;
  },
};
```

### 5. Login Form (`src/components/auth/LoginForm.tsx`)

```typescript
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/components/ui/use-toast';
import { authApi } from '@/lib/api/auth';
import { useAuthStore } from '@/lib/store/authStore';

export function LoginForm() {
  const router = useRouter();
  const { toast } = useToast();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await authApi.login(formData);
      setAuth(
        {
          id: response.userId,
          username: response.username,
          email: response.email,
          subscriptionTier: response.subscriptionTier,
        },
        response.token
      );

      toast({
        title: 'Login successful',
        description: 'Welcome back!',
      });

      router.push('/dashboard');
    } catch (error: any) {
      toast({
        title: 'Login failed',
        description: error.response?.data?.message || 'Invalid credentials',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle>Login</CardTitle>
        <CardDescription>Enter your credentials to access your account</CardDescription>
      </CardHeader>
      <form onSubmit={handleSubmit}>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="username">Username</Label>
            <Input
              id="username"
              type="text"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              required
            />
          </div>
        </CardContent>
        <CardFooter className="flex flex-col gap-4">
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </Button>
          <p className="text-sm text-muted-foreground">
            Don't have an account?{' '}
            <a href="/register" className="text-primary hover:underline">
              Register
            </a>
          </p>
        </CardFooter>
      </form>
    </Card>
  );
}
```

### 6. Dashboard Page (`src/app/(dashboard)/page.tsx`)

```typescript
'use client';

import { useQuery } from '@tanstack/react-query';
import { reviewsApi } from '@/lib/api/reviews';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { FileCode, CheckCircle, AlertCircle, TrendingUp } from 'lucide-react';

export default function DashboardPage() {
  const { data: reviews, isLoading } = useQuery({
    queryKey: ['reviews'],
    queryFn: () => reviewsApi.list(),
  });

  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {[1, 2, 3, 4].map((i) => (
          <Skeleton key={i} className="h-32" />
        ))}
      </div>
    );
  }

  const stats = {
    total: reviews?.length || 0,
    completed: reviews?.filter((r) => r.status === 'COMPLETED').length || 0,
    avgScore: reviews?.reduce((acc, r) => acc + (r.qualityScore?.overallScore || 0), 0) / (reviews?.length || 1) || 0,
    issues: reviews?.reduce((acc, r) => acc + r.findings.length, 0) || 0,
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">Welcome back! Here's your code quality overview.</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Reviews</CardTitle>
            <FileCode className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.total}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Completed</CardTitle>
            <CheckCircle className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.completed}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Average Score</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.avgScore.toFixed(0)}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Findings</CardTitle>
            <AlertCircle className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.issues}</div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Recent Reviews</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {reviews?.slice(0, 5).map((review) => (
              <div key={review.id} className="flex items-center justify-between border-b pb-4 last:border-0">
                <div>
                  <h3 className="font-medium">{review.title}</h3>
                  <p className="text-sm text-muted-foreground">
                    {new Date(review.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <div className="text-right">
                  <div className="text-2xl font-bold">{review.qualityScore?.grade || '-'}</div>
                  <p className="text-xs text-muted-foreground">{review.findings.length} findings</p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
```

---

## 🎨 Complete Component Examples

The guide above provides:
1. ✅ Project setup and configuration
2. ✅ API client with authentication
3. ✅ State management with Zustand
4. ✅ Authentication forms and flow
5. ✅ Dashboard with statistics
6. ✅ Type-safe API clients

### Additional Components to Implement:

1. **Review Creation Form** - Form with file upload/paste
2. **Review Details Page** - Show findings, scores, and code
3. **Code Viewer** - Monaco Editor integration
4. **Findings List** - Filterable, sortable findings
5. **GitHub Integration** - OAuth flow and repository selection
6. **Settings Page** - User preferences and GitHub connection

---

## 🚀 Running the Frontend

```bash
# Development mode
npm run dev

# Production build
npm run build
npm start

# Linting
npm run lint
```

Access at: http://localhost:3000

---

## 🔗 Integration with Backend

The frontend expects the backend API at `http://localhost:8080/api`. Ensure:
1. Backend is running on port 8080
2. CORS is configured to allow requests from http://localhost:3000
3. JWT tokens are properly configured

---

## 📱 Features Checklist

- [x] Authentication (Login/Register)
- [x] Dashboard with statistics
- [ ] Create new code review
- [ ] View review details
- [ ] Interactive code viewer
- [ ] GitHub OAuth integration
- [ ] GitHub repository selection
- [ ] User settings
- [ ] Dark mode support
- [ ] Responsive design
- [ ] Loading states
- [ ] Error handling
- [ ] Toast notifications

---

## 🎯 Next Steps

1. Implement remaining pages (New Review, Review Details, Settings)
2. Add Monaco Editor for code viewing
3. Implement GitHub OAuth callback handling
4. Add charts for quality score visualization
5. Implement dark mode
6. Add unit tests with Jest and React Testing Library
7. Add E2E tests with Playwright

---

**Ready to build!** Follow this guide step by step to create a complete, production-ready frontend for DevMentor AI.
