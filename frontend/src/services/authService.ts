import { apiService } from './api';
import type { User, ApiResponse } from '../types';

export interface LoginRequest {
  employeeId: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export const authService = {
  async login(credentials: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return apiService.post('/auth/login', credentials);
  },

  async logout(): Promise<ApiResponse<void>> {
    return apiService.post('/auth/logout');
  },

  async refreshToken(refreshToken: string): Promise<ApiResponse<LoginResponse>> {
    return apiService.post('/auth/refresh', { refreshToken });
  },

  async getProfile(): Promise<ApiResponse<User>> {
    return apiService.get('/users/profile');
  },

  async updateProfile(data: Partial<User>): Promise<ApiResponse<User>> {
    return apiService.put('/users/profile', data);
  },
};