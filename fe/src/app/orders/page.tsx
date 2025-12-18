"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";

import { orderCreateStyles } from "@/app/style/orderCreate";
import { ordersStyles } from "@/app/style/orders";

// ✅ 공용 + orders 전용 합쳐서 s 하나로 사용
const s = { ...orderCreateStyles, ...ordersStyles } as const;

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

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function apiGet<T>(url: string) {
  const res = await fetch(url, { cache: "no-store" });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return (await res.json()) as RsData<T>;
}

async function apiDelete<T>(url: string) {
  const res = await fetch(url, { method: "DELETE" });
  if (!res.ok) {
    let text = "";
    try {
      text = await res.text();
    } catch {}
    throw new Error(`HTTP ${res.status}${text ? ` - ${text}` : ""}`);
  }
  return (await res.json()) as RsData<T>;
}

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

export default function OrdersPage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [summaries, setSummaries] = useState<Summary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // ✅ 아코디언 상태
  const [openProductId, setOpenProductId] = useState<number | null>(null);

  // ✅ 상품별 상세 캐시/상태
  const [detailsByProductId, setDetailsByProductId] = useState<
    Record<number, Detail[]>
  >({});
  const [detailLoadingByProductId, setDetailLoadingByProductId] = useState<
    Record<number, boolean>
  >({});
  const [detailErrorByProductId, setDetailErrorByProductId] = useState<
    Record<number, string | null>
  >({});

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

  const refreshSummaries = async (emailValue: string) => {
    const res = await apiGet<Summary[]>(
      `${API_BASE}/api/orders/summary?email=${encodeURIComponent(emailValue)}`
    );
    setSummaries(res.data ?? []);
  };

  const toggleDetail = async (productId: number) => {
    if (openProductId === productId) {
      setOpenProductId(null);
      return;
    }

    // email이 아직 state에 없으면 상세 호출 막기
    if (!email) return;

    setOpenProductId(productId);

    // 캐시 있으면 재호출 안 함
    if (detailsByProductId[productId]) return;

    try {
      setDetailLoadingByProductId((prev) => ({ ...prev, [productId]: true }));
      setDetailErrorByProductId((prev) => ({ ...prev, [productId]: null }));

      const res = await apiGet<Detail[]>(
        `${API_BASE}/api/orders/summary/${productId}?email=${encodeURIComponent(
          email
        )}`
      );

      setDetailsByProductId((prev) => ({
        ...prev,
        [productId]: res.data ?? [],
      }));
    } catch (e: any) {
      setDetailErrorByProductId((prev) => ({
        ...prev,
        [productId]: e?.message ?? "상세 조회 실패",
      }));
    } finally {
      setDetailLoadingByProductId((prev) => ({ ...prev, [productId]: false }));
    }
  };

  const onDeleteOrder = async (productId: number, orderId: number) => {
    if (!confirm(`${orderId}번 주문을 삭제할까요?`)) return;

    try {
      await apiDelete<void>(`${API_BASE}/api/orders/${orderId}`);

      // ✅ 1) 상세 캐시에서 즉시 제거
      setDetailsByProductId((prev) => ({
        ...prev,
        [productId]: (prev[productId] ?? []).filter((d) => d.orderId !== orderId),
      }));

      // ✅ 2) 요약 재조회로 총수량/총금액 반영
      if (email) {
        await refreshSummaries(email);
      }
    } catch (e: any) {
      alert(e?.message ?? "삭제 실패");
    }
  };

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
          <div className={s.headerRow}>
            <div>
              <h1 className={s.title}>주문 내역</h1>
              {email && <div className={s.emailText}>{email}</div>}
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
            <div className={s.cardBody}>
              <div className={s.alertError}>{error}</div>

              <div className={s.mt4}>
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

  return (
    <div className={s.page}>
      <div className={s.container}>
        <div className={s.headerRow}>
          <div>
            <h1 className={s.title}>주문 내역</h1>
            {email && <div className={s.emailText}>{email}</div>}
          </div>

          <button
            className={s.btnSearch}
            onClick={() => router.push("/orders/search")}
          >
            다른 이메일로 조회
          </button>
        </div>

        <div className={s.stack4}>
          <section className={s.card}>
            <div className={s.cardHeader}>상품별 주문 요약</div>

            {summaries.length === 0 ? (
              <div className={s.empty}>주문 내역이 없습니다.</div>
            ) : (
              <div className={s.list}>
                {summaries.map((item) => {
                  const isOpen = openProductId === item.productId;
                  const isDetailLoading =
                    !!detailLoadingByProductId[item.productId];
                  const detailError = detailErrorByProductId[item.productId];
                  const details = detailsByProductId[item.productId] ?? [];

                  return (
                    <div key={item.productId} className={s.stack2}>
                      <button
                        className={`${s.productRow} ${s.productRowButton}`}
                        onClick={() => toggleDetail(item.productId)}
                      >
                        <div className={s.thumb} />

                        <div className={s.productInfo}>
                          <div className={s.productName}>{item.productName}</div>
                          <div className={s.productSub}>
                            총 수량{" "}
                            {Number(item.totalQuantity).toLocaleString()}개
                          </div>
                        </div>

                        <div className={s.productRight}>
                          <div className={s.productAmount}>
                            {Number(item.totalAmount).toLocaleString()}원
                          </div>
                          <span className={s.chev}>{isOpen ? "▲" : "▼"}</span>
                        </div>
                      </button>

                      {isOpen && (
                        <div className={s.detailWrap}>
                          <div className={s.detailHeader}>
                            결제/주문 상세
                            {isDetailLoading && (
                              <span className={s.detailLoading}>
                                불러오는 중...
                              </span>
                            )}
                          </div>

                          {detailError ? (
                            <div className={s.cardBody}>
                              <div className={s.alertError}>{detailError}</div>
                            </div>
                          ) : details.length === 0 && !isDetailLoading ? (
                            <div className={s.detailEmpty}>
                              상세 내역이 없습니다.
                            </div>
                          ) : (
                            <div className={s.detailScroll}>
                              <div className={s.detailMinWidth}>
                                <div className={s.tableHead}>
                                  <div>주문 ID</div>
                                  <div>주문 시간</div>
                                  <div>상태</div>
                                  <div>배송지</div>
                                  <div className={s.textRight}>수량</div>
                                  <div className={s.textRight}>단가</div>
                                  <div className={s.textRight}>소계</div>
                                  <div className={s.textRight}>관리</div>
                                </div>

                                {details.map((d) => (
                                  <div
                                    key={`${d.orderId}-${d.orderTime}`}
                                    className={s.tableRow}
                                  >
                                    <div className={s.orderId}>#{d.orderId}</div>

                                    <div>{formatDateTime(d.orderTime)}</div>

                                    <div>
                                      <span className={s.statusBadge}>
                                        {ORDER_STATUS_LABEL[d.orderStatus]}
                                      </span>
                                    </div>

                                    <div className={s.addrCell}>
                                      <div className={s.addrText}>
                                        {d.shippingAddress}
                                      </div>
                                      <div className={s.addrSub}>
                                        {d.shippingCode}
                                      </div>
                                    </div>

                                    <div className={s.textRight}>
                                      {Number(d.quantity).toLocaleString()}
                                    </div>

                                    <div className={s.textRight}>
                                      {Number(d.pricePerItem).toLocaleString()}원
                                    </div>

                                    <div className={s.subTotal}>
                                      {Number(d.subTotal).toLocaleString()}원
                                    </div>

                                    {/* ✅ 오른쪽 끝 관리 버튼 */}
                                    <div className={s.actionsCell}>
                                      <button
                                        className={s.btnEdit}
                                        onClick={() =>
                                          router.push(`/orders/${d.orderId}/edit`)
                                        }
                                      >
                                        수정
                                      </button>

                                      <button
                                        className={s.btnDelete}
                                        onClick={() =>
                                          onDeleteOrder(item.productId, d.orderId)
                                        }
                                      >
                                        삭제
                                      </button>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          <section className={s.card}>
            <div className={s.cardHeader}>Summary</div>

            <div className={s.cardBody}>
              <div className={s.summaryGrid}>
                <div className={s.summaryCard}>
                  <div className={s.summaryLabel}>상품 종류</div>
                  <div className={s.summaryValue}>
                    {totals.totalKinds.toLocaleString()}개
                  </div>
                </div>

                <div className={s.summaryCard}>
                  <div className={s.summaryLabel}>총 수량</div>
                  <div className={s.summaryValue}>
                    {totals.totalQty.toLocaleString()}개
                  </div>
                </div>

                <div className={s.summaryCard}>
                  <div className={s.summaryLabel}>총 결제 금액</div>
                  <div className={s.summaryValue}>
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
