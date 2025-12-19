"use client";

import { useRouter } from "next/navigation";
import { textTokens } from "@/app/style/textTokens"; // 너 프로젝트에 있는 토큰 사용(경로 맞춰)

type Props = {
  headerRowClassName?: string;

  /** 왼쪽 큰 타이틀 (기본: 4Lines) */
  title?: string;

  /** 타이틀 아래 설명 문구 (없으면 렌더 안함) */
  subtitle?: string;

  /** 타이틀 클릭 시 이동 (기본: /orders/create) */
  onTitleClick?: () => void;

  /** 오른쪽 버튼: 라벨 없으면 렌더 안함 */
  rightLabel?: string;
  onRightClick?: () => void;
  btnClassName?: string;
};

export default function OrdersHeader({
  headerRowClassName,
  title = "4Lines",
  subtitle,
  onTitleClick,
  rightLabel,
  onRightClick,
  btnClassName,
}: Props) {
  const router = useRouter();

  const handleTitleClick = () => {
    if (onTitleClick) return onTitleClick();
    router.push("/orders/create");
  };

  const showRight = Boolean(rightLabel && onRightClick);

  return (
    <div className={headerRowClassName ?? "mb-4 flex items-start justify-between"}>
      <div className="flex flex-col gap-1">
        <button
          type="button"
          onClick={handleTitleClick}
          className="text-left"
        >
          <div className={textTokens.title}>{title}</div>
        </button>

        {subtitle ? (
          <div className={textTokens.sub}>{subtitle}</div>
        ) : null}
      </div>

      {showRight ? (
        <button
          type="button"
          className={btnClassName ?? "rounded-md bg-gray-900 px-4 py-2 text-sm font-semibold text-white hover:bg-black"}
          onClick={onRightClick}
        >
          {rightLabel}
        </button>
      ) : null}
    </div>
  );
}
