import apiClient from './client';

export interface UserDTO {
  id: string;
  username: string;
  email: string;
  fullName?: string;
  avatarUrl?: string;
  githubUsername?: string;
  role: 'USER' | 'ADMIN';
  subscriptionTier: 'FREE' | 'PREMIUM';
  isActive: boolean;
  emailVerified: boolean;
  isSpecialUser: boolean;
  usageLimit?: number;
  reviewsCount: number;
  totalFilesReviewed: number;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSpecialUserRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
  usageLimit?: number;
}

export interface UpdateUserRequest {
  fullName?: string;
  role?: 'USER' | 'ADMIN';
  subscriptionTier?: 'FREE' | 'PREMIUM';
  isActive?: boolean;
  isSpecialUser?: boolean;
  usageLimit?: number;
}

export interface AdminStatsDTO {
  totalUsers: number;
  activeUsers: number;
  specialUsers: number;
  totalReviews: number;
  reviewsToday: number;
  reviewsThisWeek: number;
  reviewsThisMonth: number;
}

export const adminApi = {
  // Get all users
  getAllUsers: async (): Promise<UserDTO[]> => {
    const response = await apiClient.get('/admin/users');
    return response.data;
  },

  // Get special users
  getSpecialUsers: async (): Promise<UserDTO[]> => {
    const response = await apiClient.get('/admin/users/special');
    return response.data;
  },

  // Get user by ID
  getUserById: async (userId: string): Promise<UserDTO> => {
    const response = await apiClient.get(`/admin/users/${userId}`);
    return response.data;
  },

  // Create special user
  createSpecialUser: async (request: CreateSpecialUserRequest): Promise<UserDTO> => {
    const response = await apiClient.post('/admin/users/special', request);
    return response.data;
  },

  // Update user
  updateUser: async (userId: string, request: UpdateUserRequest): Promise<UserDTO> => {
    const response = await apiClient.put(`/admin/users/${userId}`, request);
    return response.data;
  },

  // Delete user
  deleteUser: async (userId: string): Promise<void> => {
    await apiClient.delete(`/admin/users/${userId}`);
  },

  // Reset user usage
  resetUserUsage: async (userId: string): Promise<UserDTO> => {
    const response = await apiClient.post(`/admin/users/${userId}/reset-usage`);
    return response.data;
  },

  // Get admin statistics
  getAdminStats: async (): Promise<AdminStatsDTO> => {
    const response = await apiClient.get('/admin/stats');
    return response.data;
  },
};
