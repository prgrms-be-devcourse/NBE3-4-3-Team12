"use client";

import React, {useEffect, useState} from "react";
import MainMenu from "../components/MainMenu";
import {useRouter} from "next/navigation"; // useRouter 훅 추가
import {getCurrentUser, getUserGroups, getLocationOfGroup} from "@/app/api"; // getUserGroups 함수 추가

const MyInfoPage = () => {
    const [user, setUser] = useState(null);
    const [groups, setGroups] = useState([]); // 소속 모임 목록
    const [locations, setLocations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const router = useRouter(); // useRouter 훅 사용

    useEffect(() => {
        const fetchData = async () => {
            try {
                const userData = await getCurrentUser();
                setUser(userData.data); // "data" 객체에서 필요한 정보를 추출

                const userGroups = await getUserGroups(); // 소속 모임 목록 조회
                setGroups(userGroups); // 소속 모임 상태 업데이트
                setLoading(false);

                const completeLocation = await getLocationOfGroup();
                setLocations(completeLocation);
                setLoading(false);
            } catch (err) {
                setError("정보를 불러오는데 실패했습니다.");
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    // 프로필 수정 버튼 클릭 시 네비게이트
    const handleEditProfile = () => {
        router.push("/profile/edit"); // "/profile/edit" 경로로 네비게이트
    };

    // 그룹 클릭 시 해당 그룹 페이지로 이동
    const handleGroupClick = (groupId) => {
        router.push(`/groups/${groupId}`); // 그룹 상세 페이지로 이동
    };

    if (loading) {
        return <div>로딩 중...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div className="min-h-screen pb-10">
            <MainMenu/>
            <div className="app-shell mt-8 max-w-3xl">
            <div className="page-card text-lg">
                <h1 className="text-2xl font-bold mb-6">내 정보</h1>
                <div className="mb-6">
                    <p className="text-[var(--text-soft)]">이름: {user.nickname}</p>
                    <p className="text-[var(--text-soft)]">이메일: {user.email}</p>
                </div>
                <button
                    onClick={handleEditProfile} // 버튼 클릭 시 프로필 수정 페이지로 이동
                    className="btn-primary w-full py-3 text-lg"
                >
                    프로필 수정
                </button>
                <h2 className="text-xl font-semibold mt-8 mb-4">소속 모임</h2>
                <ul className="space-y-4">
                    {groups.length === 0 ? (
                        <li>참여 중인 모임이 없습니다.</li>
                    ) : (
                        groups.map((group) => (
                            <li
                                key={group.id}
                                className="flex cursor-pointer justify-between rounded-xl border border-[var(--line)] bg-white/80 p-4 text-lg hover:bg-emerald-50"
                                onClick={() => handleGroupClick(group.id)} // 그룹 클릭 시 해당 페이지로 이동
                            >
                                <span>{group.title}</span>
                                <span className="text-base text-[var(--text-soft)]">{group.status}</span>
                            </li>
                        ))
                    )}
                </ul>
                <h2 className="text-xl font-semibold mt-8 mb-4">투표 완료 모임</h2>
                <ul className="space-y-4">
                    {locations.length === 0 ? (
                        <li>참여 중인 모임이 없습니다.</li>
                    ) : (
                        locations.map((location) => (
                            <li
                                key={location.id}
                                className="flex cursor-pointer justify-between rounded-xl border border-[var(--line)] bg-white/80 p-4 text-lg hover:bg-emerald-50"
                                onClick={() => handleGroupClick(location.groupId)} // 그룹 클릭 시 해당 페이지로 이동
                            >
                                <span>{location.title}</span>
                                <span className="text-base text-[var(--text-soft)]">{location.location}</span>
                            </li>
                        ))
                    )}
                </ul>
            </div>
            </div>
        </div>
    );
};

export default MyInfoPage;
