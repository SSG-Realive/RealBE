'use client';
import React, { useState } from 'react';
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";

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

const dummyPenalties = [
  { id: 1, user: "user1", reason: "부정입찰", date: "2024-06-01", userImage: "https://randomuser.me/api/portraits/men/21.jpg" },
  { id: 2, user: "user2", reason: "허위정보", date: "2024-06-02", userImage: "https://randomuser.me/api/portraits/women/22.jpg" },
  { id: 3, user: "user3", reason: "욕설", date: "2024-06-03", userImage: "https://randomuser.me/api/portraits/men/23.jpg" },
  { id: 4, user: "user4", reason: "도배", date: "2024-06-04", userImage: "https://randomuser.me/api/portraits/women/24.jpg" },
  { id: 5, user: "user5", reason: "광고성", date: "2024-06-05", userImage: "https://randomuser.me/api/portraits/men/25.jpg" },
  { id: 6, user: "user6", reason: "부정입찰", date: "2024-06-06", userImage: "https://randomuser.me/api/portraits/women/26.jpg" },
  { id: 7, user: "user7", reason: "허위정보", date: "2024-06-07", userImage: "https://randomuser.me/api/portraits/men/27.jpg" },
  { id: 8, user: "user8", reason: "욕설", date: "2024-06-08", userImage: "https://randomuser.me/api/portraits/women/28.jpg" },
  { id: 9, user: "user9", reason: "도배", date: "2024-06-09", userImage: "https://randomuser.me/api/portraits/men/29.jpg" },
  { id: 10, user: "user10", reason: "광고성", date: "2024-06-10", userImage: "https://randomuser.me/api/portraits/women/30.jpg" },
];

export default function AdminCustomersDashboard() {
  const router = useRouter();
  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }
  // 고객 요약
  const total = dummyCustomers.length;
  const active = dummyCustomers.filter(c => c.status === 'Active').length;
  const blocked = dummyCustomers.filter(c => c.status === 'Blocked').length;
  return (
    <div className="p-8 flex flex-row gap-8 overflow-x-auto">
      <h2 className="text-2xl font-bold mb-4">회원 대시보드</h2>
      <div className="mb-6 grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded shadow p-6">
          <h3 className="text-lg font-bold mb-2">전체 회원</h3>
          <div className="text-3xl font-bold">{total}</div>
        </div>
        <div className="bg-white rounded shadow p-6">
          <h3 className="text-lg font-bold mb-2">활성 회원</h3>
          <div className="text-3xl font-bold">{active}</div>
        </div>
        <div className="bg-white rounded shadow p-6">
          <h3 className="text-lg font-bold mb-2">차단 회원</h3>
          <div className="text-3xl font-bold">{blocked}</div>
        </div>
      </div>
      <div className="p-8 flex flex-row gap-8 overflow-x-auto">
        {/* 고객 관리 요약 - 테이블형 */}
        <div className="bg-white rounded shadow p-6 min-w-[400px]">
          <h2 className="text-lg font-bold mb-4">고객 관리</h2>
          <table className="min-w-full border text-sm">
            <thead>
              <tr className="bg-gray-100">
                <th className="px-2 py-1">이름</th>
                <th className="px-2 py-1">이메일</th>
                <th className="px-2 py-1">상태</th>
              </tr>
            </thead>
            <tbody>
              {dummyCustomers.slice(0, 5).map(c => (
                <tr key={c.id}>
                  <td className="px-2 py-1">{c.name}</td>
                  <td className="px-2 py-1">{c.email}</td>
                  <td className="px-2 py-1">{c.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {/* 사용자 패널티 요약 - 테이블형 */}
        <div className="bg-white rounded shadow p-6 min-w-[400px]">
          <h2 className="text-lg font-bold mb-4">사용자 패널티</h2>
          <table className="min-w-full border text-sm">
            <thead>
              <tr className="bg-gray-100">
                <th className="px-2 py-1">이름</th>
                <th className="px-2 py-1">사유</th>
                <th className="px-2 py-1">상태</th>
              </tr>
            </thead>
            <tbody>
              {dummyPenalties.slice(0, 5).map((p, idx) => (
                <tr key={idx}>
                  <td className="px-2 py-1">{p.user}</td>
                  <td className="px-2 py-1">{p.reason}</td>
                  <td className="px-2 py-1">{p.date}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
} 