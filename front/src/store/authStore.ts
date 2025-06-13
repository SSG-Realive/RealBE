import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  token: string | null;
  email: string | null;
  isTemporaryUser: boolean;
  setAuth: (auth: { token: string; email: string; temporaryUser: boolean }) => void;
  setToken: (token: string | null) => void;
  isAuthenticated: () => boolean;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      email: null,
      isTemporaryUser: false,

      setAuth: ({ token, email, temporaryUser }) =>
        set({ token, email, isTemporaryUser: temporaryUser }),

      setToken: (token) => set({ token }),

      isAuthenticated: () => !!get().token,

      logout: () => set({ token: null, email: null, isTemporaryUser: false }),
    }),
    {
      name: 'auth-storage', // localStorage key
    }
  )
);
