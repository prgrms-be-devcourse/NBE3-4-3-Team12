"use client";

import MainMenu from "./components/MainMenu";
import CardList from "./components/CardList";
import { getGroups, getTop3Posts } from "./api/group"; // getTop3Posts 추가
import { useEffect, useState } from "react";

export default function Home() {
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

    return (
        <div className="min-h-screen bg-gray-50">
            <MainMenu />
            {/* 인기 게시물 섹션 */}
            <div className="my-8">
                <h3 className="text-xl font-semibold mb-4">인기 게시물</h3>
                {topPosts.length > 0 ? (
                    <CardList groups={topPosts} /> // 인기 게시물 데이터를 CardList 컴포넌트로 전달
                ) : (
                    <p className="text-center text-gray-500">인기 게시물이 없습니다.</p>
                )}
            </div>

            {/* 그룹 목록 섹션 */}
            <div className="my-8">
                <h3 className="text-xl font-semibold mb-4">모든 그룹</h3>
                <CardList groups={groups} /> {/* 그룹 목록을 CardList 컴포넌트로 전달 */}
            </div>
        </div>
    );
}
