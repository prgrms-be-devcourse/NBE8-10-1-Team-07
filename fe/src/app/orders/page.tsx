"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { orderCreateStyles as s } from "@/app/style/orderCreate";

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

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function apiGet<T>(url: string) {
  const res = await fetch(url, { cache: "no-store" });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return (await res.json()) as RsData<T>;
}

export default function OrdersPage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [summaries, setSummaries] = useState<Summary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const run = async () => {
      try {
        setLoading(true);
        setError(null);

        const savedEmail = sessionStorage.getItem("orderEmail");
        if (!savedEmail) {
          setError("이메일 정보가 없습니다. 주문 조회에서 이메일을 입력해주세요.");
          return;
        }

        setEmail(savedEmail);

        const res = await apiGet<Summary[]>(
          `${API_BASE}/api/orders/summary?email=${encodeURIComponent(savedEmail)}`
        );

        setSummaries(res.data ?? []);
      } catch (e: any) {
        setError(e?.message ?? "알 수 없는 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    };

    run();
  }, []);

  const totals = useMemo(() => {
    const totalKinds = summaries.length;
    const totalQty = summaries.reduce(
      (acc, x) => acc + Number(x.totalQuantity || 0),
      0
    );
    const totalAmount = summaries.reduce(
      (acc, x) => acc + Number(x.totalAmount || 0),
      0
    );
    return { totalKinds, totalQty, totalAmount };
  }, [summaries]);

  if (loading) {
    return (
      <div className={s.page}>
        <div className={s.container}>
          <div className={s.card}>
            <div className={s.cardHeader}>주문 내역</div>
            <div className={s.empty}>로딩중...</div>
          </div>
        </div>
      </div>
    );
  }

  // ✅ 에러 상태
  if (error) {
    return (
      <div className={s.page}>
        <div className={s.container}>
          {/* ✅ 오른쪽 상단 버튼(이동) */}
          <div className={s.headerRow}>
            <div>
              <h1 className={s.title}>주문 내역</h1>
              {email && (
                <div className="mt-1 text-sm text-gray-500">{email}</div>
              )}
            </div>

            <button
              className={s.btnSearch}
              onClick={() => router.push("/orders/search")}
            >
              다른 이메일로 조회
            </button>
          </div>

          <div className={s.card}>
            <div className={s.cardHeader}>안내</div>
            <div className="p-4">
              <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {error}
              </div>

              <div className="mt-4">
                <button
                  className={s.btnSearch}
                  onClick={() => router.push("/orders/search")}
                >
                  주문 조회로 이동
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // ✅ 정상 화면
  return (
    <div className={s.page}>
      <div className={s.container}>
        {/* ✅ 헤더: 오른쪽 상단에 "다른 이메일로 조회" */}
        <div className={s.headerRow}>
          <div>
            <h1 className={s.title}>주문 내역</h1>
            {email && <div className="mt-1 text-sm text-gray-500">{email}</div>}
          </div>

          <button
            className={s.btnSearch}
            onClick={() => router.push("/orders/search")}
          >
            다른 이메일로 조회
          </button>
        </div>

        {/* ✅ 세로 스택 레이아웃 */}
        <div className="space-y-4">
          {/* 1) 주문 내역 카드(위) */}
          <section className={s.card}>
            <div className={s.cardHeader}>상품별 주문 요약</div>

            {summaries.length === 0 ? (
              <div className={s.empty}>주문 내역이 없습니다.</div>
            ) : (
              <div className={s.list}>
                {summaries.map((item) => (
                  <button
                    key={item.productId}
                    className={`${s.productRow} w-full text-left hover:bg-gray-50`}
                  >
                    <div className={s.thumb} />

                    <div className={s.productInfo}>
                      <div className={s.productName}>{item.productName}</div>

                      <div className="mt-1 text-xs text-gray-500">
                        총 수량 {Number(item.totalQuantity).toLocaleString()}개
                      </div>
                    </div>

                    <div className="text-sm font-bold text-gray-900">
                      {Number(item.totalAmount).toLocaleString()}원
                    </div>
                  </button>
                ))}
              </div>
            )}
          </section>

          {/* 2) Summary 카드(아래) */}
          <section className={s.card}>
            <div className={s.cardHeader}>Summary</div>

            <div className="p-4">
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
                <div className="rounded-lg border border-gray-200 p-3">
                  <div className="text-xs font-semibold text-gray-600">
                    상품 종류
                  </div>
                  <div className="mt-1 text-lg font-bold text-gray-900">
                    {totals.totalKinds.toLocaleString()}개
                  </div>
                </div>

                <div className="rounded-lg border border-gray-200 p-3">
                  <div className="text-xs font-semibold text-gray-600">
                    총 수량
                  </div>
                  <div className="mt-1 text-lg font-bold text-gray-900">
                    {totals.totalQty.toLocaleString()}개
                  </div>
                </div>

                <div className="rounded-lg border border-gray-200 p-3">
                  <div className="text-xs font-semibold text-gray-600">
                    총 결제 금액
                  </div>
                  <div className="mt-1 text-lg font-bold text-gray-900">
                    {totals.totalAmount.toLocaleString()}원
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
