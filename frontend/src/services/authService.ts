import { apiService } from './api';
import type { User, ApiResponse } from '../types';

export interface LoginRequest {
  employeeId: string;
  password: string;
}

export interface LoginResponse {
  user: User;
  token: string;
}

export const authService = {
  async login(credentials: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return apiService.post('/auth/login', credentials);
  },

  async logout(): Promise<ApiResponse<void>> {
    return apiService.post('/auth/logout');
  },

  async refreshToken(): Promise<ApiResponse<{ token: string }>> {
    return apiService.post('/auth/refresh');
  },

  async getProfile(): Promise<ApiResponse<User>> {
    return apiService.get('/auth/profile');
  },

  async updateProfile(data: Partial<User>): Promise<ApiResponse<User>> {
    return apiService.put('/auth/profile', data);
  },
};