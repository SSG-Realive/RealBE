'use client';

import { toggleWishlist } from '@/service/customer/wishlistService';
import { ProductListDTO } from '@/types/product';
import { useState } from 'react';

export default function ProductCard({ product }: { product: ProductListDTO }) {
    if (!product) return null; // 방어 처리

    const [isWished, setIsWished] = useState<boolean>(product.isWished ?? false);

    const handleToggle = async () => {
        const res = await toggleWishlist({ productId: product.productId });
        if (res) setIsWished(res.wishlist);
    };

    return (
        <div className="bg-white shadow rounded overflow-hidden border p-3">
            <img
                src={product.thumbnailUrl}
                alt={product.name}
                className="w-full h-48 object-cover"
            />
            <div className="mt-3">
                <p className="text-sm font-semibold truncate">{product.name}</p>
                <p className="text-green-600 font-bold text-sm">
                    {product.price.toLocaleString()}원
                </p>
                <button
                    onClick={handleToggle}
                    className="mt-2 text-xl"
                    aria-label="찜하기 버튼"
                >
                    {isWished ? '❤️' : '🤍'}
                </button>
            </div>
        </div>
    );
}