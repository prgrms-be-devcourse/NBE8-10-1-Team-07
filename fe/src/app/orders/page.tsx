"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";

import { orderCreateStyles } from "@/app/style/orderCreate";
import { ordersStyles } from "@/app/style/orders";

// 공용 + orders 전용 스타일 병합
const s = { ...orderCreateStyles, ...ordersStyles } as const;

/* =======================
   타입 정의
======================= */
type Summary = {
  productId: number;
  productName: string;
  totalQuantity: number;
  totalAmount: number;
};

type OrderStatus =
  | "ORDERED"
  | "PAID"
  | "PREPARING"
  | "SHIPPING"
  | "DELIVERED"
  | "CANCELED";

type Detail = {
  orderId: number;
  orderTime: string;
  orderStatus: OrderStatus;
  shippingAddress: string;
  shippingCode: string;
  quantity: number;
  pricePerItem: number;
  subTotal: number;
};

type RsData<T> = {
  resultCode: string;
  msg: string;
  data: T;
};

/* =======================
   API
======================= */
const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function apiGet<T>(url: string) {
  const res = await fetch(url, { cache: "no-store" });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return (await res.json()) as RsData<T>;
}

async function apiDelete<T>(url: string) {
  const res = await fetch(url, { method: "DELETE" });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return (await res.json()) as RsData<T>;
}

