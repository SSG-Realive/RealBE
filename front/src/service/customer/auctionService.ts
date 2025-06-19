import api from '@/lib/axios';

export const fetchCustomerAuctions = async () => {
  const response = await api.get('/api/customer/auctions');
  return response.data;
};
