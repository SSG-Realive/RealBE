import apiClient from '@/lib/apiClient';
import {
    ReviewCreateRequest,
    ReviewUpdateRequest,
    ReviewResponse,
    MyReviewResponse,
} from '@/types/customer/review;

// 🔹 특정 상품의 리뷰 목록 조회
export async function fetchProductReviews(productId: number): Promise<ReviewResponse[]> {
    const res = await apiClient.get(`/customer/reviews/product/${productId}`);
    return res.data;
}

// 🔹 리뷰 작성
export async function createReview(data: ReviewCreateRequest): Promise<void> {
    await apiClient.post('/customer/reviews', data);
}

// 🔹 리뷰 수정
export async function updateReview(reviewId: number, data: ReviewUpdateRequest): Promise<void> {
    await apiClient.put(`/customer/reviews/${reviewId}`, data);
}

// 🔹 리뷰 삭제
export async function deleteReview(reviewId: number): Promise<void> {
    await apiClient.delete(`/customer/reviews/${reviewId}`);
}

// 🔹 내가 쓴 리뷰 목록
export async function fetchMyReviews(): Promise<MyReviewResponse[]> {
    const res = await apiClient.get('/customer/reviews/my');
    return res.data;
}
