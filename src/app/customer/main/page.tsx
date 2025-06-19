'use client';

import { useEffect, useRef, useState } from 'react';
import CustomerHeader from '@/components/customer/CustomerHeader';
import PopularProducts from '@/components/customer/PopularProducts';
import ChatbotFloatingButton from '@/components/customer/ChatbotFloatingButton';
import ProductCard from '@/components/customer/ProductCard';
import Navbar from '@/components/customer/Navbar';

interface Product {
    id: number;
    name: string;
    imageUrl: string;
    price: number;
    category: string;
}

// 전체 mock 상품 100개 생성 (카테고리 포함)
const mockProducts: Product[] = Array.from({ length: 100 }, (_, i) => {
    const categories = ['의자', '책상', '침대', '소파'];
    return {
        id: i + 1,
        name: `상품 ${i + 1}`,
        imageUrl: '/sample.jpg',
        price: 30000 + i * 1000,
        category: categories[i % categories.length],
    };
});

const categories = ['전체', '의자', '책상', '침대', '소파'];

export default function CustomerHomePage() {
    const [selectedCategory, setSelectedCategory] = useState('전체');
    const [products, setProducts] = useState<Product[]>([]);
    const [page, setPage] = useState(1);
    const loader = useRef<HTMLDivElement | null>(null);
    const ITEMS_PER_PAGE = 20;

    const getFilteredProducts = () => {
        return selectedCategory === '전체'
            ? mockProducts
            : mockProducts.filter((p) => p.category === selectedCategory);
    };

    const loadMore = () => {
        const filtered = getFilteredProducts();
        const next = filtered.slice((page - 1) * ITEMS_PER_PAGE, page * ITEMS_PER_PAGE);
        setProducts((prev) => [...prev, ...next]);
    };

    // 무한 스크롤 감지
    useEffect(() => {
        if (!loader.current) return;

        const observer = new IntersectionObserver(([entry]) => {
            if (entry.isIntersecting) {
                setPage((prev) => prev + 1);
            }
        }, { rootMargin: '100px' });

        observer.observe(loader.current);
        return () => {
            if (loader.current) observer.unobserve(loader.current);
        };
    }, []);

    // 페이지 변경 시 load
    useEffect(() => {
        loadMore();
    }, [page]);

    // 카테고리 변경 시 초기화
    useEffect(() => {
        setPage(1);
        const filtered = getFilteredProducts();
        const next = filtered.slice(0, ITEMS_PER_PAGE);
        setProducts(next);
    }, [selectedCategory]);

    return (
        <div>
            <Navbar/>

            {/* 🔥 인기 상품 */}
            <div className="px-4 py-4">
                <h2 className="text-xl font-semibold mb-2">🔥 인기 상품</h2>
                <PopularProducts />
            </div>

            {/* 카테고리 필터 */}
            <div className="flex gap-3 overflow-x-auto mb-6 px-4 py-2">
                {categories.map((category) => (
                    <button
                        key={category}
                        onClick={() => setSelectedCategory(category)}
                        className={`px-4 py-1 rounded-full border text-sm whitespace-nowrap ${
                            selectedCategory === category
                                ? 'bg-green-600 text-white border-green-600'
                                : 'bg-white text-gray-700 border-gray-300'
                        }`}
                    >
                        {category}
                    </button>
                ))}
            </div>

            {/* 전체 상품 목록 (무한스크롤) */}
            <div className="px-4 py-6 grid grid-cols-2 md:grid-cols-4 gap-4">
                {products.map((p) => (
                    <ProductCard key={p.id} {...p} />
                ))}
                <div ref={loader} className="h-10 col-span-full"></div>
            </div>

            <ChatbotFloatingButton />
        </div>
    );
}