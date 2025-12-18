export const ordersStyles = {
  emailText: "mt-1 text-sm text-gray-500",
  stack4: "space-y-4",
  stack2: "space-y-2",

  productRowButton: "w-full text-left hover:bg-gray-50",
  productSub: "mt-1 text-xs text-gray-500",
  productRight: "flex items-center gap-3",
  productAmount: "text-sm font-bold text-gray-900",
  chev: "text-xs font-semibold text-gray-500",

  alertError:
    "rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700",

  mt4: "mt-4",
  cardBody: "p-4",

  detailWrap: "rounded-lg border border-gray-200 bg-white",
  detailHeader:
    "px-4 py-3 border-b border-gray-200 text-sm font-semibold text-gray-900",
  detailLoading: "ml-2 text-xs font-normal text-gray-500",
  detailEmpty: "p-4 text-sm text-gray-500",
  detailScroll: "p-2 overflow-x-auto",
  detailMinWidth: "min-w-[920px]",

  // ✅ 마지막에 "관리" 컬럼(140px) 추가
  tableHead:
    "grid grid-cols-[110px_160px_110px_1fr_80px_90px_110px_140px] gap-2 px-2 py-2 text-xs font-semibold text-gray-600 border-y border-gray-200",
  // ✅ 글자톤 통일(text-gray-900) 추가
  tableRow:
    "grid grid-cols-[110px_160px_110px_1fr_80px_90px_110px_140px] gap-2 px-2 py-3 text-sm text-gray-900 border-b border-gray-100",

  textRight: "text-right",
  orderId: "font-semibold text-gray-900",

  statusBadge:
    "inline-flex items-center rounded-md border border-gray-200 bg-gray-50 px-2 py-1 text-xs font-semibold text-gray-700",

  addrCell: "min-w-0",
  addrText: "truncate",
  addrSub: "text-xs text-gray-500",

  subTotal: "text-right font-semibold text-gray-900",

  summaryGrid: "grid grid-cols-1 gap-3 sm:grid-cols-3",
  summaryCard: "rounded-lg border border-gray-200 p-3",
  summaryLabel: "text-xs font-semibold text-gray-600",
  summaryValue: "mt-1 text-lg font-bold text-gray-900",

  // ✅ 관리 버튼 영역(오른쪽 끝, 줄바꿈 방지)
  actionsCell: "flex justify-end gap-2 whitespace-nowrap",
  btnEdit:
    "inline-flex items-center rounded-md border border-gray-900 px-2 py-1 text-xs font-semibold text-gray-900 hover:bg-gray-900 hover:text-white",
  btnDelete:
    "inline-flex items-center rounded-md border border-red-600 px-2 py-1 text-xs font-semibold text-red-600 hover:bg-red-600 hover:text-white",
} as const;
