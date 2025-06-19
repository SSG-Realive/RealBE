'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';

const dummySellers = [
  { id: 1, name: '이상훈', email: 'sang@test.com', status: '승인', image: 'https://randomuser.me/api/portraits/men/1.jpg' },
  { id: 2, name: '박지민', email: 'park@test.com', status: '승인 처리 전', image: 'https://randomuser.me/api/portraits/women/2.jpg' },
  { id: 4, name: '최민수', email: 'choi@test.com', status: '승인', image: 'https://randomuser.me/api/portraits/women/4.jpg' },
  { id: 5, name: '정가영', email: 'jung@test.com', status: '승인 처리 전', image: 'https://randomuser.me/api/portraits/men/5.jpg' },
  { id: 6, name: '한지민', email: 'han@test.com', status: '승인', image: 'https://randomuser.me/api/portraits/women/6.jpg' },
  { id: 8, name: '유재석', email: 'yoo@test.com', status: '승인', image: 'https://randomuser.me/api/portraits/men/8.jpg' },
  { id: 9, name: '강호동', email: 'kang@test.com', status: '승인 처리 전', image: 'https://randomuser.me/api/portraits/men/9.jpg' },
  { id: 10, name: '신동엽', email: 'shin@test.com', status: '승인', image: 'https://randomuser.me/api/portraits/men/10.jpg' },
];

export default function AdminSellersPage() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const router = useRouter();

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filtered = dummySellers.filter(s =>
    (s.name.includes(search) || s.email.includes(search)) &&
    (!status || s.status === status)
  );
  return (
    <div>
      <h2>판매자 관리</h2>
      <div style={{ marginBottom: 16, display: 'flex', gap: 8 }}>
        <input
          type="text"
          placeholder="이름/이메일 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ width: 200, padding: 4, border: '1px solid #ccc', borderRadius: 4 }}
        />
        <select
          value={status}
          onChange={e => setStatus(e.target.value)}
          style={{ padding: 4, border: '1px solid #ccc', borderRadius: 4 }}
        >
          <option value="">전체</option>
          <option value="승인">승인</option>
          <option value="승인 처리 전">승인 처리 전</option>
        </select>
      </div>
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 24 }}>
        <thead>
          <tr style={{ background: '#f7f7f7' }}>
            <th style={{ padding: 8, border: '1px solid #eee' }}>사진</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>이름</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>이메일</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>상태</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((s) => (
            <tr key={s.id}>
              <td style={{ padding: 8, border: '1px solid #eee' }}><img src={s.image} alt="seller" style={{ width: 36, height: 36, borderRadius: '50%', objectFit: 'cover' }} /></td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{s.name}</td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{s.email}</td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>
                {s.status === '승인 처리 전' ? (
                  <button
                    style={{ background: '#4caf50', color: '#fff', padding: '4px 12px', borderRadius: 4, border: 'none', fontWeight: 'bold' }}
                    onClick={() => alert(`${s.name} 판매자 승인처리! (추후 구현)`)}
                  >
                    승인처리
                  </button>
                ) : (
                  s.status
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
} 