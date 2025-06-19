export interface ReviewResponse {
    id: number;
    rating: number;
    content: string;
    productId: number;
    customerNickname: string;
    createdAt: string;
    images: string[];
}

export interface ReviewCreateRequest {
    rating: number;
    content: string;
    productId: number;
    images?: File[];
}
