import { textTokens } from "./textTokens";

export const ordersStyles = {
  // 상단 이메일(기존: text-gray-500)
  emailText: `mt-1 ${textTokens.sub}`,

  stack4: "space-y-4",
  stack2: "space-y-2",

  productRowButton: "w-full text-left hover:bg-gray-50",

  // 상품명 아래 보조문구(기존: text-gray-500)
  productSub: `mt-1 ${textTokens.subXs}`,

  productRight: "flex items-center gap-3",

  // 금액은 진하게 유지
  productAmount: `text-sm font-bold text-gray-900`,

  // (사용 안 하면 지워도 됨)
  chev: textTokens.mutedXs,

  // 에러 토큰화(기존 유지해도 됨)
  alertError:
    "rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700",

  mt4: "mt-4",
  cardBody: "p-4",

  detailWrap: "rounded-lg border border-gray-200 bg-white",

  // 헤더도 토큰 적용
  detailHeader: `px-4 py-3 border-b border-gray-200 ${textTokens.body}`,

  detailLoading: `ml-2 ${textTokens.subXs}`,
  detailEmpty: `p-4 ${textTokens.sub}`,
  detailScroll: "p-2 overflow-x-auto",
  detailMinWidth: "min-w-[900px]",

  // ✅ 여기 핵심: 상세 테이블 헤더/바디 색 통일
  tableHead:
    `grid grid-cols-[110px_160px_110px_1fr_80px_90px_110px_120px] ` +
    `gap-2 px-2 py-2 border-y border-gray-200 items-start ${textTokens.mutedXs}`,

  // ✅ 여기 핵심: tableRow 자체를 진한 글씨로(내부 div 전부 상속)
  tableRow:
    "grid grid-cols-[110px_160px_110px_1fr_80px_90px_110px_120px] " +
    "gap-2 px-2 py-3 text-sm font-semibold text-gray-900 border-b border-gray-100 items-start",

  // 상태 뱃지는 기존 느낌 유지(원하면 body로 바꿔도 됨)
  statusBadge:
    "inline-flex items-center rounded-md border border-gray-200 bg-gray-50 px-2 py-1 text-xs font-semibold text-gray-700",

  addrCell: "min-w-0 text-left",
  addrText: "truncate text-gray-900",

  // 배송지 우편번호 보조텍스트도 토큰으로
  addrSub: textTokens.subXs,

  subTotalLeft: "font-semibold text-gray-900",
  actionsCell: "flex items-start gap-2",

  btnEdit:
    "rounded-md border border-gray-900 px-3 py-1 text-xs font-semibold text-gray-900 hover:bg-gray-900 hover:text-white",
  btnDelete:
    "rounded-md border border-red-500 px-3 py-1 text-xs font-semibold text-red-600 hover:bg-red-600 hover:text-white",

  summaryGrid: "grid grid-cols-1 gap-3 sm:grid-cols-3",
  summaryCard: "rounded-lg border border-gray-200 p-3",
  summaryLabel: textTokens.mutedXs,
  summaryValue: "mt-1 text-lg font-bold text-gray-900",
} as const;
