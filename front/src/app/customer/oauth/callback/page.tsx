'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';


export default function OAuthCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const setAuth = useAuthStore((state) => state.setAuth);

  useEffect(() => {
    const token = searchParams.get('token');
    const email = searchParams.get('email');
    const isTemporaryUser = searchParams.get('temporaryUser') === 'true';

    if (token && email) {
      setAuth({ token, email, temporaryUser: isTemporaryUser });
      router.push('/'); // 로그인 후 이동할 경로
    } else {
      router.push('/login?error=invalid_callback');
    }
  }, [searchParams, setAuth, router]);

  return <div>로그인 처리 중입니다...</div>;
}
