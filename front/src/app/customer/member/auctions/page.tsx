'use client';

import { useEffect, useRef, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/store/authStore';
import { useAuctionStore } from '@/store/auctionStore'; // Zustand store import
import Navbar from '@/components/customer/Navbar';

// 쓰로틀링 훅
function useThrottle<T extends (...args: any[]) => any>(
  callback: T,
  delay: number
): T {
  const lastCall = useRef<number>(0);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  return useCallback(
    ((...args: Parameters<T>) => {
      const now = Date.now();
      
      if (now - lastCall.current >= delay) {
        // 즉시 실행
        lastCall.current = now;
        callback(...args);
      } else {
        // 남은 시간 후 실행 (마지막 호출 보장)
        if (timeoutRef.current) {
          clearTimeout(timeoutRef.current);
        }
        
        timeoutRef.current = setTimeout(() => {
          lastCall.current = Date.now();
          callback(...args);
        }, delay - (now - lastCall.current));
      }
    }) as T,
    [callback, delay]
  );
}

// 로그인 확인 훅
function useRequireAuth() {
  const router = useRouter();
  const { token } = useAuthStore();

  useEffect(() => {
    if (!token) {
      const currentPath = window.location.pathname;
      router.push(`/customer/member/login?redirectTo=${encodeURIComponent(currentPath)}`);
    }
  }, [token, router]);

  return token;
}

export default function AuctionsPage() {
  const token = useRequireAuth();
  const router = useRouter();
  
  // Zustand store 사용
  const {
    auctions,
    category,
    page,
    hasNext,
    loading,
    error,
    setCategory,
    reset,
    fetchAuctions
  } = useAuctionStore();

  const observer = useRef<IntersectionObserver | null>(null);
  const isInitialized = useRef(false);

  // 쓰로틀링된 fetch 함수 (500ms 간격)
  const throttledFetch = useThrottle(() => {
    if (hasNext && !loading && token) {
      console.log('쓰로틀링된 fetch 실행 - 현재 페이지:', page);
      fetchAuctions();
    }
  }, 500);

  // 마지막 아이템 감지를 위한 ref callback
  const lastAuctionRef = useCallback((node: HTMLLIElement | null) => {
    if (loading || !hasNext) return;
    
    // 기존 observer 해제
    if (observer.current) observer.current.disconnect();

    // 새로운 observer 생성
    observer.current = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          console.log('무한스크롤 트리거 감지');
          throttledFetch();
        }
      },
      {
        threshold: 0.1,
        rootMargin: '50px', // 50px 전에 미리 로드
      }
    );

    if (node) observer.current.observe(node);
  }, [loading, hasNext, throttledFetch]);

  // 초기 데이터 로드
  useEffect(() => {
    if (token && !isInitialized.current && auctions.length === 0 && !loading) {
      console.log('초기 데이터 로드');
      isInitialized.current = true;
      fetchAuctions();
    }
  }, [token, auctions.length, loading, fetchAuctions]);

  // 카테고리 변경 처리
  const handleCategoryChange = (newCategory: string) => {
    console.log('카테고리 변경:', newCategory);
    setCategory(newCategory);
    isInitialized.current = false; // 새로운 카테고리로 초기화
  };

  // 컴포넌트 언마운트 시 정리
  useEffect(() => {
    return () => {
      if (observer.current) {
        observer.current.disconnect();
      }
      reset(); // store 상태 리셋
    };
  }, [reset]);

  // 카테고리 목록
  const categories = ['', '가구', '전자제품', '의류'];

  if (!token) return <div style={{ textAlign: 'center', padding: 20 }}>로그인 확인 중...</div>;
  if (error) return <div style={{ color: 'red', textAlign: 'center', padding: 20 }}>에러: {error}</div>;

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 900, margin: '0 auto', padding: 20 }}>
        <header style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
          <h1>경매 목록</h1>
        </header>

        {/* 카테고리 필터 */}
        <div style={{ marginBottom: 20 }}>
          <label htmlFor="category-select" style={{ marginRight: 10 }}>카테고리:</label>
          <select
            id="category-select"
            value={category}
            onChange={(e) => handleCategoryChange(e.target.value)}
            disabled={loading}
          >
            {categories.map((c) => (
              <option key={c} value={c}>{c || '전체'}</option>
            ))}
          </select>
        </div>

        {/* 경매 목록 */}
        <ul
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
            gap: 20,
            listStyle: 'none',
            padding: 0,
          }}
        >
          {auctions.map((auction, index) => {
            const {
              id,
              startPrice,
              currentPrice,
              endTime,
              status,
              adminProduct,
            } = auction;

            const productName = adminProduct?.productName ?? '상품 없음';
            const imageUrl = adminProduct?.imageUrl ?? '/default-image.png';

            const isLast = index === auctions.length - 1;

            return (
              <li
                key={`${id}-${index}`} // 고유 키 보장
                ref={isLast ? lastAuctionRef : null}
                style={{
                  border: '1px solid #ccc',
                  borderRadius: 6,
                  padding: 10,
                  backgroundColor: '#fff',
                  boxShadow: '0 1px 3px rgb(0 0 0 / 0.1)',
                  display: 'flex',
                  flexDirection: 'column',
                  height: '100%',
                }}
              >
                <Link
                  href={`/auctions/${id}`}
                  style={{
                    textDecoration: 'none',
                    color: 'inherit',
                    display: 'flex',
                    flexDirection: 'column',
                    flexGrow: 1,
                  }}
                >
                  <div
                    style={{
                      width: '100%',
                      height: 140,
                      marginBottom: 8,
                      backgroundColor: '#eee',
                      display: 'flex',
                      justifyContent: 'center',
                      alignItems: 'center',
                      overflow: 'hidden',
                      borderRadius: 4,
                    }}
                  >
                    <img
                      src={imageUrl || '/images/placeholder.png'}
                      alt={productName || '이름 없는 상품'}
                      style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }}
                      onError={(e) => {
                        (e.target as HTMLImageElement).src = '/images/placeholder.png';
                      }}
                    />
                  </div>
                  <div style={{ flexGrow: 1 }}>
                    <strong>{productName || '이름 없는 상품'}</strong>
                    <p>시작가: {startPrice.toLocaleString()}원</p>
                    <p>현재가: {currentPrice.toLocaleString()}원</p>
                    <p>종료일: {new Date(endTime).toLocaleString()}</p>
                    <p>상태: {status}</p>
                  </div>
                </Link>
              </li>
            );
          })}
        </ul>

        {/* 로딩 상태 */}
        {loading && (
          <div style={{ textAlign: 'center', padding: 20 }}>
            <div>로딩중...</div>
          </div>
        )}

        {/* 더 이상 데이터가 없을 때 */}
        {!hasNext && auctions.length > 0 && (
          <div style={{ textAlign: 'center', padding: 20, color: '#666' }}>
            모든 경매를 확인했습니다. (총 {auctions.length}개)
          </div>
        )}

        {/* 데이터가 없을 때 */}
        {!loading && auctions.length === 0 && (
          <div style={{ textAlign: 'center', padding: 40, color: '#666' }}>
            경매가 없습니다.
          </div>
        )}

        {/* 디버깅 정보 */}
        {process.env.NODE_ENV === 'development' && (
          <div style={{ 
            position: 'fixed', 
            bottom: 10, 
            right: 10, 
            backgroundColor: 'rgba(0,0,0,0.8)', 
            color: 'white', 
            padding: 10, 
            borderRadius: 4,
            fontSize: 12,
            zIndex: 1000
          }}>
            <div>Page: {page}</div>
            <div>HasNext: {hasNext.toString()}</div>
            <div>Loading: {loading.toString()}</div>
            <div>Items: {auctions.length}</div>
            <div>Category: {category || 'All'}</div>
          </div>
        )}
      </div>
    </>
  );
}