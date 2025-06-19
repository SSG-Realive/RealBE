"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";

export default function PenaltyRegisterPage() {
  const router = useRouter();
  const [user, setUser] = useState("");
  const [reason, setReason] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // 실제 등록 로직은 API 연동 필요
    alert(`패널티가 등록되었습니다.\n사용자: ${user}\n사유: ${reason}`);
    router.push("/admin/customers/penalty");
  };

  return (
    <div className="p-8 max-w-md mx-auto">
      <h2 className="text-lg font-bold mb-4">패널티 등록</h2>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <input
          type="text"
          placeholder="사용자명"
          value={user}
          onChange={e => setUser(e.target.value)}
          className="border rounded px-3 py-2"
          required
        />
        <input
          type="text"
          placeholder="사유"
          value={reason}
          onChange={e => setReason(e.target.value)}
          className="border rounded px-3 py-2"
          required
        />
        <button type="submit" className="bg-blue-500 text-white rounded px-4 py-2">등록</button>
        <button type="button" className="bg-gray-300 text-black rounded px-4 py-2" onClick={() => router.push('/admin/customers/penalty')}>취소</button>
      </form>
    </div>
  );
} 