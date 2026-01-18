import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthState, LoginRequest, RegisterRequest } from '../types/auth';
import { ApiAuthRepository } from '../api/auth';

const authRepo = new ApiAuthRepository();

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      isAdmin: false,
      login: async (credentials: LoginRequest) => {
        const response = await authRepo.login(credentials);
        set({ 
          user: response, 
          isAuthenticated: true,
          isAdmin: response.role === 'ROLE_ADMIN'
        });
      },
      register: async (data: RegisterRequest) => {
        const response = await authRepo.register(data);
        set({ 
          user: response, 
          isAuthenticated: true,
          isAdmin: response.role === 'ROLE_ADMIN'
        });
      },
      logout: () => {
        set({ user: null, isAuthenticated: false, isAdmin: false });
      },
    }),
    {
      name: 'auth-storage',
    }
  )
);
