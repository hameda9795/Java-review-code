export interface User {
  id: string;
  username: string;
  email: string;
  fullName?: string;
  subscriptionTier: 'FREE' | 'PREMIUM';
  reviewsCount: number;
  isActive: boolean;
  githubUsername?: string;
  githubConnected?: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: string;
  username: string;
  email: string;
  subscriptionTier: string;
  githubUsername?: string;
  githubConnected?: boolean;
}
