import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, AuthState } from '../types';

interface AuthStore extends AuthState {
  login: (user: User, accessToken: string, refreshToken?: string) => void;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
  updateTokens: (accessToken: string, refreshToken?: string) => void;
  isManager: () => boolean;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: (user: User, accessToken: string, refreshToken?: string) => {
        set({
          user,
          token: accessToken,
          isAuthenticated: true,
        });
        // Store refreshToken in localStorage separately if provided
        if (refreshToken) {
          localStorage.setItem('refresh-token', refreshToken);
        }
      },

      logout: () => {
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        });
        localStorage.removeItem('auth-storage');
        localStorage.removeItem('refresh-token');
      },

      updateTokens: (accessToken: string, refreshToken?: string) => {
        set({ token: accessToken });
        if (refreshToken) {
          localStorage.setItem('refresh-token', refreshToken);
        }
      },

      updateUser: (userData: Partial<User>) => {
        const currentUser = get().user;
        if (currentUser) {
          set({
            user: { ...currentUser, ...userData },
          });
        }
      },

      isManager: () => {
        const user = get().user;
        return user?.role === 'MANAGER';
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);