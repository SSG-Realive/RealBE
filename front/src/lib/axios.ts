import { useAuthStore } from '@/store/authStore';
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_ROOT_URL,
});

// 토큰이 필요없는 public API 경로들
const publicPaths = [
  '/public/auth/login',
  '/public/auth/join'
];

api.interceptors.request.use((config) => {
  // public API 경로인 경우 토큰을 포함하지 않음
  const isPublicPath = publicPaths.some(path => config.url?.includes(path));
  if (!isPublicPath) {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
    }
    return Promise.reject(error);
  }
);

export default api; 