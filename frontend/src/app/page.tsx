"use client";

import MainMenu from "./components/MainMenu";
import CardList from "./components/CardList";
import {getGroups, getTop3Posts} from "@/app/api";
import {useEffect, useState} from "react";
import {useSearchParams} from "next/navigation";

export default function Home() {
    const searchParams = useSearchParams();
    const keyword = (searchParams.get("q") || "").trim().toLowerCase();
    const [groups, setGroups] = useState<any[]>([]); // 그룹 데이터 상태
    const [topPosts, setTopPosts] = useState<any[]>([]); // 인기 게시물 상태

    useEffect(() => {
        // 서버에서 그룹 데이터를 가져오는 함수
        const fetchGroups = async () => {
            const data = await getGroups(); // 서버에서 모든 그룹 데이터 가져오기
            setGroups(data); // 상태 업데이트
        };

        fetchGroups(); // 비동기 함수 호출

        // 인기 게시물 데이터를 가져오는 함수
        const fetchTopPosts = async () => {
            try {
                const posts = await getTop3Posts(); // 인기 게시물 API 호출
                setTopPosts(posts); // 받아온 인기 게시물 데이터로 상태 업데이트
            } catch (error) {
                console.error("인기 게시물 불러오기 실패", error);
            }
        };

        fetchTopPosts(); // 컴포넌트가 마운트되면 인기 게시물 API 호출
    }, []); // 빈 배열로 한 번만 호출됨

    const filteredGroups = keyword
        ? groups.filter((g) => `${g.title} ${g.description ?? ""}`.toLowerCase().includes(keyword))
        : groups;

    return (
        <div className="min-h-screen pb-10">
            <MainMenu/>

            {/* 인기 게시물 섹션 */}
            <section className="mx-auto my-8 w-[95%] max-w-6xl">
                <h3 className="mb-4 text-2xl font-bold text-[var(--text-main)]">인기 게시물</h3>
                {topPosts.length > 0 ? (
                    <CardList groups={topPosts}/> // 인기 게시물 데이터를 CardList 컴포넌트로 전달
                ) : (
                    <p className="text-center text-[var(--text-soft)]">인기 게시물이 없습니다.</p>
                )}
            </section>

            {/* 그룹 목록 섹션 */}
            <section className="mx-auto my-8 w-[95%] max-w-6xl">
                <h3 className="mb-4 text-2xl font-bold text-[var(--text-main)]">모든 그룹</h3>
                <CardList groups={filteredGroups}/> {/* 그룹 목록을 CardList 컴포넌트로 전달 */}
            </section>
        </div>
    );
}
