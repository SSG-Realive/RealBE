import { NextResponse } from 'next/server';
import api from '@/app/lib/axios';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const credentials = {
      email: body.email,
      password: body.password
    };

    const response = await api.post('/public/auth/login', credentials);
    
    return NextResponse.json({
      success: true,
      data: response.data
    });
  } catch (error: any) {
    console.error('Login error:', error);
    
    if (error.response?.data?.message) {
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