'use client';

import { useEffect, useState } from 'react';
import { getCustomerProfile } from '@/service/customer/customerService';

interface CustomerProfile {
    name: string;
    email: string;
    phone: string;
}

export default function Profile() {
    const [profile, setProfile] = useState<CustomerProfile | null>(null);

    useEffect(() => {
        getCustomerProfile().then(setProfile).catch(console.error);
    }, []);

    if (!profile) return <div>Loading...</div>;

    return (
        <div className="border p-4 rounded shadow bg-white">
            <h2 className="text-lg font-semibold mb-2">👤 회원 정보</h2>
            <ul className="text-sm text-gray-700">
                <li>이름: {profile.name}</li>
                <li>이메일: {profile.email}</li>
                <li>전화번호: {profile.phone}</li>
            </ul>
        </div>
    );
}
