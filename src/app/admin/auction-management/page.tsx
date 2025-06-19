"use client";
import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";

// Auction 타입 정의
interface Auction {
  id: number;
  name: string;
  product: string;
  seller: string;
  start: string;
  end: string;
  status: string;
  image: string;
}

export default function AuctionManagementPage() {
  const router = useRouter();
  // 더미 데이터
  const [auctionSearch, setAuctionSearch] = useState("");
  const [bidSearch, setBidSearch] = useState("");
  const [penaltySearch, setPenaltySearch] = useState("");
  const [penaltyUser, setPenaltyUser] = useState("");
  const [penaltyReason, setPenaltyReason] = useState("");
  const [selectedAuction, setSelectedAuction] = useState<Auction | null>(null);

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const dummyAuctions: Auction[] = [
    {
      id: 1,
      name: "노트북 경매",
      product: "노트북",
      seller: "홍길동",
      start: "2024-06-01",
      end: "2024-06-05",
      status: "진행중",
      image: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=80",
    },
    {
      id: 2,
      name: "스마트폰 경매",
      product: "스마트폰",
      seller: "김철수",
      start: "2024-06-02",
      end: "2024-06-06",
      status: "종료",
      image: "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=400&q=80",
    },
    {
      id: 3,
      name: "자전거 경매",
      product: "자전거",
      seller: "이영희",
      start: "2024-06-03",
      end: "2024-06-07",
      status: "진행중",
      image: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=400&q=80",
    },
    {
      id: 4,
      name: "커피머신 경매",
      product: "커피머신",
      seller: "박민수",
      start: "2024-06-04",
      end: "2024-06-08",
      status: "종료",
      image: "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=400&q=80",
    },
    {
      id: 5,
      name: "의자 경매",
      product: "의자",
      seller: "최지우",
      start: "2024-06-05",
      end: "2024-06-09",
      status: "진행중",
      image: "https://images.unsplash.com/photo-1519710164239-da123dc03ef4?auto=format&fit=crop&w=400&q=80",
    },
    {
      id: 6,
      name: "모니터 경매",
      product: "모니터",
      seller: "정우성",
      start: "2024-06-06",
      end: "2024-06-10",
      status: "종료",
      image: "https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=crop&w=400&q=80",
    },
    {
      id: 7,
      name: "책상 경매",
      product: "책상",
      seller: "이민호",
      start: "2024-06-07",
      end: "2024-06-11",
      status: "진행중",
      image: "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?auto=format&fit=crop&w=400&q=80",
    },
    {
      id: 8,
      name: "스피커 경매",
      product: "스피커",
      seller: "박지성",
      start: "2024-06-08",
      end: "2024-06-12",
      status: "종료",
      image: "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=400&q=80",
    },
    {
      id: 9,
      name: "프린터 경매",
      product: "프린터",
      seller: "김연아",
      start: "2024-06-09",
      end: "2024-06-13",
      status: "진행중",
      image: "https://images.unsplash.com/photo-1519985176271-adb1088fa94c?auto=format&fit=crop&w=400&q=80",
    },
    {
      id: 10,
      name: "램프 경매",
      product: "램프",
      seller: "손흥민",
      start: "2024-06-10",
      end: "2024-06-14",
      status: "종료",
      image: "https://images.unsplash.com/photo-1501594907352-04cda38ebc29?auto=format&fit=crop&w=400&q=80",
    },
  ];
  const dummyBids = [
    { id: 1, user: "user1", amount: 100000, date: "2024-06-01" },
    { id: 2, user: "user2", amount: 120000, date: "2024-06-01" },
    { id: 3, user: "user3", amount: 150000, date: "2024-06-02" },
    { id: 4, user: "user4", amount: 200000, date: "2024-06-03" },
    { id: 5, user: "user5", amount: 90000, date: "2024-06-04" },
    { id: 6, user: "user6", amount: 110000, date: "2024-06-05" },
    { id: 7, user: "user7", amount: 130000, date: "2024-06-06" },
    { id: 8, user: "user8", amount: 170000, date: "2024-06-07" },
    { id: 9, user: "user9", amount: 95000, date: "2024-06-08" },
    { id: 10, user: "user10", amount: 105000, date: "2024-06-09" },
  ];
  // 더미 패널티 데이터
  const dummyPenalties = [
    { id: 1, user: "user1", reason: "부정입찰", date: "2024-06-01" },
    { id: 2, user: "user2", reason: "허위정보", date: "2024-06-02" },
    { id: 3, user: "user3", reason: "욕설", date: "2024-06-03" },
    { id: 4, user: "user4", reason: "도배", date: "2024-06-04" },
    { id: 5, user: "user5", reason: "광고성", date: "2024-06-05" },
    { id: 6, user: "user6", reason: "부정입찰", date: "2024-06-06" },
    { id: 7, user: "user7", reason: "허위정보", date: "2024-06-07" },
    { id: 8, user: "user8", reason: "욕설", date: "2024-06-08" },
    { id: 9, user: "user9", reason: "도배", date: "2024-06-09" },
    { id: 10, user: "user10", reason: "광고성", date: "2024-06-10" },
  ];

  const filteredAuctions = dummyAuctions.filter(a => a.name.includes(auctionSearch) || a.product.includes(auctionSearch) || a.seller.includes(auctionSearch));
  const filteredBids = dummyBids.filter(b => b.user.includes(bidSearch));
  const filteredPenalties = dummyPenalties.filter(p => p.user.includes(penaltySearch) || p.reason.includes(penaltySearch));

  return (
    <div className="p-8 flex flex-row gap-8 overflow-x-auto">
      {/* 경매 요약 - 테이블형 */}
      <div className="bg-white rounded shadow p-6 min-w-[400px]">
        <h2 className="text-lg font-bold mb-4">경매</h2>
        <table className="min-w-full border text-sm">
          <thead>
            <tr className="bg-gray-100">
              <th className="px-2 py-1">경매명</th>
              <th className="px-2 py-1">판매자</th>
              <th className="px-2 py-1">상태</th>
            </tr>
          </thead>
          <tbody>
            {filteredAuctions.slice(0, 5).map(a => (
              <tr key={a.id}>
                <td className="px-2 py-1">{a.name}</td>
                <td className="px-2 py-1">{a.seller}</td>
                <td className="px-2 py-1">{a.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {/* 입찰 내역 요약 - 테이블형 */}
      <div className="bg-white rounded shadow p-6 min-w-[400px]">
        <h2 className="text-lg font-bold mb-4">입찰 내역</h2>
        <table className="min-w-full border text-sm">
          <thead>
            <tr className="bg-gray-100">
              <th className="px-2 py-1">경매명</th>
              <th className="px-2 py-1">입찰자</th>
              <th className="px-2 py-1">금액</th>
            </tr>
          </thead>
          <tbody>
            {filteredBids.slice(0, 5).map((b, idx) => (
              <tr key={idx}>
                <td className="px-2 py-1">{b.user}</td>
                <td className="px-2 py-1">{b.amount.toLocaleString()}원</td>
                <td className="px-2 py-1">{b.date}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
} 