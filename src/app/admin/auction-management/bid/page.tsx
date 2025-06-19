"use client";
import { useRouter } from "next/navigation";
import { useEffect } from 'react';

// 더미 데이터
const dummyBids = [
  { id: "1", user: "user1", amount: 100000, date: "2024-06-01", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/1.jpg" },
  { id: "2", user: "user2", amount: 120000, date: "2024-06-02", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/2.jpg" },
  { id: "3", user: "user3", amount: 150000, date: "2024-06-02", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/3.jpg" },
  { id: "4", user: "user4", amount: 200000, date: "2024-06-03", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/4.jpg" },
  { id: "5", user: "user5", amount: 90000, date: "2024-06-04", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/5.jpg" },
  { id: "6", user: "user6", amount: 110000, date: "2024-06-05", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/6.jpg" },
  { id: "7", user: "user7", amount: 130000, date: "2024-06-06", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/7.jpg" },
  { id: "8", user: "user8", amount: 170000, date: "2024-06-07", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/8.jpg" },
  { id: "9", user: "user9", amount: 95000, date: "2024-06-08", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/9.jpg" },
  { id: "10", user: "user10", amount: 105000, date: "2024-06-09", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/10.jpg" },
];

export default function BidHistoryPage() {
  const router = useRouter();

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  return (
    <div className="p-8">
      <input className="mb-4 border px-2 py-1" placeholder="입찰자 검색" />
      <table>
        <thead>
          <tr>
            <th>User</th>
            <th>Amount</th>
            <th>Date</th>
            <th>낙찰자</th>
            <th>낙찰가</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {dummyBids.map((bid) => (
            <tr key={bid.id}>
              <td>{bid.user}</td>
              <td>{bid.amount.toLocaleString()}원</td>
              <td>{bid.date}</td>
              <td>{bid.winner}</td>
              <td>{bid.winAmount.toLocaleString()}원</td>
              <td>
                <button onClick={() => router.push(`/admin/auction-management/bid/${bid.id}`)}>View</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
} 