// src/store/seller/useSellerAuthStore.ts

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

// 1. 스토어의 타입 정의
interface SellerAuthState {
  token: string | null;
  setToken: (token: string) => void;
  logout: () => void;
}

// 2. 스토어 생성
export const useSellerAuthStore = create<SellerAuthState>()(
  persist(
    (set) => ({
      // 상태 (State)
      token: null,

      // 액션 (Actions)
      setToken: (newToken) => set({ token: newToken }),
      logout: () => set({ token: null }),
    }),
    {
      // 3. Persist 설정
      name: 'seller-auth-storage', // localStorage에 저장될 키 이름
      storage: createJSONStorage(() => localStorage), // 사용할 스토리지 지정
    }
  )
);