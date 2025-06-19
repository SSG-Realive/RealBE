'use client';

import { useEffect, useState } from 'react';
import { fetchWishlist, toggleWishlist } from '@/service/customer/wishlistService';
import { ProductListDTO } from '@/types/product';
import MypageCard from '../MypageCard';

export default function Wishlist() {
    const [products, setProducts] = useState<ProductListDTO[]>([]);

    useEffect(() => {
        fetchWishlist().then(setProducts);
    }, []);

    const handleToggle = async (productId: number) => {
        const result = await toggleWishlist({ productId });
        if (!result) return;

        // 찜 해제 후 목록에서 제거
        setProducts((prev) => prev.filter((p) => p.productId !== productId));
    };

    return (
        <MypageCard title="찜 목록">
            {products.length === 0 ? (
                <p className="text-gray-500">찜한 상품이 없습니다.</p>
            ) : (
                <ul className="grid grid-cols-2 gap-4">
                    {products.map((p) => (
                        <li key={p.productId} className="border p-2 rounded">
                            <img
                                src={p.thumbnailUrl}
                                alt={p.name}
                                className="w-full h-32 object-cover rounded"
                            />
                            <div className="mt-1 text-sm font-medium">{p.name}</div>
                            <button
                                className="text-red-500 text-sm mt-1"
                                onClick={() => handleToggle(p.productId)}
                            >
                                ❤️ 찜 해제
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </MypageCard>
    );
}
