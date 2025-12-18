"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ordersSearchStyles as s } from "@/app/style/ordersSearch";

/**
 * 백엔드 공통 응답 타입 (Spring RsData)
 */
type RsData<T> = {
  resultCode: string;
  msg: string;
  data: T;
};

/**
 * 백엔드 API Base URL
 * (환경변수 없으면 로컬 8080 사용)
 */
const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

/**
 * GET 요청 헬퍼
 */
async function apiGet<T>(url: string) {
  const res = await fetch(url, { cache: "no-store" });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }
  return (await res.json()) as RsData<T>;
}

export default function OrdersSearchPage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const onSubmit = async () => {
    const trimmed = email.trim();

    if (!trimmed) {
      setErrorMsg("이메일을 입력해주세요.");
      return;
    }

    setLoading(true);
    setErrorMsg(null);

    try {
      // ✅ 실제 백엔드(8080)로 이메일 존재 여부 확인
      const res = await apiGet<{ exists: boolean }>(
        `${API_BASE}/api/customers/exists?email=${encodeURIComponent(trimmed)}`
      );

      if (!res.data?.exists) {
        setErrorMsg("해당 이메일의 고객이 없습니다.");
        return;
      }

      // ✅ 주문 조회용 이메일 저장 (URL 노출 X)
      sessionStorage.setItem("orderEmail", trimmed);

      // ✅ 주문 내역 페이지로 이동
      router.push("/orders");
    } catch (e: any) {
      setErrorMsg(`조회 실패: ${e?.message ?? "unknown error"}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={s.page}>
      <div className={s.container}>
        <div className={s.content}>
          <h1 className={s.title}>이메일 확인</h1>
          <p className={s.desc}>주문 내역을 확인할 이메일을 입력해주세요.</p>

          <div className={s.box}>
            <label className={s.label}>이메일</label>

            <div className={s.row}>
              <input
                className={s.input}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="test@test.com"
                disabled={loading}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    onSubmit();
                  }
                }}
              />

              <button
                className={s.btn}
                onClick={onSubmit}
                disabled={loading}
              >
                {loading ? "확인중..." : "확인"}
              </button>
            </div>

            {errorMsg && <div className={s.error}>{errorMsg}</div>}
          </div>
        </div>
      </div>
    </div>
  );
}
