// components/SocialSignupForm.tsx
'use client';

import React from 'react';
import { Gender, useSocialSignupForm } from '@/hooks/useSocialSignupForm';

interface Props {
  email: string;
  token: string;
  onSuccess: (userName: string) => void;
}

export default function SocialSignupForm({ email, token, onSuccess }: Props) {
  const {
    form: { userName, phone, address, birth, gender },
    setUserName,
    setPhone,
    setAddress,
    setBirth,
    setGender,
    loading,
    setLoading,
    isValid,
  } = useSocialSignupForm(email);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    if (!isValid()) {
      alert('모든 필드를 입력해주세요.');
      setLoading(false);
      return;
    }

    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_ROOT_URL}/api/customer/member/update-info`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          email,
          password: 'aaaa1111', // 고정된 임시 비밀번호
          name: userName,
          phone,
          address,
          birth,
          gender,
        }),
      });

      if (!res.ok) throw new Error('회원가입 실패');

      onSuccess(userName);
    } catch (err) {
      console.error(err);
      alert('회원가입에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>이메일</label>
        <input value={email} readOnly />
      </div>
      <div>
        <label>닉네임</label>
        <input value={userName} onChange={(e) => setUserName(e.target.value)} />
      </div>
      <div>
        <label>전화번호</label>
        <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} />
      </div>
      <div>
        <label>주소</label>
        <input type="text" value={address} onChange={(e) => setAddress(e.target.value)} />
      </div>
      <div>
        <label>생년월일</label>
        <input type="date" value={birth} onChange={(e) => setBirth(e.target.value)} />
      </div>
      <div>
        <label>성별</label>
        <select value={gender} onChange={(e) => setGender(e.target.value as Gender)}>
          <option value="">선택하세요</option>
          <option value="MALE">남성</option>
          <option value="FEMALE">여성</option>
        </select>
      </div>
      <button type="submit" disabled={loading}>
        {loading ? '가입 중...' : '회원가입'}
      </button>
    </form>
  );
}
