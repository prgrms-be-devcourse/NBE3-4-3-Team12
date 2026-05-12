"use client";

import React, {useState} from "react";
import {useRouter} from "next/navigation"; // useRouter 훅 사용
import {updateUserProfile} from "@/app/api";
import MainMenu from "@/app/components/MainMenu";

const ProfileEditPage = () => {
    const [nickname, setNickname] = useState(""); // nickname 상태
    const [loading, setLoading] = useState(false); // 로딩 상태
    const [error, setError] = useState(null); // 에러 상태
    const router = useRouter(); // 라우터 사용

    // nickname 변경 처리
    const handleNicknameChange = (e) => {
        setNickname(e.target.value);
    };

    // 프로필 수정 요청 처리
    const handleSubmit = async (e) => {
        e.preventDefault(); // 기본 폼 제출 방지

        if (!nickname) {
            setError("이름을 입력해주세요.");
            return;
        }

        setLoading(true); // 로딩 시작
        try {
            // 프로필 수정 API 호출
            const response = await updateUserProfile({nickname});
            alert(`이름이 ${response.data.nickname}(으)로 변경되었습니다!`); // 알림 띄우기
            router.push("/profile"); // 수정 완료 후 profile 페이지로 이동
        } catch (err) {
            setError("프로필 수정에 실패했습니다. 다시 시도해주세요.");
        } finally {
            setLoading(false); // 로딩 종료
        }
    };

    return (
        <div className="min-h-screen pb-10">
            <MainMenu />
            <div className="app-shell mt-8 max-w-2xl">
                <div className="page-card text-lg">
                    <h1 className="mb-6 text-2xl font-bold">프로필 수정</h1>
                    {error && <p className="mb-4 text-red-500">{error}</p>}
                    <form onSubmit={handleSubmit}>
                        <div className="mb-6">
                            <label htmlFor="nickname" className="mb-2 block text-[var(--text-soft)]">이름</label>
                            <input
                                type="text"
                                id="nickname"
                                value={nickname}
                                onChange={handleNicknameChange}
                                className="ui-input"
                                placeholder="새로운 이름을 입력하세요"
                            />
                        </div>
                        <button
                            type="submit"
                            disabled={loading}
                            className={`w-full py-3 text-lg ${loading ? "btn-secondary" : "btn-primary"}`}
                        >
                            {loading ? "수정 중..." : "수정 완료"}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default ProfileEditPage;
