import { NextResponse } from 'next/server';  // Next.js 응답 객체
import { LoginRequest, LoginResponse } from '@/app/types/customer/login';
import api from '@/app/lib/axios';  // 커스텀 axios 인스턴스
import axios from 'axios';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const credentials: LoginRequest = {
      email: body.email,
      password: body.password
    };

    const response = await api.post('/api/public/auth/login', credentials);
    
    return NextResponse.json({
      success: true,
      data: response.data
    });
  } catch (error: unknown) {
    console.error('Login error:', error);
    
    if (axios.isAxiosError(error) && error.response?.data?.message) {
      return NextResponse.json(
        { success: false, message: error.response.data.message },
        { status: error.response.status || 400 }
      );
    }
    
    return NextResponse.json(
      { success: false, message: '로그인에 실패했습니다.' },
      { status: 400 }
    );
  }
}