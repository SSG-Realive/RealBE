'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { login } from '@/service/customer/loginService';
import { useAuthStore } from '@/store/customer/authStore';

export default function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const setToken = useAuthStore((state) => state.setToken);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState('');

  useEffect(() => {
    // URL에서 에러 파라미터 확인
    const errorParam = searchParams?.get('error');
    if (errorParam === 'kakao_login_failed') {
      setError('카카오 로그인에 실패했습니다. 다시 시도해주세요.');
    }
  }, [searchParams]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const setAuth = useAuthStore((state) => state.setAuth);
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // 1. 이벤트(e)가 발생한 form 요소를 직접 가져옵니다.
    const form = e.currentTarget;
    // 2. form 안의 input 요소들을 이름으로 접근할 수 있도록 타입을 지정합니다.
    const formElements = form.elements as typeof form.elements & {
        email: { value: string };
        password: { value: string };
    };

    // 3. state 대신, input 요소에서 직접 값을 읽어옵니다.
    const payload = {
        email: formElements.email.value,
        password: formElements.password.value,
    };

    console.log("--- [최종 확인] 입력창에서 직접 읽은 데이터 ---");
    console.log("전송할 페이로드:", payload);
    console.log("전송할 비밀번호 값:", payload.password);
    console.log("---------------------------------------");

    try {
      const data = await login(formData); // Spring 서버로 직접 요청
      if (data?.accessToken) {
        setAuth({
          token: data.accessToken,
          email: data.email,
          name: data.name,
          temporaryUser: false, // 이건 백엔드 판단에 따라 다를 수 있음
        });

        const redirectTo = searchParams?.get('redirectTo');
        router.push(redirectTo || '/');
      } else {
        setError('로그인 응답이 올바르지 않습니다.');
      }

    } catch (err) {
        console.error('Login error:', err);
        setError('로그인 처리 중 오류가 발생했습니다.');
    }
};

  const handleKakaoLogin = () => {
    const state = crypto.randomUUID();
    
    // URL에서 원래 가려던 페이지 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const originalPage = urlParams.get('redirectTo') || '/';
    
    // 원래 페이지를 sessionStorage에 저장
    sessionStorage.setItem('loginRedirectUrl', originalPage);
    
    const redirectTo = encodeURIComponent(`${window.location.origin}/customer/oauth/callback`);
    
    window.location.href = `${process.env.NEXT_PUBLIC_API_ROOT_URL}/oauth2/authorization/kakao?state=${state}&redirectTo=${redirectTo}`;
  };


  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-gray-700">
          이메일
        </label>
        <input
          type="email"
          id="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          required
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          placeholder="이메일을 입력하세요"
        />
      </div>
      <div>
        <label htmlFor="password" className="block text-sm font-medium text-gray-700">
          비밀번호
        </label>
        <input
          type="password"
          id="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          required
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          placeholder="비밀번호를 입력하세요"
        />
      </div>
      {error && (
        <p className="text-red-500 text-sm text-center">{error}</p>
      )}
      <button
        type="submit"
        className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700"
      >
        로그인
      </button>
      
      <div className="relative my-4">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-gray-300"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="px-2 bg-white text-gray-500">또는</span>
        </div>
      </div>

      <button
        type="button"
        onClick={handleKakaoLogin}
        className="w-full bg-[#FEE500] text-[#000000] py-2 px-4 rounded-md hover:bg-[#FDD835] flex items-center justify-center gap-2"
      >
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M12 3C6.477 3 2 6.463 2 10.702c0 2.682 1.76 5.035 4.4 6.4-.19.882-.74 3.22-.85 3.72-.13.55.2.53.37.39.14-.11 2.2-1.51 2.2-1.51 1.17.17 2.38.26 3.58.26 5.523 0 10-3.463 10-7.702S17.523 3 12 3z"
            fill="#000000"
          />
        </svg>
        카카오로 로그인
      </button>
    </form>
  );
} 