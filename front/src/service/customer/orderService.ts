import apiClient from '@/lib/apiClient';

export async function getOrderSummary() {
    const res = await apiClient.get('/customer/orders/summary');
    return res.data;
}
