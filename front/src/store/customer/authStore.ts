import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  token: string | null;
  email: string | null;
  userName: string | null; // 추가
  isTemporaryUser: boolean;
  setAuth: (auth: { token: string; email: string; temporaryUser: boolean }) => void;
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
      userName: null, // 초기값 추가
      isTemporaryUser: false,

      setAuth: ({ token, email, temporaryUser }) =>
        set({ token, email, isTemporaryUser: temporaryUser }),

      setToken: (token) => set({ token }),

      setUserName: (name) => set({ userName: name }), // 추가

      isAuthenticated: () => !!get().token,

      logout: () => set({ token: null, email: null, userName: null, isTemporaryUser: false }), // userName 초기화도 포함
    }),
    {
      name: 'auth-storage',
    }
  )
);
