"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from 'next/navigation';

const dummyCustomers = [
  { id: 1, name: '홍길동', email: 'hong@test.com', status: 'Active', image: 'https://randomuser.me/api/portraits/men/11.jpg' },
  { id: 2, name: '김영희', email: 'kim@test.com', status: 'Blocked', image: 'https://randomuser.me/api/portraits/women/12.jpg' },
  { id: 3, name: '이수민', email: 'soo@test.com', status: 'Active', image: 'https://randomuser.me/api/portraits/men/13.jpg' },
  { id: 4, name: '박철수', email: 'park@test.com', status: 'Active', image: 'https://randomuser.me/api/portraits/men/14.jpg' },
  { id: 5, name: '최지우', email: 'choi@test.com', status: 'Blocked', image: 'https://randomuser.me/api/portraits/women/15.jpg' },
  { id: 6, name: '정수빈', email: 'jung@test.com', status: 'Active', image: 'https://randomuser.me/api/portraits/women/16.jpg' },
  { id: 7, name: '한가람', email: 'han@test.com', status: 'Active', image: 'https://randomuser.me/api/portraits/men/17.jpg' },
  { id: 8, name: '오세훈', email: 'oh@test.com', status: 'Blocked', image: 'https://randomuser.me/api/portraits/men/18.jpg' },
  { id: 9, name: '유재석', email: 'yoo@test.com', status: 'Active', image: 'https://randomuser.me/api/portraits/men/19.jpg' },
  { id: 10, name: '강호동', email: 'kang@test.com', status: 'Active', image: 'https://randomuser.me/api/portraits/men/20.jpg' },
];

function CustomerListPage() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const router = useRouter();

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filtered = dummyCustomers.filter(c =>
    (c.name.includes(search) || c.email.includes(search)) &&
    (!status || c.status === status)
  );
  return (
    <div className="p-8">
      <div className="mb-4 flex gap-2">
        <input
          type="text"
          placeholder="이름/이메일 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded px-3 py-2"
        />
        <select
          value={status}
          onChange={e => setStatus(e.target.value)}
          className="border rounded px-3 py-2"
        >
          <option value="">전체</option>
          <option value="Active">Active</option>
          <option value="Blocked">Blocked</option>
        </select>
      </div>
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-2 py-1">사진</th>
            <th className="px-2 py-1">이름</th>
            <th className="px-2 py-1">이메일</th>
            <th className="px-2 py-1">상태</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((c) => (
            <tr key={c.id}>
              <td className="px-2 py-1"><img src={c.image} alt="customer" className="w-9 h-9 rounded-full object-cover" /></td>
              <td className="px-2 py-1">{c.name}</td>
              <td className="px-2 py-1">{c.email}</td>
              <td className="px-2 py-1">{c.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Object.assign(CustomerListPage, { pageTitle: '고객 관리' }); 