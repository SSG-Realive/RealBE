'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';

export default function OAuthCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const setAuth = useAuthStore((state) => state.setAuth);

  useEffect(() => {
    console.log('=== 콜백 페이지 실행 ===');
    console.log('현재 URL:', window.location.href);

    const token = searchParams.get('token');
    const email = searchParams.get('email');
    const isTemporaryUser = searchParams.get('temporaryUser') === 'true';

    console.log('받은 파라미터들:', {
      token: token?.substring(0, 20) + '...',
      email,
      isTemporaryUser,
    });

    const originalUrl = sessionStorage.getItem('loginRedirectUrl');
    console.log('sessionStorage에서 가져온 원래 URL:', originalUrl);

    if (token && email) {
      setAuth({ token, email, temporaryUser: isTemporaryUser });

      if (isTemporaryUser) {
        console.log('임시 회원 → 추가 정보 입력 페이지로 이동');
        router.push('/customer/socialsignup');
      } else {
        console.log('정상 회원 → 리다이렉트할 URL:', originalUrl || '/');
        router.push(originalUrl || '/');
      }
    } else {
      console.log('토큰 또는 이메일이 없음 → 로그인 페이지로 이동');
      router.push('/login?error=invalid_callback');
    }
  }, [searchParams, setAuth, router]);

  return <div>로그인 처리 중입니다...</div>;
}
