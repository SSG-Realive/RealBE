"use client";
import { useParams } from "next/navigation";
import { Card, CardContent } from "@/components/ui/card";
import Link from "next/link";

const dummyAuctions = [
  { id: 1, name: "노트북 경매", product: "노트북", seller: "홍길동", start: "2025-06-01", end: "2025-06-05", status: "진행중", image: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=80" },
  { id: 2, name: "스마트폰 경매", product: "스마트폰", seller: "김철수", start: "2025-06-02", end: "2025-06-06", status: "종료", image: "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=400&q=80", winner: "user3", winningPrice: "150,000원" },
  { id: 3, name: "자전거 경매", product: "자전거", seller: "이영희", start: "2025-06-03", end: "2025-06-07", status: "진행중", image: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=400&q=80" },
  { id: 4, name: "커피머신 경매", product: "커피머신", seller: "박민수", start: "2025-06-04", end: "2025-06-08", status: "종료", image: "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=400&q=80", winner: "user7", winningPrice: "200,000원" },
  { id: 5, name: "의자 경매", product: "의자", seller: "최지우", start: "2025-06-05", end: "2025-06-09", status: "진행중", image: "https://images.unsplash.com/photo-1519710164239-da123dc03ef4?auto=format&fit=crop&w=400&q=80" },
  { id: 6, name: "모니터 경매", product: "모니터", seller: "정우성", start: "2025-06-06", end: "2025-06-10", status: "종료", image: "https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=crop&w=400&q=80", winner: "user2", winningPrice: "110,000원" },
  { id: 7, name: "책상 경매", product: "책상", seller: "이민호", start: "2025-06-07", end: "2025-06-11", status: "진행중", image: "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?auto=format&fit=crop&w=400&q=80" },
  { id: 8, name: "스피커 경매", product: "스피커", seller: "박지성", start: "2025-06-08", end: "2025-06-12", status: "종료", image: "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=400&q=80", winner: "user5", winningPrice: "170,000원" },
  { id: 9, name: "프린터 경매", product: "프린터", seller: "김연아", start: "2025-06-09", end: "2025-06-13", status: "진행중", image: "https://images.unsplash.com/photo-1519985176271-adb1088fa94c?auto=format&fit=crop&w=400&q=80" },
  { id: 10, name: "램프 경매", product: "램프", seller: "손흥민", start: "2025-06-10", end: "2025-06-14", status: "종료", image: "https://images.unsplash.com/photo-1501594907352-04cda38ebc29?auto=format&fit=crop&w=400&q=80", winner: "user9", winningPrice: "105,000원" },
];

export default function AuctionDetailPage() {
  const params = useParams();
  const id = Number(params.id);
  const auction = dummyAuctions.find(a => a.id === id);

  if (!auction) return <div className="p-8">경매 정보를 찾을 수 없습니다.</div>;

  return (
    <div className="p-8 max-w-lg mx-auto">
      <Card>
        <CardContent className="flex flex-col gap-4 p-6">
          <img src={auction.image} alt="auction" className="w-full h-48 object-cover rounded" />
          <div className="font-bold text-xl">{auction.name}</div>
          <div>상품명: {auction.product}</div>
          <div>판매자: {auction.seller}</div>
          <div>시작일: {auction.start}</div>
          <div>종료일: {auction.end}</div>
          <div>상태: {auction.status}</div>
          <div>낙찰자: {auction.status === "종료" ? auction.winner || "-" : "-"}</div>
          <div>낙찰가: {auction.status === "종료" ? auction.winningPrice || "-" : "-"}</div>
          <Link href="/admin/auction-management/list" className="mt-4 text-blue-600 underline self-end">목록으로</Link>
        </CardContent>
      </Card>
    </div>
  );
} 