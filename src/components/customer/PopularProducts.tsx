// src/components/PopularProducts.tsx
import ProductCard from './ProductCard';

const mockPopular = [
    { name: '모던 소파', imageUrl: '/sample1.jpg', price: 120000 },
    { name: '수납 침대', imageUrl: '/sample2.jpg', price: 85000 },
    { name: '1인용 책상', imageUrl: '/sample3.jpg', price: 48000 },
    { name: '원목 의자', imageUrl: '/sample4.jpg', price: 29000 },
];

export default function PopularProducts() {
    return (
        <section className="px-4 py-6">
            <h2 className="text-lg font-bold mb-4">🔥 찜 많은 인기 상품</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {mockPopular.map((p, idx) => (
                    <ProductCard key={idx} {...p} />
                ))}
            </div>
        </section>
    );
}
