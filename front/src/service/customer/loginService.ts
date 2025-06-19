import api from '@/lib/axios';
import { LoginRequest, LoginResponse } from '@/types/custoemr/login';

// 로그인 API 호출
export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  const response = await api.post<LoginResponse>('/api/public/auth/login', credentials);
  return response.data;
};