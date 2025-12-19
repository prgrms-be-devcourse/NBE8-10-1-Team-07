import { inputBase } from "./common";

export const ordersSearchStyles = {
  page: "min-h-screen bg-gray-100",
  container: "mx-auto max-w-5xl px-4 py-6",

  content: "mt-16 ml-40",

  headerRow: "mb-2 flex items-center justify-between",
  
  title: "text-xl font-bold text-gray-900 text-left",
  desc: "mt-1 mb-6 text-sm text-gray-500 text-left",

  box:
    "w-full max-w-xl rounded-xl border border-gray-200 bg-white p-6 shadow-sm",

  label: "block text-xs font-semibold text-gray-700",

  row: "flex items-center gap-2",

  input: `flex-1 ${inputBase}`,

  btn:
    "shrink-0 rounded-md bg-gray-900 px-4 py-2 text-sm font-semibold text-white hover:bg-black disabled:opacity-50",

  error:
    "mt-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700",
} as const;
