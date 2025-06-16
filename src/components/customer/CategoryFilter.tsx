// src/components/CategoryFilter.tsx
'use client';

import { useState } from 'react';

const categories = ['전체', '의자', '책상', '침대', '소파', '수납장', '기타'];

export default function CategoryFilter({
                                           onSelect,
                                       }: {
    onSelect: (category: string) => void;
}) {
    const [selected, setSelected] = useState('전체');

    return (
        <div className="flex gap-3 overflow-x-auto px-4 py-2">
            {categories.map((category) => (
                <button
                    key={category}
                    onClick={() => {
                        setSelected(category);
                        onSelect(category);
                    }}
                    className={`px-4 py-1 rounded-full border text-sm whitespace-nowrap ${
                        selected === category
                            ? 'bg-green-600 text-white border-green-600'
                            : 'bg-white text-gray-700 border-gray-300'
                    }`}
                >
                    {category}
                </button>
            ))}
        </div>
    );
}
