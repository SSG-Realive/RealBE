'use client';

import { useAuthStore } from '@/store/customer/authStore';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { fetchMyProfile } from '@/app/api/customer/member/route';
import SearchBar from './SearchBar'; // ✅ 검색창 컴포넌트 임포트

interface NavbarProps {
  onSearch?: (keyword: string) => void;
}

export default function Navbar({ onSearch }: NavbarProps) {
  const router = useRouter();
  const pathname = usePathname();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useAuthStore((state) => state.logout);
  const userName = useAuthStore((state) => state.userName);
  const setUserName = useAuthStore((state) => state.setUserName);

  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);

  if (pathname === '/customer/member/login') return null;

  useEffect(() => {
    if (mounted && isAuthenticated() && !userName) {
      fetchMyProfile()
          .then((data) => setUserName(data.name))
          .catch((e) => console.error('회원 이름 조회 실패:', e));
    }
  }, [mounted, isAuthenticated, userName, setUserName]);

  const handleLogout = () => {
    logout();
    router.push('/main');
  };

  return (
      <nav className="bg-white shadow-md w-full">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* 왼쪽: 로고 */}
            <div className="flex-shrink-0">
              <Link href="/main" className="text-xl font-bold text-gray-800">
                Realive
              </Link>
            </div>

            {/* 가운데: 검색창 */}
            <div className="flex-1 flex justify-center px-4">
              <SearchBar onSearch={onSearch} />
            </div>

            {/* 오른쪽: 로그인/로그아웃 관련 */}
            {mounted && (
                <div className="flex items-center space-x-4">
                  {isAuthenticated() ? (
                      <>
                        {userName && (
                            <span className="text-gray-700 whitespace-nowrap hidden sm:inline">
                      {userName}님
                    </span>
                        )}
                        <Link
                            href="/customer/mypage"
                            className="text-gray-600 hover:text-gray-900"
                        >
                          마이페이지
                        </Link>
                        <Link
                            href="/customer/cart"
                            className="text-gray-600 hover:text-gray-900"
                        >
                          장바구니
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
            )}
          </div>
        </div>
      </nav>
  );
}
