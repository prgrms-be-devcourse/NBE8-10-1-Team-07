"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { orderCreateStyles as s } from "@/app/style/orderCreate";

type Product = {
  id: number;
  name: string;
  price: number;
};

type CartItem = {
  productId: number;
  name: string;
  price: number;
  quantity: number;
};

const DUMMY_PRODUCTS: Product[] = [
  { id: 1, name: "콜롬비아 어쩌구 커피멍", price: 5000 },
  { id: 2, name: "에티오피아 어쩌구 커피멍", price: 6000 },
  { id: 3, name: "케냐 어쩌구 커피멍", price: 5500 },
  { id: 4, name: "과테말라 어쩌구 커피멍", price: 5200 },
];

export default function OrderCreatePage() {
  const router = useRouter();

  const [cart, setCart] = useState<CartItem[]>([]);

  // 배송 정보 (API 연결 전 UI만)
  const [email, setEmail] = useState("");
  const [shippingAddress, setShippingAddress] = useState("");
  const [shippingCode, setShippingCode] = useState("");

  const totalAmount = useMemo(
    () => cart.reduce((sum, item) => sum + item.price * item.quantity, 0),
    [cart]
  );

  const addToCart = (p: Product) => {
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
    setCart((prev) =>
      prev.map((x) =>
        x.productId === productId ? { ...x, quantity: x.quantity + 1 } : x
      )
    );
  };

  const dec = (productId: number) => {
    setCart((prev) =>
      prev
        .map((x) =>
          x.productId === productId ? { ...x, quantity: x.quantity - 1 } : x
        )
        .filter((x) => x.quantity > 0)
    );
  };

  const remove = (productId: number) => {
    setCart((prev) => prev.filter((x) => x.productId !== productId));
  };

  const onCheckout = () => {
    const payload = {
      email,
      shippingAddress,
      shippingCode,
      items: cart.map((x) => ({
        productId: x.productId,
        quantity: x.quantity,
      })),
      totalAmount,
    };

    console.log("checkout payload:", payload);
    alert("임시 주문 생성 (콘솔 payload 확인)");
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
            <div className={s.list}>
              {DUMMY_PRODUCTS.map((p) => (
                <div key={p.id} className={s.productRow}>
                  <div className={s.thumb} />
                  <div className={s.productInfo}>
                    <div className={s.productName}>{p.name}</div>
                    <div className={s.productPrice}>
                      {p.price.toLocaleString()}원
                    </div>
                  </div>
                  <button className={s.btnAdd} onClick={() => addToCart(p)}>
                    추가
                  </button>
                </div>
              ))}
            </div>
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
                      >
                        -
                      </button>
                      <div className={s.qtyValue}>{item.quantity}</div>
                      <button
                        className={s.qtyBtn}
                        onClick={() => inc(item.productId)}
                      >
                        +
                      </button>
                    </div>

                    <button
                      className={s.btnRemove}
                      onClick={() => remove(item.productId)}
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
                />
              </label>

              <label className={s.label}>
                배송지 주소
                <input
                  className={s.input}
                  value={shippingAddress}
                  onChange={(e) => setShippingAddress(e.target.value)}
                  placeholder="서울시 강남구 ..."
                />
              </label>

              <label className={s.label}>
                우편번호
                <input
                  className={s.input}
                  value={shippingCode}
                  onChange={(e) => setShippingCode(e.target.value)}
                  placeholder="12345"
                />
              </label>
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
              disabled={cart.length === 0}
            >
              결제하기
            </button>
          </section>
        </div>
      </div>
    </div>
  );
}
