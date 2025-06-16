// src/components/ProductCard.tsx
export default function ProductCard({
                                        name,
                                        imageUrl,
                                        price,
                                    }: {
    name: string;
    imageUrl: string;
    price: number;
}) {
    return (
        <div className="bg-white shadow rounded overflow-hidden">
            <img src={imageUrl} alt={name} className="w-full h-48 object-cover" />
            <div className="p-3">
                <p className="text-sm font-semibold truncate">{name}</p>
                <p className="text-green-600 font-bold text-sm">{price.toLocaleString()}원</p>
            </div>
        </div>
    );
}
