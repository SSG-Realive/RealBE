'use client';

import { useEffect, useState } from 'react';
import { getOrderSummary } from '@/service/customer/orderService';

interface OrderSummary {
    totalOrders: number;
    deliveryInProgress: number;
    completed: number;
}

export default function OrderStatus() {
    const [summary, setSummary] = useState<OrderSummary | null>(null);

    useEffect(() => {
        getOrderSummary().then(setSummary).catch(console.error);
    }, []);

    if (!summary) return <div>Loading...</div>;

    return (
        <div className="border p-4 rounded shadow bg-white">
            <h2 className="text-lg font-semibold mb-2">📦 주문 및 배송 현황</h2>
            <ul className="text-sm text-gray-700">
                <li>총 주문: {summary.totalOrders}</li>
                <li>배송 중: {summary.deliveryInProgress}</li>
                <li>배송 완료: {summary.completed}</li>
            </ul>
        </div>
    );
}
