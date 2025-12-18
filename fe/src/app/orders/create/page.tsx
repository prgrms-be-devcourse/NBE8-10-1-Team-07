"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { orderCreateStyles as s } from "@/app/style/orderCreate";

/** Spring RsData */
type RsData<T> = {
  resultCode: string;
  msg: string;
  data: T;
};

type Product = {
  id: number;
  name: string;
  price: number;
  description?: string;
};

type CartItem = {
  productId: number;
  name: string;
  price: number;
  quantity: number;
};

type OrderCreateRequest = {
  email: string;
  shippingAddress: string;
  shippingCode: string;
  items: { productId: number; quantity: number }[];
};

/** 주문 생성 응답(OrderDto 일부만 사용) */
type OrderDto = {
  id: number;
  email: string;
  shippingAddress: string;
  shippingCode: string;
  totalAmount: number;
};

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function apiGet<T>(url: string) {
  const res = await fetch(url, { cache: "no-store" });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return (await res.json()) as RsData<T>;
}

async function apiPost<T>(url: string, body: unknown) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    let text = "";
    try {
      text = await res.text();
    } catch {}
    throw new Error(`HTTP ${res.status}${text ? ` - ${text}` : ""}`);
  }

  return (await res.json()) as RsData<T>;
}

export default function OrderCreatePage() {
  const router = useRouter();

  // 상품 목록
  const [products, setProducts] = useState<Product[]>([]);
  const [productsLoading, setProductsLoading] = useState(true);
  const [productsError, setProductsError] = useState<string | null>(null);

  // 장바구니
  const [cart, setCart] = useState<CartItem[]>([]);

  // 배송 정보
  const [email, setEmail] = useState("");
  const [shippingAddress, setShippingAddress] = useState("");
  const [shippingCode, setShippingCode] = useState("");

  // 주문 생성 상태
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  // ✅ 결제 완료 "팝업(모달)" 상태
  const [isSuccessOpen, setIsSuccessOpen] = useState(false);
  const [successText, setSuccessText] = useState("");

  const totalAmount = useMemo(
    () => cart.reduce((sum, item) => sum + item.price * item.quantity, 0),
    [cart]
  );

  // ✅ 상품 목록 로딩: GET /api/products
  useEffect(() => {
    const run = async () => {
      try {
        setProductsLoading(true);
        setProductsError(null);

        const res = await apiGet<Product[]>(`${API_BASE}/api/products`);
        setProducts(res.data ?? []);
      } catch (e: any) {
        setProductsError(e?.message ?? "상품 목록 로딩 실패");
      } finally {
        setProductsLoading(false);
      }
    };

    run();
  }, []);

  const clearMessages = () => {
    setCreateError(null);
  };

  const addToCart = (p: Product) => {
    clearMessages();
    setCart((prev) => {
      const found = prev.find((x) => x.productId === p.id);
      if (!found) {
        return [
          ...prev,
          { productId: p.id, name: p.name, price: p.price, quantity: 1 },
        ];
      }
      return prev.map((x) =>
        x.productId === p.id ? { ...x, quantity: x.quantity + 1 } : x
      );
    });
  };

  const inc = (productId: number) => {
    clearMessages();
    setCart((prev) =>
      prev.map((x) =>
        x.productId === productId ? { ...x, quantity: x.quantity + 1 } : x
      )
    );
  };

  const dec = (productId: number) => {
    clearMessages();
    setCart((prev) =>
      prev
        .map((x) =>
          x.productId === productId ? { ...x, quantity: x.quantity - 1 } : x
        )
        .filter((x) => x.quantity > 0)
    );
  };

  const remove = (productId: number) => {
    clearMessages();
    setCart((prev) => prev.filter((x) => x.productId !== productId));
  };

  const validateBeforeCheckout = () => {
    const trimmedEmail = email.trim();
    const trimmedAddr = shippingAddress.trim();
    const trimmedCode = shippingCode.trim();

    if (!trimmedEmail) return "이메일을 입력해주세요.";
    if (!/^\S+@\S+\.\S+$/.test(trimmedEmail))
      return "이메일 형식이 올바르지 않습니다.";

    if (!trimmedAddr) return "배송지 주소를 입력해주세요.";
    if (!trimmedCode) return "우편번호를 입력해주세요.";
    if (!/^\d{5}$/.test(trimmedCode)) return "우편번호는 5자리 숫자여야 합니다.";

    if (cart.length === 0) return "상품을 1개 이상 담아주세요.";

    return null;
  };

  // ✅ 결제(주문 생성) 후: 페이지 이동 X, 모달 팝업만 표시
  const onCheckout = async () => {
    const err = validateBeforeCheckout();
    if (err) {
      setCreateError(err);
      return;
    }

    setCreating(true);
    setCreateError(null);

    const payload: OrderCreateRequest = {
      email: email.trim(),
      shippingAddress: shippingAddress.trim(),
      shippingCode: shippingCode.trim(),
      items: cart.map((x) => ({
        productId: x.productId,
        quantity: x.quantity,
      })),
      // ✅ totalAmount는 백엔드 DTO에 없으므로 보내지 않음
    };

    try {
      const res = await apiPost<OrderDto>(`${API_BASE}/api/orders`, payload);

      // ✅ alert 대신 "팝업(모달)"
      setSuccessText(res.msg || "결제가 완료되었습니다.");
      setIsSuccessOpen(true);

      // ✅ 결제 후 장바구니/주소 초기화 (원하는 UX)
      setCart([]);
      setShippingAddress("");
      setShippingCode("");
      // email은 유지(원하면 setEmail("")로 변경)
    } catch (e: any) {
      setCreateError(e?.message ?? "주문 생성 실패");
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className={s.page}>
      <div className={s.container}>
        {/* 🔹 상단 헤더 */}
        <div className={s.headerRow}>
          <h1 className={s.title}>주문 생성</h1>

          {/* ✅ 우측 상단 주문 조회 버튼 */}
          <button
            className={s.btnSearch}
            onClick={() => router.push("/orders/search")}
          >
            주문 조회
          </button>
        </div>

        <div className={s.grid}>
          {/* 왼쪽: 상품 목록 */}
          <section className={`${s.card} ${s.leftCol}`}>
            <div className={s.cardHeader}>상품 목록</div>

            {productsLoading ? (
              <div className={s.empty}>상품 불러오는 중...</div>
            ) : productsError ? (
              <div className={s.empty}>상품 로딩 실패: {productsError}</div>
            ) : products.length === 0 ? (
              <div className={s.empty}>등록된 상품이 없습니다.</div>
            ) : (
              <div className={s.list}>
                {products.map((p) => (
                  <div key={p.id} className={s.productRow}>
                    <div className={s.thumb} />
                    <div className={s.productInfo}>
                      <div className={s.productName}>{p.name}</div>
                      <div className={s.productPrice}>
                        {p.price.toLocaleString()}원
                      </div>
                    </div>
                    <button
                      className={s.btnAdd}
                      onClick={() => addToCart(p)}
                      disabled={creating}
                    >
                      추가
                    </button>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* 오른쪽: Summary */}
          <section className={`${s.card} ${s.rightCol} overflow-visible`}>
            <div className={s.cardHeader}>Summary</div>

            {cart.length === 0 ? (
              <div className={s.empty}>추가된 상품이 없습니다.</div>
            ) : (
              <div className={s.summaryList}>
                {cart.map((item) => (
                  <div key={item.productId} className={s.summaryRow}>
                    <div className={s.summaryLeft}>
                      <div className={s.nameWrap}>
                        <div className={s.summaryName}>{item.name}</div>
                        <div className={s.tooltip}>{item.name}</div>
                      </div>
                      <div className={s.summarySub}>
                        {item.price.toLocaleString()}원
                      </div>
                    </div>

                    <div className={s.qtyBox}>
                      <button
                        className={s.qtyBtn}
                        onClick={() => dec(item.productId)}
                        disabled={creating}
                      >
                        -
                      </button>
                      <div className={s.qtyValue}>{item.quantity}</div>
                      <button
                        className={s.qtyBtn}
                        onClick={() => inc(item.productId)}
                        disabled={creating}
                      >
                        +
                      </button>
                    </div>

                    <button
                      className={s.btnRemove}
                      onClick={() => remove(item.productId)}
                      disabled={creating}
                    >
                      삭제
                    </button>
                  </div>
                ))}
              </div>
            )}

            {/* 배송 정보 입력 */}
            <div className={s.formBox}>
              <label className={s.label}>
                이메일
                <input
                  className={s.input}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="test@test.com"
                  disabled={creating}
                />
              </label>

              <label className={s.label}>
                배송지 주소
                <input
                  className={s.input}
                  value={shippingAddress}
                  onChange={(e) => setShippingAddress(e.target.value)}
                  placeholder="서울시 강남구 ..."
                  disabled={creating}
                />
              </label>

              <label className={s.label}>
                우편번호
                <input
                  className={s.input}
                  value={shippingCode}
                  onChange={(e) => setShippingCode(e.target.value)}
                  placeholder="12345"
                  disabled={creating}
                />
              </label>

              {createError && (
                <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                  {createError}
                </div>
              )}
            </div>

            <div className={s.totalRow}>
              <div className={s.totalLabel}>총 금액</div>
              <div className={s.totalValue}>
                {totalAmount.toLocaleString()}원
              </div>
            </div>

            <button
              className={s.btnCheckout}
              onClick={onCheckout}
              disabled={cart.length === 0 || creating}
            >
              {creating ? "결제 처리 중..." : "결제하기"}
            </button>
          </section>
        </div>
      </div>

      {/* ✅ 결제 완료 팝업(모달) */}
      <SuccessModal
        open={isSuccessOpen}
        title="결제 완료"
        message={successText || "결제가 완료되었습니다."}
        onClose={() => setIsSuccessOpen(false)}
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
        // 배경 클릭 닫기
        if (e.target === e.currentTarget) onClose();
      }}
    >
      {/* dim */}
      <div className="absolute inset-0 bg-black/40" />

      {/* modal */}
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
