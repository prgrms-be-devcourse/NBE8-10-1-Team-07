"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

type Summary = {
  productId: number;
  productName: string;
  totalQuantity: number;
  totalAmount: number;
};

type RsData<T> = {
  resultCode: string;
  msg: string;
  data: T;
};

// ✅ 백엔드 주소 (환경변수 있으면 그걸 사용)
const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export default function OrdersPage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [summaries, setSummaries] = useState<Summary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const run = async () => {
      try {
        const savedEmail = sessionStorage.getItem("orderEmail");

        // ✅ 이메일이 없으면 search로 보내기
        if (!savedEmail) {
          setError("이메일 정보가 없습니다. 이메일 확인 페이지로 이동해주세요.");
          return;
        }

        setEmail(savedEmail);

        const res = await fetch(
          `${API_BASE}/api/orders/summary?email=${encodeURIComponent(savedEmail)}`,
          { cache: "no-store" }
        );

        if (!res.ok) throw new Error(`요약 조회 실패 (HTTP ${res.status})`);

        const json: RsData<Summary[]> = await res.json();
        setSummaries(json.data ?? []);
      } catch (e: any) {
        setError(e?.message ?? "알 수 없는 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    };

    run();
  }, []);

  if (loading) {
    return <div className="min-h-screen bg-gray-100 p-6">로딩중...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="mx-auto max-w-3xl rounded-xl bg-white p-6 shadow-sm">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-gray-900">주문 내역</h1>
            {email && <div className="mt-1 text-sm text-gray-500">{email}</div>}
          </div>

          <button
            className="rounded-md border border-gray-900 px-4 py-2 text-sm font-semibold text-gray-900 hover:bg-gray-900 hover:text-white"
            onClick={() => router.push("/orders/search")}
          >
            이메일 다시 입력
          </button>
        </div>

        {error ? (
          <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
            <div className="mt-3">
              <button
                className="rounded-md bg-gray-900 px-4 py-2 text-sm font-semibold text-white hover:bg-black"
                onClick={() => router.push("/orders/search")}
              >
                이메일 확인하러 가기
              </button>
            </div>
          </div>
        ) : summaries.length === 0 ? (
          <div className="text-sm text-gray-500">주문 내역이 없습니다.</div>
        ) : (
          <ul className="divide-y">
            {summaries.map((s) => (
              <li
                key={s.productId}
                className="flex items-center justify-between py-3"
              >
                <div className="min-w-0">
                  <div className="truncate font-semibold text-gray-900">
                    {s.productName}
                  </div>
                  <div className="text-xs text-gray-500">
                    총 수량 {s.totalQuantity}개
                  </div>
                </div>
                <div className="font-bold text-gray-900">
                  {Number(s.totalAmount).toLocaleString()}원
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
