'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import Navbar from '@/components/customer/Navbar';

export default function MyPage() {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (mounted && !isAuthenticated()) {
      router.push('/customer/member/login?redirectTo=/customer/member/mypage');
    }
  }, [mounted, isAuthenticated, router]);

  if (!mounted) {
    return null;
  }

  if (!isAuthenticated()) {
    return null;
  }

  return (
    <>
      <Navbar />
      <div className="min-h-screen flex items-center justify-center">
        <h1 className="text-2xl font-bold">마이페이지</h1>
      </div>
    </>
  );
}
