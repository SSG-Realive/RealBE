import api from '@/lib/axios';
import { MemberJoinDTO } from '@/types/custoemr/signup';

export const signup = async (formData: MemberJoinDTO) => {
  const response = await api.post('/api/public/auth/join', formData);
  return response.data; // { message, token, email, temporaryUser?, name? } 등 포함 예상
};
