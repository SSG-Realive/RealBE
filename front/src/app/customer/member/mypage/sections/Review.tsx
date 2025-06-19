'use client';

import { useEffect, useState } from 'react';
import { getReviewSummary } from '@/service/customer/reviewService';

interface ReviewSummary {
    writable: number;
    written: number;
}

export default function Review() {
    const [summary, setSummary] = useState<ReviewSummary | null>(null);

    useEffect(() => {
        getReviewSummary().then(setSummary).catch(console.error);
    }, []);

    if (!summary) return <div>Loading...</div>;

    return (
        <div className="border p-4 rounded shadow bg-white">
            <h2 className="text-lg font-semibold mb-2">⭐ 리뷰 관리</h2>
            <ul className="text-sm text-gray-700">
                <li>작성 가능한 리뷰: {summary.writable}</li>
                <li>작성한 리뷰: {summary.written}</li>
            </ul>
        </div>
    );
}
