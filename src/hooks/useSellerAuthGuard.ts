// src/hooks/useSellerAuthGuard.ts

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore';
import { useHasHydrated } from './useHasHydrated';

/**
 * Seller 페이지 인증 가드 훅.
 * 하이드레이션 완료 후 토큰을 검사하며, 없으면 로그인 페이지로 보냅니다.
 * @returns {boolean} 인증 확인 중이면 true, 완료되면 false를 반환합니다.
 */
export default function useSellerAuthGuard() {
  const router = useRouter();
  const hasHydrated = useHasHydrated();
  const token = useSellerAuthStore((state) => state.token);
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    // 하이드레이션이 끝날 때까지 기다립니다.
    if (!hasHydrated) {
      return;
    }

    // 하이드레이션 후 토큰이 없으면 로그인 페이지로 보냅니다.
    if (!token) {
      router.replace('/seller/login');
    } else {
      // 토큰이 있으면 확인 절차를 종료합니다.
      setIsChecking(false);
    }
  }, [hasHydrated, token, router]);

  return isChecking;
}