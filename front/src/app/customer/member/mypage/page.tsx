'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import Navbar from '@/components/customer/Navbar';
import Wishlist from './sections/Wishlist';
import OrderStatus from './sections/OrderStatus';
import Profile from './sections/Profile';
import Review from './sections/Review';

export default function MyPage() {
    const router = useRouter();
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    const [mounted, setMounted] = useState(false);

    useEffect(() => setMounted(true), []);

    useEffect(() => {
        if (mounted && !isAuthenticated()) {
            router.push('/customer/member/login?redirectTo=/member/mypage');
        }
    }, [mounted, isAuthenticated, router]);

    if (!mounted || !isAuthenticated()) return null;

    return (
        <>
            <Navbar />
            <main className="max-w-6xl mx-auto p-6 grid gap-6 grid-cols-2">
                <Wishlist />
                <OrderStatus />
                <Profile />
                <Review />
            </main>
        </>
    );
}
