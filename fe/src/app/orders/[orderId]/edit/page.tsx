"use client";

import { useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";

import { orderCreateStyles } from "@/app/style/orderCreate";
import { ordersEditStyles } from "@/app/style/ordersEdit";
import OrdersHeader from "@/app/_components/OrdersHeader";
import { apiGet, apiPut } from "@/lib/api";

const s = { ...orderCreateStyles, ...ordersEditStyles } as const;

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

type EditItem = {
  productId: number;
  productName: string;
  quantity: number;
  pricePerItem: number;
  subTotal: number;
};

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export default function OrderEditPage() {
  const router = useRouter();
  const params = useParams<{ orderId: string }>();
  const orderId = useMemo(() => Number(params.orderId), [params.orderId]);

  // 화면에 보여줄 “주문” 데이터(조립)
  const [email, setEmail] = useState("");
  const [items, setItems] = useState<EditItem[]>([]);
  const [totalAmount, setTotalAmount] = useState(0);

  // 수정 폼
  const [shippingAddress, setShippingAddress] = useState("");
  const [shippingCode, setShippingCode] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  // ✅ 에러 분리
  const [pageError, setPageError] = useState<string | null>(null); // 치명적(화면 못 그림)
  const [formError, setFormError] = useState<string | null>(null); // 폼 아래 인라인
  const [successOpen, setSuccessOpen] = useState(false);

  useEffect(() => {
    const run = async () => {
      try {
        setLoading(true);
        setPageError(null);
        setFormError(null);

        if (!Number.isFinite(orderId) || orderId <= 0) {
          setPageError("유효하지 않은 주문 ID입니다.");
          return;
        }

        const savedEmail = sessionStorage.getItem("orderEmail");
        if (!savedEmail) {
          setPageError("이메일 정보가 없습니다. 주문 조회에서 이메일을 입력해주세요.");
          return;
        }
        setEmail(savedEmail);

        // 1) summaries 로드
        const sumRes = await apiGet<Summary[]>(
          `${API_BASE}/api/orders/summary?email=${encodeURIComponent(savedEmail)}`
        );
        const summaries = sumRes.data ?? [];

        if (summaries.length === 0) {
          setPageError("주문 내역이 없습니다.");
          return;
        }

        // 2) 각 productId detail 로드 후 orderId 매칭
        const detailResults = await Promise.all(
          summaries.map(async (s) => {
            const dRes = await apiGet<Detail[]>(
              `${API_BASE}/api/orders/summary/${s.productId}?email=${encodeURIComponent(savedEmail)}`
            );
            return { productId: s.productId, details: dRes.data ?? [] };
          })
        );

        const productNameById = new Map<number, string>(
          summaries.map((x) => [x.productId, x.productName])
        );

        const matchedRows: { productId: number; d: Detail }[] = [];
        for (const r of detailResults) {
          for (const d of r.details) {
            if (Number(d.orderId) === orderId) matchedRows.push({ productId: r.productId, d });
          }
        }

        if (matchedRows.length === 0) {
          setPageError("해당 주문을 찾을 수 없습니다. 주문 내역에서 다시 진입해주세요.");
          return;
        }

        // 3) items 구성
        const nextItems: EditItem[] = matchedRows.map(({ productId, d }) => ({
          productId,
          productName: productNameById.get(productId) ?? `상품 #${productId}`,
          quantity: Number(d.quantity),
          pricePerItem: Number(d.pricePerItem),
          subTotal: Number(d.subTotal),
        }));

        setItems(nextItems);

        // 4) 배송 정보
        const first = matchedRows[0].d;
        setShippingAddress(first.shippingAddress ?? "");
        setShippingCode(first.shippingCode ?? "");

        // 5) 총 금액
        setTotalAmount(nextItems.reduce((acc, x) => acc + Number(x.subTotal), 0));
      } catch (e: any) {
        setPageError(e?.message ?? "주문 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    run();
  }, [orderId]);

  const validate = () => {
    const addr = shippingAddress.trim();
    const code = shippingCode.trim();

    if (!addr) return "배송지 주소를 입력해주세요.";
    if (!code) return "우편번호를 입력해주세요.";
    if (!/^\d{5}$/.test(code)) return "우편번호는 5자리 숫자여야 합니다.";
    return null;
  };

  const onSave = async () => {
    const err = validate();
    if (err) {
      setFormError(err); // ✅ 폼 아래로
      return;
    }

    setSaving(true);
    setFormError(null);

    try {
      await apiPut(`${API_BASE}/api/orders/${orderId}`, {
        shippingAddress: shippingAddress.trim(),
        shippingCode: shippingCode.trim(),
      });

      sessionStorage.setItem("orderNeedRefresh", "1");
      setSuccessOpen(true);
    } catch (e: any) {
      // ✅ 백엔드 msg가 그대로 내려오면 여기로 뜸
      setFormError(e?.message ?? "수정 실패");
    } finally {
      setSaving(false);
    }
  };

  const onCloseSuccess = () => {
    setSuccessOpen(false);
    router.push("/orders");
  };

  return (
    <div className={s.page}>
      <div className={s.container}>
        {/* ✅ 4Lines 공통 헤더 */}
        <OrdersHeader
          headerRowClassName={s.headerRow}
          btnClassName={s.btnSearch}
          rightLabel="주문 내역"
          onRightClick={() => router.push("/orders")}
        />

        {loading ? (
          <div className={s.card}>
            <div className={s.cardHeader}>불러오는 중</div>
            <div className={s.empty}>로딩중...</div>
          </div>
        ) : pageError ? (
          // ✅ 치명적 에러만 안내 화면
          <div className={s.card}>
            <div className={s.cardHeader}>안내</div>
            <div className="p-4 space-y-3">
              <div className={s.alertError}>{pageError}</div>
              <button className={s.btnSearch} onClick={() => router.push("/orders/search")}>
                주문 조회로 이동
              </button>
            </div>
          </div>
        ) : (
          <div className={s.gridEdit}>
            {/* 왼쪽 */}
            <section className={`${s.card} ${s.leftEdit} flex flex-col h-full`}>
              <div className={s.cardHeader}>주문 상품</div>

              {items.length === 0 ? (
                <div className={`${s.empty} flex-1`}>상품 정보가 없습니다.</div>
              ) : (
                <div className={`${s.list} flex-1`}>
                  {items.map((it) => (
                    <div key={it.productId} className={s.productRow}>
                      <div className={s.thumb} />
                      <div className={s.productInfo}>
                        <div className={s.productName}>{it.productName}</div>
                        <div className={s.productPrice}>{it.pricePerItem.toLocaleString()}원</div>
                      </div>
                      <div className="rounded-md bg-gray-600 px-3 py-1 text-xs font-semibold text-white">
                        {it.quantity}개
                      </div>
                    </div>
                  ))}
                </div>
              )}

              <div className={s.totalRow}>
                <div className={s.totalLabel}>총 금액</div>
                <div className={s.totalValue}>{totalAmount.toLocaleString()}원</div>
              </div>
            </section>

            {/* 오른쪽 */}
            <section className={`${s.card} ${s.rightEdit}`}>
              <div className={s.cardHeader}>배송 정보 수정</div>

              <div className={s.formBody}>
                <label className={s.label}>
                  이메일
                  <input className={s.readonlyInput} value={email} readOnly tabIndex={-1} />
                </label>

                <label className={s.label}>
                  주소 설정
                  <input
                    className={s.input}
                    value={shippingAddress}
                    onChange={(e) => setShippingAddress(e.target.value)}
                    placeholder="서울시 강남구 ..."
                    disabled={saving}
                  />
                </label>

                <label className={s.label}>
                  우편번호 설정
                  <input
                    className={s.input}
                    value={shippingCode}
                    onChange={(e) => setShippingCode(e.target.value)}
                    placeholder="12345"
                    disabled={saving}
                  />
                </label>

                {/* ✅ 여기! 인라인 에러 */}
                {formError && <div className={s.alertError}>{formError}</div>}

                <button className={s.btnPrimary} onClick={onSave} disabled={saving}>
                  {saving ? "수정 중..." : "수정하기"}
                </button>

                <button className={s.btnSecondary} onClick={() => router.back()} disabled={saving}>
                  취소
                </button>
              </div>
            </section>
          </div>
        )}
      </div>

      <SuccessModal
        open={successOpen}
        title="수정 완료"
        message="배송 정보가 수정되었습니다."
        onClose={onCloseSuccess}
      />
    </div>
  );
}

function SuccessModal({
  open,
  title,
  message,
  onClose,
}: {
  open: boolean;
  title: string;
  message: string;
  onClose: () => void;
}) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      role="dialog"
      aria-modal="true"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="absolute inset-0 bg-black/40" />
      <div className="relative z-10 w-[92%] max-w-sm rounded-xl border border-gray-200 bg-white p-5 shadow-lg">
        <div className="text-base font-bold text-gray-900">{title}</div>
        <div className="mt-2 text-sm text-gray-600">{message}</div>

        <div className="mt-4 flex justify-end gap-2">
          <button
            className="rounded-md border border-gray-900 px-4 py-2 text-sm font-semibold text-gray-900 hover:bg-gray-900 hover:text-white"
            onClick={onClose}
          >
            확인
          </button>
        </div>
      </div>
    </div>
  );
}
