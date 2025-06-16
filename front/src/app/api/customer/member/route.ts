import api from '@/app/lib/axios';
import { MemberReadDTO, MemberModifyDTO } from '@/app/types/customer/member';

// 내 정보 조회
export const fetchMyProfile = async (): Promise<MemberReadDTO> => {
  const res = await api.get('/api/customer/member/me');
  return res.data;
};

// 내 정보 수정
export const updateMyProfile = async (data: MemberModifyDTO): Promise<void> => {
  await api.put('/api/customer/member/me', data);
};

// 회원 탈퇴
export const deleteMyAccount = async (): Promise<string> => {
  const res = await api.delete('/api/customer/member/me');
  return res.data; // "회원 탈퇴가 정상 처리되었습니다."
};
