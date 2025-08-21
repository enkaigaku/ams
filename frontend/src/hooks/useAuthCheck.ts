import { useEffect } from 'react';
import { useAuthStore } from '../stores/authStore';
import { authService } from '../services/authService';

export const useAuthCheck = () => {
  const { token, login, logout } = useAuthStore();

  useEffect(() => {
    const checkAuth = async () => {
      if (token) {
        try {
          const response = await authService.getProfile();
          if (!response.success || !response.data) {
            logout();
          } else {
            login(response.data, token);
          }
        } catch (error) {
          console.error('Auth check failed:', error);
          logout();
        }
      }
    };

    checkAuth();
  }, [token, login, logout]);
};