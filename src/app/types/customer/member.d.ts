export interface MemberReadDTO {
  email: string;
  name: string;
  phone?: string;
  // 필요한 필드 추가
}

export interface MemberModifyDTO {
  name: string;
  phone?: string;
  // 수정 가능한 필드만 포함
}
