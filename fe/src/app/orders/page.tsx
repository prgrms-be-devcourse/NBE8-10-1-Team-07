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
       초기 로딩
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

    /* =======================
       Summary 계산
    ======================= */
    const totals = useMemo(() => {
        return {
            totalKinds: summaries.length,
            totalQty: summaries.reduce(
                (acc, x) => acc + Number(x.totalQuantity),
                0
            ),
            totalAmount: summaries.reduce(
                (acc, x) => acc + Number(x.totalAmount),
                0
            ),
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

        if (detailsByProductId[productId]) return;

        try {
            setDetailLoadingByProductId((p) => ({ ...p, [productId]: true }));

            const res = await apiGet<Detail[]>(
                `${API_BASE}/api/orders/summary/${productId}?email=${encodeURIComponent(
                    email
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
       삭제
    ======================= */
    const onDeleteOrder = async (productId: number, orderId: number) => {
        if (!confirm(`${orderId}번 주문을 삭제할까요?`)) return;

        await apiDelete(`${API_BASE}/api/orders/${orderId}`);

        setDetailsByProductId((prev) => ({
            ...prev,
            [productId]: (prev[productId] ?? []).filter(
                (d) => d.orderId !== orderId
            ),
        }));
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

    /* =======================
       정상 화면
    ======================= */
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
                                            {item.totalAmount.toLocaleString()}원
                                        </div>
                                    </button>

                                    {openProductId === item.productId && (
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

                                            {details.map((d) => (
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
                                                    <div>
                                                        {d.shippingAddress}
                                                        <div className={s.addrSub}>{d.shippingCode}</div>
                                                    </div>
                                                    <div>{d.quantity}</div>
                                                    <div>{d.pricePerItem}원</div>
                                                    <div>{d.subTotal}원</div>

                                                    <div className={s.actionsCell}>
                                                        <button
                                                            className={s.btnEdit}
                                                            onClick={() => router.push(`/orders/${d.orderId}/edit`)}
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
