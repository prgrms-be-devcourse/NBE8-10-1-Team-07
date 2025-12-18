export const ordersStyles = {
  page: "min-h-screen bg-gray-100",
  container: "mx-auto max-w-5xl px-4 py-6",
  title: "text-xl font-bold text-gray-900 mb-4",

  searchRow: "flex gap-2 mb-4",
  input:
    "flex-1 rounded-md border border-gray-300 bg-white px-3 py-2 text-sm outline-none focus:border-gray-900 focus:ring-1 focus:ring-gray-900",
  btn:
    "rounded-md bg-gray-900 px-4 py-2 text-sm font-semibold text-white hover:bg-black disabled:opacity-50",

  error: "mb-3 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700",
  card: "rounded-xl bg-white shadow-sm border border-gray-200 overflow-hidden",

  summaryHeader:
    "grid grid-cols-[1fr_140px_120px] items-center gap-2 px-4 py-3 border-b border-gray-200 text-xs font-semibold text-gray-600",
  hLeft: "text-left",
  hRight: "text-right",

  empty: "px-4 py-10 text-center text-sm text-gray-500",

  list: "divide-y divide-gray-100",
  rowBtn:
    "w-full text-left grid grid-cols-[1fr_140px_120px] items-center gap-2 px-4 py-3 hover:bg-gray-50",
  rowActive: "bg-gray-50",

  rowLeft: "flex items-center gap-3 min-w-0",
  thumb: "h-10 w-10 rounded-md bg-gray-200 shrink-0",
  nameWrap: "min-w-0",
  name: "text-sm font-semibold text-gray-900 truncate",
  sub: "text-xs text-gray-500",
  rowRight: "text-right text-sm text-gray-900",
  badge: "inline-flex items-center rounded-md bg-gray-100 px-2 py-1 text-xs font-semibold text-gray-700",

  detailBox: "border-t border-gray-200 bg-white",
  detailTitle: "px-4 py-3 text-sm font-semibold text-gray-900",
  loading: "text-xs font-normal text-gray-500",
  detailEmpty: "px-4 pb-6 text-sm text-gray-500",

  tableWrap: "px-2 pb-4",
  thead:
    "grid grid-cols-[160px_1fr_120px_100px_80px_120px] gap-2 px-2 py-2 text-xs font-semibold text-gray-600 border-y border-gray-200",
  trow:
    "grid grid-cols-[160px_1fr_120px_100px_80px_120px] gap-2 px-2 py-3 text-sm border-b border-gray-100",
  tMain: "text-sm text-gray-900",
  tSub: "text-xs text-gray-500",
  tBold: "font-semibold text-gray-900",
  status:
    "inline-flex items-center rounded-md border border-gray-200 bg-gray-50 px-2 py-1 text-xs font-semibold text-gray-700",
} as const;
