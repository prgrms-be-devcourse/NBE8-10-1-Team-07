export const ordersEditStyles = {
    // edit 레이아웃
    gridEdit: "grid grid-cols-1 gap-4 md:grid-cols-10",
    leftEdit: "md:col-span-6",
    rightEdit: "md:col-span-4",

    readonlyInput:
        "mt-1 w-full rounded-md border border-gray-300 \
     bg-gray-400 px-3 py-2 text-sm font-semibold text-gray-700 \
     cursor-not-allowed",

    leftBody: "flex flex-col h-full",

    editBody: "p-4 space-y-3",
    row: "flex items-start justify-between gap-4",
    k: "text-xs font-semibold text-gray-600",
    v: "text-sm font-semibold text-gray-900",

    help: "pt-2 text-xs text-gray-500 border-t border-gray-100",

    formBody: "p-4 space-y-3",

    alertError:
        "rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700",

    btnPrimary:
        "w-full rounded-md bg-gray-900 px-4 py-3 text-sm font-semibold text-white hover:bg-black disabled:opacity-40 disabled:cursor-not-allowed",

    btnSecondary:
        "w-full rounded-md border border-gray-300 bg-white px-4 py-3 text-sm font-semibold text-gray-900 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed",
} as const;
