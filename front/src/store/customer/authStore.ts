import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  token: string | null;
  email: string | null;
  name: string | null; // 추가
  isTemporaryUser: boolean;
  setAuth: (auth: { token: string; email: string; name: string; temporaryUser: boolean }) => void;
  setToken: (token: string | null) => void;
  setUserName: (name: string) => void; // 추가
  isAuthenticated: () => boolean;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      email: null,
      name: null, // 초기값 추가
      isTemporaryUser: false,

      setAuth: ({ token, email, name, temporaryUser }) =>
        set({ token, email, name, isTemporaryUser: temporaryUser }),

      setToken: (token) => set({ token }),

      setUserName: (name) => set({ name: name }), // 추가

      isAuthenticated: () => !!get().token,

      logout: () => set({ token: null, email: null, name: null, isTemporaryUser: false }), // userName 초기화도 포함
    }),
    {
      name: 'auth-storage',
    }
  )
);
