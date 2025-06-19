import apiClient from '@/lib/apiClient';

export async function getCustomerProfile() {
    const res = await apiClient.get('/customer/profile');
    return res.data;
}
