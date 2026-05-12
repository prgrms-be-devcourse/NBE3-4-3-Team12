"use client";

import { useRouter } from "next/navigation";
import {joinGroup} from "@/app/api";
import React from "react";

type Category = {
  id: number;
  type: string;
  name: string;
};

type CardProps = {
  id: string;
  title: string;
  category: Category[];
  status: string;
};

const categoryColors: Record<string, string> = {
  STUDY: "bg-emerald-100 text-emerald-800",
  HOBBY: "bg-lime-100 text-lime-800",
  EXERCISE: "bg-green-100 text-green-800",
};

const statusMapping: Record<string, string> = {
  RECRUITING: "모집중",
  COMPLETED: "마감",
};

const statusColors: Record<string, string> = {
  모집중: "bg-emerald-200 text-emerald-900",
  마감: "bg-stone-200 text-stone-700",
};

const categoryMapping: Record<string, string> = {
  STUDY: "자기 개발",
  HOBBY: "취미",
  EXERCISE: "운동",
};

const Card = ({ id, title, category, status }: CardProps) => {
  const router = useRouter();
  const categoryKey = category.length > 0 ? category[0].name : "";
  const displayedStatus = statusMapping[status] || status;
  const displayedCategory =
    categoryKey
      ? categoryMapping[categoryKey] || categoryKey
      : "미정"; // 첫 번째 카테고리만 표시, 기본값 "미정"

  const handleJoin = async (event: React.MouseEvent) => {
    event.stopPropagation();
    try {
      await joinGroup(Number(id));
      alert("그룹 참가 성공!");
      router.push("/");
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || "그룹 참가에 실패했습니다.";
      alert(errorMessage);
    }
  }

  return (
    <div
      className="glass-surface group w-full rounded-2xl p-5 shadow-[0_8px_24px_rgba(33,152,93,0.09)] transition duration-200 hover:-translate-y-1 hover:shadow-[0_14px_32px_rgba(33,152,93,0.18)] flex flex-col justify-between"
      onClick={()=>router.push(`/groups/${id}`)}
    >
      {/* 제목 */}
      <h2 className="line-clamp-2 text-base font-bold text-[var(--text-main)]">{title}</h2>

      {/* 카테고리 */}
      <span
        className={`text-sm px-3 py-1 rounded-md mt-2 inline-block w-fit ${
          categoryColors[categoryKey] || "bg-gray-200 text-gray-700"
        }`}
      >
        {displayedCategory}
      </span>

      {/* 하단: 모집 상태 & 참가 버튼 */}
      <div className="flex justify-between items-center mt-4">
        {/* 모집 상태 */}
        <span
          className={`text-sm px-3 py-1 rounded-md ${
            statusColors[displayedStatus] || "bg-gray-200 text-gray-700"
          }`}
        >
          {displayedStatus}
        </span>

        {/* 참가하기 버튼 */}
        <button
            onClick={handleJoin}
            className="rounded-lg bg-[var(--accent)] px-4 py-1.5 text-sm font-semibold text-white transition hover:bg-[var(--accent-strong)]">
          참가하기
        </button>
      </div>
    </div>
  );
};

export default Card;
