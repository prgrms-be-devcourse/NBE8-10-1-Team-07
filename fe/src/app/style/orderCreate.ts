import { inputBase } from "./common";

export const orderCreateStyles = {
  page: "min-h-screen bg-gray-100",
  container: "mx-auto max-w-6xl px-4 py-6",
  headerRow: "mb-4 flex items-center justify-between",
  title: "text-xl font-bold text-gray-900",

  btnSearch:
    "rounded-md border border-gray-900 px-4 py-2 text-sm font-semibold text-gray-900 hover:bg-gray-900 hover:text-white",

  grid: "grid grid-cols-1 gap-4 md:grid-cols-10",
  leftCol: "md:col-span-7",
  rightCol: "md:col-span-3",

  card: "rounded-xl bg-white shadow-sm border border-gray-200",
  cardHeader: "px-4 py-3 border-b border-gray-200 font-semibold text-gray-900",

  list: "p-4 space-y-3",

  productRow: "flex items-center gap-3 rounded-lg border border-gray-200 p-3",
  thumb: "h-12 w-12 rounded-md bg-gray-200",
  productInfo: "flex-1",
  productName: "text-sm font-semibold text-gray-900",
  productPrice: "text-sm text-gray-700",

  btnAdd:
    "rounded-md border border-gray-900 bg-gray-900 px-3 py-1 text-xs font-semibold text-white hover:bg-black",

  empty: "p-4 text-sm text-gray-500",

  summaryList: "p-4 space-y-3",
  summaryRow: "flex items-center gap-3 rounded-lg border border-gray-200 p-3",
  summaryLeft: "flex-1 min-w-0",

  nameWrap: "relative min-w-0 group",
  summaryName: "text-sm font-semibold text-gray-900 truncate",
  tooltip:
    "pointer-events-none absolute left-0 top-full z-20 mt-1 hidden max-w-[260px] rounded-md bg-gray-900 px-2 py-1 text-xs text-white shadow-lg group-hover:block",

  summarySub: "text-xs text-gray-500",

  qtyBox: "flex items-center rounded-md border border-gray-300 overflow-hidden",
  qtyBtn: "px-3 py-1 text-sm font-bold text-gray-900 hover:bg-gray-100",
  qtyValue: "w-8 text-center text-sm font-semibold text-gray-900",

  btnRemove:
    "rounded-md bg-gray-900 px-3 py-1 text-xs font-semibold text-white hover:bg-black shrink-0",

  formBox: "px-4 pb-4 space-y-3",
  label: "block text-xs font-semibold text-gray-700",
  input: `mt-1 ${inputBase} disabled:opacity-100 `,

  totalRow: "flex items-center justify-between px-4 py-3 border-t border-gray-200",
  totalLabel: "text-sm font-semibold text-gray-700",
  totalValue: "text-sm font-bold text-gray-900",

  btnCheckout:
    "mx-4 mb-4 w-[calc(100%-2rem)] rounded-md bg-gray-900 px-4 py-3 text-sm font-semibold text-white hover:bg-black disabled:opacity-40 disabled:cursor-not-allowed",
} as const;
