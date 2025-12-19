"use client";

import OrdersHeader from "@/app/_components/OrdersHeader";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { ordersSearchStyles as s } from "@/app/style/ordersSearch";

type RsData<T> = {
  resultCode: string;
  msg: string;
  data: T;
};

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

/** ✅ 해결3: 에러 메시지 추출 */
function pickErrorMessage(res: Response, rawText: string): string {
  try {
    const j = JSON.parse(rawText);

    if (j && typeof j === "object" && typeof j.msg === "string" && j.msg.trim()) {
      return j.msg;
    }

    if (j && typeof j === "object") {
      if (typeof j.message === "string" && j.message.trim()) return j.message;
      if (typeof j.error === "string" && j.error.trim()) return j.error;
    }
  } catch { }

  const trimmed = (rawText ?? "").trim();
  if (trimmed) return trimmed.length > 180 ? trimmed.slice(0, 180) + "..." : trimmed;
  return `HTTP ${res.status}`;
}

async function apiGet<T>(url: string) {
  const res = await fetch(url, { cache: "no-store" });
  const text = await res.text();

  if (!res.ok) {
    throw new Error(pickErrorMessage(res, text));
  }

  try {
    return JSON.parse(text) as RsData<T>;
  } catch {
    throw new Error("응답 파싱 실패");
  }
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
      const res = await apiGet<{ exists: boolean }>(
        `${API_BASE}/api/customers/exists?email=${encodeURIComponent(trimmed)}`
      );

      if (!res.data?.exists) {
        setErrorMsg("해당 이메일의 고객이 없습니다.");
        return;
      }

      sessionStorage.setItem("orderEmail", trimmed);
      router.push("/orders");
    } catch (e: any) {
      setErrorMsg(`${e?.message ?? "unknown error"}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={s.page}>
      <div className={s.container}>
        <div className={s.content}>
          <OrdersHeader
            headerRowClassName={s.headerRow}
            subtitle="주문 내역을 확인할 이메일을 입력해주세요."
          />

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
                  if (e.key === "Enter") onSubmit();
                }}
              />

              <button className={s.btn} onClick={onSubmit} disabled={loading}>
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
