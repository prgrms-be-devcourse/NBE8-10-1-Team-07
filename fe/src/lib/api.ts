// src/lib/api.ts
export type RsData<T> = {
  resultCode: string;
  msg: string;
  data: T;
};

function pickErrorMessage(res: Response, rawText: string): string {
  try {
    const j = JSON.parse(rawText);

    // ✅ RsData 우선
    if (j && typeof j === "object" && typeof j.msg === "string" && j.msg.trim()) {
      return j.msg;
    }

    // ✅ Spring 기본 에러 JSON 대응(백엔드 수정 전/후 모두 커버)
    if (j && typeof j === "object") {
      if (typeof (j as any).message === "string" && (j as any).message.trim()) {
        return (j as any).message;
      }
      if (typeof (j as any).error === "string" && (j as any).error.trim()) {
        return (j as any).error;
      }
    }
  } catch {
    // JSON이 아니면 텍스트로 fallback
  }

  const trimmed = (rawText ?? "").trim();
  if (trimmed) return trimmed.length > 180 ? trimmed.slice(0, 180) + "..." : trimmed;

  return `HTTP ${res.status}`;
}

async function request<T>(url: string, init?: RequestInit) {
  const res = await fetch(url, init);
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

export function apiGet<T>(url: string) {
  return request<T>(url, { cache: "no-store" });
}

export function apiPost<T>(url: string, body: unknown) {
  return request<T>(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

export function apiPut<T>(url: string, body: unknown) {
  return request<T>(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

export function apiDelete<T>(url: string) {
  return request<T>(url, { method: "DELETE" });
}
