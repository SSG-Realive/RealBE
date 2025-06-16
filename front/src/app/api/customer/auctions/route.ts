import { NextRequest, NextResponse } from "next/server";

export async function GET(request: NextRequest) {
  const backendUrl = `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/customer/auctions`;

  try {
    const res = await fetch(backendUrl, {
      method: "GET",
      headers: {
        Cookie: request.headers.get("cookie") || "",
      },
      credentials: "include",
    });

    const data = await res.json();
    return NextResponse.json(data, { status: res.status });
  } catch (error) {
    return NextResponse.json(
      { success: false, message: "경매 목록 조회 중 오류 발생" },
      { status: 500 }
    );
  }
}
