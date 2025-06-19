"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  stock: number;
  createdAt: string;
  status: "판매중" | "품절" | "숨김";
  productImage: string;
}

const dummyProducts: Product[] = [
  { id: 1, name: "무선 마우스", category: "전자기기", price: 25000, stock: 12, createdAt: "2024-03-01", status: "판매중", productImage: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=facearea&w=80&h=80" },
  { id: 2, name: "유선 키보드", category: "전자기기", price: 18000, stock: 0, createdAt: "2024-03-02", status: "품절", productImage: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=facearea&w=80&h=80" },
  { id: 3, name: "텀블러", category: "생활용품", price: 12000, stock: 30, createdAt: "2024-03-03", status: "판매중", productImage: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=facearea&w=80&h=80" },
  { id: 4, name: "노트북", category: "전자기기", price: 1200000, stock: 5, createdAt: "2024-03-04", status: "판매중", productImage: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=facearea&w=80&h=80" },
  { id: 5, name: "운동화", category: "패션", price: 69000, stock: 8, createdAt: "2024-03-05", status: "판매중", productImage: "https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=facearea&w=80&h=80" },
  { id: 6, name: "에코백", category: "패션", price: 15000, stock: 0, createdAt: "2024-03-06", status: "품절", productImage: "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=facearea&w=80&h=80" },
  { id: 7, name: "샴푸", category: "생활용품", price: 9000, stock: 40, createdAt: "2024-03-07", status: "판매중", productImage: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=facearea&w=80&h=80" },
  { id: 8, name: "스마트워치", category: "전자기기", price: 250000, stock: 3, createdAt: "2024-03-08", status: "숨김", productImage: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=facearea&w=80&h=80" },
  { id: 9, name: "블루투스 스피커", category: "전자기기", price: 45000, stock: 10, createdAt: "2024-03-09", status: "판매중", productImage: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=facearea&w=80&h=80" },
  { id: 10, name: "커피머신", category: "전자기기", price: 350000, stock: 2, createdAt: "2024-03-10", status: "판매중", productImage: "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=facearea&w=80&h=80" },
];

export default function ProductManagementPage() {
  const [filter, setFilter] = useState("");
  const [selected, setSelected] = useState<Product | null>(null);
  const router = useRouter();

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filtered = dummyProducts.filter(p => p.name.includes(filter) || p.category.includes(filter));

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">상품 관리</h1>
        <button
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          onClick={() => alert('상품 매입 기능(추후 구현)')}
        >
          상품 매입
        </button>
      </div>
      <div className="mb-4">
        <input
          type="text"
          placeholder="상품명/카테고리 검색"
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      <table className="min-w-full border">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-4 py-2">상품 사진</th>
            <th className="px-4 py-2">상품명</th>
            <th className="px-4 py-2">카테고리</th>
            <th className="px-4 py-2">가격</th>
            <th className="px-4 py-2">재고</th>
            <th className="px-4 py-2">등록일</th>
            <th className="px-4 py-2">상태</th>
            <th className="px-4 py-2">상세</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(p => (
            <tr key={p.id}>
              <td className="px-4 py-2"><img src={p.productImage} alt="product" className="w-10 h-10 rounded object-cover" /></td>
              <td className="px-4 py-2">{p.name}</td>
              <td className="px-4 py-2">{p.category}</td>
              <td className="px-4 py-2">{p.price.toLocaleString()}원</td>
              <td className="px-4 py-2">{p.stock}</td>
              <td className="px-4 py-2">{p.createdAt}</td>
              <td className="px-4 py-2">{p.status}</td>
              <td className="px-4 py-2">
                <button className="text-blue-600 underline" onClick={() => router.push(`/admin/products/${p.id}`)}>
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
            <h2 className="text-xl font-bold mb-4">상품 상세</h2>
            <img src={selected.productImage} alt="product" className="w-16 h-16 rounded object-cover mb-4" />
            <p><b>상품명:</b> {selected.name}</p>
            <p><b>카테고리:</b> {selected.category}</p>
            <p><b>가격:</b> {selected.price.toLocaleString()}원</p>
            <p><b>재고:</b> {selected.stock}</p>
            <p><b>등록일:</b> {selected.createdAt}</p>
            <p><b>상태:</b> {selected.status}</p>
            <div className="flex gap-2 mt-6">
              <button className="px-4 py-2 bg-blue-500 text-white rounded" onClick={() => router.push(`/admin/products/edit/${selected.id}`)}>수정</button>
              <button className="px-4 py-2 bg-green-500 text-white rounded" onClick={() => router.push(`/admin/products/stock/${selected.id}`)}>재고 관리</button>
              <button className="px-4 py-2 bg-gray-700 text-white rounded" onClick={() => router.push(`/admin/products/images/${selected.id}`)}>이미지 관리</button>
            </div>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelected(null)}>
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
} 