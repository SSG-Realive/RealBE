'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import SocialSignupForm from '@/components/customer/join/SocialSignupForm';

export default function SocialSignupPage() {
  const router = useRouter();
  const { email, token, isTemporaryUser, setUserName, setAuth } = useAuthStore();

  useEffect(() => {
    if (!email || !token || !isTemporaryUser) {
      router.replace('/');
    }
  }, [email, token, isTemporaryUser, router]);

  const handleSuccess = (name: string) => {
  setUserName(name);
  setAuth({ token: token || '', email: email || '', name, temporaryUser: false });
  alert('회원가입이 완료되었습니다!');
  router.push('/');
};

  if (!email || !token || !isTemporaryUser) return null;

  return (
    <div>
      <h1>추가 정보 입력</h1>
      <SocialSignupForm email={email} token={token} onSuccess={handleSuccess} />
    </div>
  );
}
