"use client";
import React, { useState } from "react";

interface Seller {
  id: number;
  name: string;
  email: string;
  businessNumber: string;
  joinedAt: string;
  status: "승인" | "대기" | "정지";
}

const dummySellers: Seller[] = [
  { id: 1, name: "이상훈", email: "lee@test.com", businessNumber: "123-45-67890", joinedAt: "2024-02-15", status: "승인" },
  { id: 2, name: "박지민", email: "park@test.com", businessNumber: "987-65-43210", joinedAt: "2024-03-05", status: "대기" },
  { id: 3, name: "김영희", email: "kim@test.com", businessNumber: "111-22-33333", joinedAt: "2024-03-10", status: "정지" },
  { id: 4, name: "최민수", email: "choi@test.com", businessNumber: "444-55-66666", joinedAt: "2024-03-12", status: "승인" },
  { id: 5, name: "정가영", email: "jung@test.com", businessNumber: "777-88-99999", joinedAt: "2024-03-14", status: "대기" },
  { id: 6, name: "한지민", email: "han@test.com", businessNumber: "222-33-44444", joinedAt: "2024-03-16", status: "승인" },
  { id: 7, name: "오세훈", email: "oh@test.com", businessNumber: "555-66-77777", joinedAt: "2024-03-18", status: "정지" },
  { id: 8, name: "유재석", email: "yoo@test.com", businessNumber: "888-99-00000", joinedAt: "2024-03-20", status: "승인" },
  { id: 9, name: "강호동", email: "kang@test.com", businessNumber: "333-44-55555", joinedAt: "2024-03-22", status: "대기" },
  { id: 10, name: "신동엽", email: "shin@test.com", businessNumber: "666-77-88888", joinedAt: "2024-03-24", status: "승인" },
];

export default function SellerManagementPage() {
  const [filter, setFilter] = useState("");
  const [selected, setSelected] = useState<Seller | null>(null);

  const filtered = dummySellers.filter(s => s.name.includes(filter) || s.email.includes(filter) || s.businessNumber.includes(filter));

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">판매자</h1>
        <button
          className="bg-green-500 text-white px-4 py-2 rounded"
          onClick={() => alert('판매자 승인처리 기능(추후 구현)')}
        >
          판매자 승인처리
        </button>
      </div>
      <div className="mb-4">
        <input
          type="text"
          placeholder="이름/이메일/사업자번호 검색"
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      <table className="min-w-full border">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-4 py-2">이름</th>
            <th className="px-4 py-2">이메일</th>
            <th className="px-4 py-2">사업자번호</th>
            <th className="px-4 py-2">가입일</th>
            <th className="px-4 py-2">상태</th>
            <th className="px-4 py-2">상세</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(s => (
            <tr key={s.id}>
              <td className="px-4 py-2">{s.name}</td>
              <td className="px-4 py-2">{s.email}</td>
              <td className="px-4 py-2">{s.businessNumber}</td>
              <td className="px-4 py-2">{s.joinedAt}</td>
              <td className="px-4 py-2">{s.status}</td>
              <td className="px-4 py-2">
                <button className="text-blue-600 underline" onClick={() => setSelected(s)}>
                  상세
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[300px]">
            <h2 className="text-xl font-bold mb-4">판매자 상세</h2>
            <p><b>이름:</b> {selected.name}</p>
            <p><b>이메일:</b> {selected.email}</p>
            <p><b>사업자번호:</b> {selected.businessNumber}</p>
            <p><b>가입일:</b> {selected.joinedAt}</p>
            <p><b>상태:</b> {selected.status}</p>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelected(null)}>
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
} 