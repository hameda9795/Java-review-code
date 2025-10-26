import apiClient from './client';
import { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

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