/* =======================
   유틸
======================= */
function formatDateTime(iso: string) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const hh = String(d.getHours()).padStart(2, "0");
  const mi = String(d.getMinutes()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`;
}

const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  ORDERED: "주문 접수",
  PAID: "결제 완료",
  PREPARING: "상품 준비 중",
  SHIPPING: "배송 중",
  DELIVERED: "배송 완료",
  CANCELED: "주문 취소",
};

/* =======================
   Page
======================= */
export default function OrdersPage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [summaries, setSummaries] = useState<Summary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [openProductId, setOpenProductId] = useState<number | null>(null);

  const [detailsByProductId, setDetailsByProductId] =
    useState<Record<number, Detail[]>>({});

  const [detailLoadingByProductId, setDetailLoadingByProductId] =
    useState<Record<number, boolean>>({});

  const [detailErrorByProductId, setDetailErrorByProductId] =
    useState<Record<number, string | null>>({});

  /* =======================
     요약 재조회 함수
  ======================= */
  const fetchSummaries = async (targetEmail: string) => {
    const res = await apiGet<Summary[]>(
      `${API_BASE}/api/orders/summary?email=${encodeURIComponent(targetEmail)}`
    );
    setSummaries(res.data ?? []);
  };

  /* =======================
     상세 조회 함수
  ======================= */
  const fetchDetails = async (productId: number, targetEmail: string) => {
    setDetailLoadingByProductId((p) => ({ ...p, [productId]: true }));
    setDetailErrorByProductId((p) => ({ ...p, [productId]: null }));

    try {
      const res = await apiGet<Detail[]>(
        `${API_BASE}/api/orders/summary/${productId}?email=${encodeURIComponent(
          targetEmail
        )}`
      );
      setDetailsByProductId((p) => ({ ...p, [productId]: res.data ?? [] }));
    } catch (e: any) {
      setDetailErrorByProductId((p) => ({
        ...p,
        [productId]: e?.message ?? "상세 조회 실패",
      }));
    } finally {
      setDetailLoadingByProductId((p) => ({ ...p, [productId]: false }));
    }
  };

  /* =======================
     최초 로딩 + 수정 후 리프레시 처리
  ======================= */
  useEffect(() => {
    const run = async () => {
      try {
        const savedEmail = sessionStorage.getItem("orderEmail");
        if (!savedEmail) {
          setError("이메일 정보가 없습니다. 주문 조회에서 이메일을 입력해주세요.");
          return;
        }

        setEmail(savedEmail);
        await fetchSummaries(savedEmail);
      } catch (e: any) {
        setError(e?.message ?? "알 수 없는 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    };

    run();
  }, []);

  // ✅ edit에서 저장 후 돌아왔을 때: 요약/하단Summary까지 최신 보장
  useEffect(() => {
    const refreshIfNeeded = async () => {
      const flag = sessionStorage.getItem("orderNeedRefresh");
      if (flag !== "1") return;

      sessionStorage.removeItem("orderNeedRefresh");

      // 상세 캐시가 있으면 옛 주소가 남을 수 있어서 캐시 비우고 다시 보게
      setDetailsByProductId({});
      setDetailErrorByProductId({});
      setDetailLoadingByProductId({});
      setOpenProductId(null);

      if (!email) return;
      try {
        await fetchSummaries(email);
      } catch (e: any) {
        // 여기서 에러는 치명적이지 않으니 필요하면 화면에만 표시
        setError(e?.message ?? "새로고침 실패");
      }
    };

    // 페이지가 다시 포커스될 때도 체크 (edit -> orders 돌아오는 케이스)
    const onFocus = () => {
      refreshIfNeeded();
    };

    window.addEventListener("focus", onFocus);
    // 최초 마운트 시에도 한번 체크
    refreshIfNeeded();

    return () => window.removeEventListener("focus", onFocus);
  }, [email]);

  /* =======================
     Summary 계산
  ======================= */
  const totals = useMemo(() => {
    return {
      totalKinds: summaries.length,
      totalQty: summaries.reduce((acc, x) => acc + Number(x.totalQuantity), 0),
      totalAmount: summaries.reduce((acc, x) => acc + Number(x.totalAmount), 0),
    };
  }, [summaries]);

  /* =======================
     상세 토글
  ======================= */
  const toggleDetail = async (productId: number) => {
    if (openProductId === productId) {
      setOpenProductId(null);
      return;
    }

    setOpenProductId(productId);

    // 캐시 있으면 재조회 생략 (단, 수정 후에는 캐시를 통째로 비우기 때문에 최신 보장됨)
    if (detailsByProductId[productId]) return;

    await fetchDetails(productId, email);
  };

  /* =======================
     삭제 (상세 + 요약 + Summary까지 갱신)
  ======================= */
  const onDeleteOrder = async (productId: number, orderId: number) => {
    if (!confirm(`${orderId}번 주문을 삭제할까요?`)) return;

    try {
      await apiDelete(`${API_BASE}/api/orders/${orderId}`);

      // 1) 현재 열려있는 상세 UI에서 즉시 row 제거 (UX 빠르게)
      setDetailsByProductId((prev) => {
        const next = { ...prev };
        const filtered = (next[productId] ?? []).filter((d) => d.orderId !== orderId);
        next[productId] = filtered;
        return next;
      });

      // 2) 요약(summaries) 재조회 → 상단 요약/하단 Summary 값까지 정확히 맞춤
      await fetchSummaries(email);

      // 3) (선택) 해당 상품 상세가 완전히 비면 접고 캐시 정리
      setDetailsByProductId((prev) => {
        const list = prev[productId] ?? [];
        if (list.length > 0) return prev;

        const next = { ...prev };
        delete next[productId];
        return next;
      });

      if ((detailsByProductId[productId] ?? []).length <= 1) {
        // 방금 삭제로 마지막 1개가 없어졌을 가능성
        setOpenProductId((cur) => (cur === productId ? null : cur));
      }
    } catch (e: any) {
      alert(e?.message ?? "삭제 실패");
    }
  };

  /* =======================
     UI
  ======================= */
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

  if (error) {
    return (
      <div className={s.page}>
        <div className={s.container}>
          <div className={s.card}>
            <div className={s.cardHeader}>안내</div>
            <div className={s.cardBody}>
              <div className={s.alertError}>{error}</div>
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
    );
  }

  return (
    <div className={s.page}>
      <div className={s.container}>
        <div className={s.headerRow}>
          <div>
            <h1 className={s.title}>주문 내역</h1>
            <div className={s.emailText}>{email}</div>
          </div>

          <button
            className={s.btnSearch}
            onClick={() => router.push("/orders/search")}
          >
            다른 이메일로 조회
          </button>
        </div>

        <section className={s.card}>
          <div className={s.cardHeader}>상품별 주문 요약</div>

          <div className={s.list}>
            {summaries.map((item) => {
              const details = detailsByProductId[item.productId] ?? [];
              const isOpen = openProductId === item.productId;
              const isDetailLoading = !!detailLoadingByProductId[item.productId];
              const detailErr = detailErrorByProductId[item.productId];

              return (
                <div key={item.productId} className={s.stack2}>
                  <button
                    className={`${s.productRow} ${s.productRowButton}`}
                    onClick={() => toggleDetail(item.productId)}
                  >
                    <div className={s.productInfo}>
                      <div className={s.productName}>{item.productName}</div>
                      <div className={s.productSub}>
                        총 수량 {item.totalQuantity}개
                      </div>
                    </div>

                    <div className={s.productAmount}>
                      {Number(item.totalAmount).toLocaleString()}원
                    </div>
                  </button>

                  {isOpen && (
                    <div className={s.detailWrap}>
                      <div className={s.tableHead}>
                        <div>주문 ID</div>
                        <div>주문 시간</div>
                        <div>상태</div>
                        <div>배송지</div>
                        <div>수량</div>
                        <div>단가</div>
                        <div>소계</div>
                        <div>관리</div>
                      </div>

                      {isDetailLoading && (
                        <div className={s.detailEmpty}>상세 불러오는 중...</div>
                      )}

                      {detailErr && (
                        <div className={s.detailEmpty}>
                          <div className={s.alertError}>{detailErr}</div>
                        </div>
                      )}

                      {!isDetailLoading && !detailErr && details.length === 0 && (
                        <div className={s.detailEmpty}>상세 내역이 없습니다.</div>
                      )}

                      {!isDetailLoading &&
                        !detailErr &&
                        details.map((d) => (
                          <div
                            key={`${d.orderId}-${d.orderTime}`}
                            className={s.tableRow}
                          >
                            <div>#{d.orderId}</div>
                            <div>{formatDateTime(d.orderTime)}</div>

                            <div>
                              <span className={s.statusBadge}>
                                {ORDER_STATUS_LABEL[d.orderStatus]}
                              </span>
                            </div>

                            <div className={s.addrCell}>
                              <div className={s.addrText}>{d.shippingAddress}</div>
                              <div className={s.addrSub}>{d.shippingCode}</div>
                            </div>

                            <div>{d.quantity}</div>
                            <div>{Number(d.pricePerItem).toLocaleString()}원</div>
                            <div className={s.subTotalLeft}>
                              {Number(d.subTotal).toLocaleString()}원
                            </div>

                            <div className={s.actionsCell}>
                              <button
                                className={s.btnEdit}
                                onClick={() => router.push(`/orders/${d.orderId}/edit`)}
                              >
                                수정
                              </button>

                              <button
                                className={s.btnDelete}
                                onClick={() => onDeleteOrder(item.productId, d.orderId)}
                              >
                                삭제
                              </button>
                            </div>
                          </div>
                        ))}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </section>

        <section className={s.card}>
          <div className={s.cardHeader}>Summary</div>
          <div className={s.cardBody}>
            <div className={s.summaryGrid}>
              <div className={s.summaryCard}>
                <div className={s.summaryLabel}>총 상품 종류</div>
                <div className={s.summaryValue}>{totals.totalKinds}개</div>
              </div>

              <div className={s.summaryCard}>
                <div className={s.summaryLabel}>총 상품 개수</div>
                <div className={s.summaryValue}>{totals.totalQty}개</div>
              </div>

              <div className={s.summaryCard}>
                <div className={s.summaryLabel}>총 주문 금액</div>
                <div className={s.summaryValue}>
                  {totals.totalAmount.toLocaleString()}원
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
