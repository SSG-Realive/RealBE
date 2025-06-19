import { AdminDashboardDTO } from '@/types/admin/admin';
import apiClient from '@/lib/apiClient';
import { ApiResponse } from '@/types/admin/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

const defaultHeaders = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${typeof window !== 'undefined' ? localStorage.getItem('accessToken') : ''}`,
};

// // 더미 데이터
// const dummyDashboardData: AdminDashboardDTO = {
//   queryDate: new Date().toISOString().split('T')[0],
//   periodType: 'DAILY',
//   pendingSellerCount: 5,
//   productLog: {
//     salesWithCommissions: [
//       {
//         salesLog: {
//           id: 1,
//           orderItemId: 101,
//           productId: 201,
//           sellerId: 301,
//           customerId: 401,
//           quantity: 2,
//           unitPrice: 50000,
//           totalPrice: 100000,
//           soldAt: '2024-03-20T10:00:00Z'
//         },
//         commissionLog: {
//           id: 1,
//           salesLogId: 1,
//           commissionRate: 0.1,
//           commissionAmount: 10000,
//           recordedAt: '2024-03-20T10:00:00Z'
//         }
//       }
//     ],
//     payoutLogs: [
//       {
//         id: 1,
//         sellerId: 301,
//         periodStart: '2024-03-01',
//         periodEnd: '2024-03-31',
//         totalSales: 1000000,
//         totalCommission: 100000,
//         payoutAmount: 900000,
//         processedAt: '2024-04-01T00:00:00Z'
//       }
//     ]
//   },
//   penaltyLogs: [
//     {
//       id: 1,
//       customerId: 401,
//       reason: '부적절한 리뷰',
//       points: -10,
//       description: '비방성 리뷰 작성',
//       createdAt: '2024-03-20T15:00:00Z'
//     }
//   ],
//   memberSummaryStats: {
//     totalMembers: 120,
//     newMembersInPeriod: 15,
//     uniqueVisitorsInPeriod: 300,
//     engagedUsersInPeriod: 200,
//     activeUsersInPeriod: 150
//   },
//   salesSummaryStats: {
//     totalOrdersInPeriod: 50,
//     totalRevenueInPeriod: 5000000,
//     totalFeesInPeriod: 500000
//   },
//   auctionSummaryStats: {
//     totalAuctionsInPeriod: 30,
//     totalBidsInPeriod: 150,
//     averageBidsPerAuctionInPeriod: 5
//   },
//   reviewSummaryStats: {
//     totalReviewsInPeriod: 100,
//     newReviewsInPeriod: 20,
//     averageRatingInPeriod: 4.5,
//     deletionRate: 0.05
//   }
// };

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || `HTTP error! status: ${response.status}`);
  }
  const data = await response.json();
  return data.data;
}

export async function getAdminDashboard(date: string, periodType: string): Promise<AdminDashboardDTO> {
  try {
    const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : '';
    const response = await apiClient.get<ApiResponse<AdminDashboardDTO>>(
      `/api/admin/stats/main-dashboard?date=${date}&periodType=${periodType}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      }
    );
    return response.data.data;
  } catch (error) {
    console.error('Failed to fetch admin dashboard:', error);
    throw error;
  }
}

export async function getSalesStatistics(
  startDate: string,
  endDate: string,
  sellerId?: number,
  sortBy?: string
) {
  try {
    const params = new URLSearchParams({
      startDate,
      endDate,
      ...(sellerId && { sellerId: sellerId.toString() }),
      ...(sortBy && { sortBy }),
    });

    const response = await apiClient.get(
      `/api/admin/stats/sales-period?${params.toString()}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch sales statistics:', error);
    throw error;
  }
}

export async function getAuctionStatistics(startDate: string, endDate: string) {
  try {
    const response = await apiClient.get(
      `/api/admin/stats/auctions-period?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch auction statistics:', error);
    throw error;
  }
}

export async function getMemberStatistics(startDate: string, endDate: string) {
  try {
    const response = await apiClient.get(
      `/api/admin/stats/members-period?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch member statistics:', error);
    throw error;
  }
}

export async function getReviewStatistics(startDate: string, endDate: string) {
  try {
    const response = await apiClient.get(
      `/api/admin/stats/reviews-period?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch review statistics:', error);
    throw error;
  }
}

interface AdminLoginResponse {
  accessToken: string;
  refreshToken: string;
}

export async function adminLogin(email: string, password: string): Promise<AdminLoginResponse> {
  try {
    const response = await apiClient.post<AdminLoginResponse>('/api/admin/login', {
      email,
      password
    });
    
    // 로그인 성공 시 토큰 저장
    localStorage.setItem('accessToken', response.data.accessToken);
    localStorage.setItem('refreshToken', response.data.refreshToken);
    
    return response.data;
  } catch (error) {
    console.error('Failed to login:', error);
    throw error;
  }
} 