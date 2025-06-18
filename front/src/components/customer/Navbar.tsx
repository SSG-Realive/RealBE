'use client';

import { useAuthStore } from '@/store/authStore';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';


export default function Navbar() {
  const router = useRouter();
  const pathname = usePathname();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useAuthStore((state) => state.logout);

  // 로그인 페이지에서는 네비바를 숨김
  if (pathname === '/customer/member/login') {
    return null;
  }

  const handleLogout = () => {
    logout();
    router.push('/');
  };

  return (
    <nav className="bg-white shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link href="/" className="text-xl font-bold text-gray-800">
              Realive
            </Link>
          </div>
          
          <div className="flex items-center space-x-4">
            {isAuthenticated() ? (
              <>
                <Link 
                  href="/customer/member/mypage"
                  className="text-gray-600 hover:text-gray-900"
                >
                  마이페이지
                </Link>
                <button
                  onClick={handleLogout}
                  className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <Link
                href="/customer/member/login"
                className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
              >
                로그인
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
} 