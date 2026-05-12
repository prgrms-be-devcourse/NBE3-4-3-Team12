"use client";

import { useState } from "react";
import { getMembersByNickName, blacklistMember } from "@/app/api";

interface User {
    id: string;
    nickname: string;
    email: string;
}

const AdminUserSearchPage = () => {
    const [nickname, setNickname] = useState("");
    const [userList, setUserList] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSearch = async () => {
        setLoading(true);
        try {
            const users = (await getMembersByNickName(nickname)).map((user) => ({
                id: user.id,
                nickname: user.nickname,
                email: user.email || "",
            }));
            setUserList(users);
            setError(null);
        } catch {
            setError("유저 검색에 실패했습니다. 다시 시도해 주세요.");
            setUserList([]);
        }
        setLoading(false);
    };

    const handleBlacklist = async (memberId: string) => {
        const confirmed = window.confirm("정말로 블랙리스트 처리를 하시겠습니까?");
        if (!confirmed) return;

        try {
            await blacklistMember(memberId);
            alert("유저가 블랙리스트 처리되었습니다.");
            setUserList(userList.filter((user) => user.id !== memberId));
        } catch {
            alert("블랙리스트 처리 실패. 다시 시도해 주세요.");
        }
    };

    return (
        <div className="app-shell mt-10 max-w-3xl page-card text-lg">
            <h1 className="mb-6 text-center text-2xl font-bold">유저 검색 및 블랙리스트 처리</h1>

            <div className="mb-6">
                <input
                    type="text"
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                    placeholder="닉네임으로 검색"
                    className="ui-input mb-4"
                />
                <button onClick={handleSearch} className="btn-primary w-full py-3 text-lg">유저 검색</button>
            </div>

            {loading && <div className="mt-4 text-center">검색 중...</div>}
            {error && <div className="mt-4 text-center text-red-500">{error}</div>}

            {userList.length > 0 && (
                <div>
                    <h2 className="mb-4 text-xl font-semibold">검색된 유저</h2>
                    <ul>
                        {userList.map((user) => (
                            <li key={user.id} className="mb-4 flex items-center justify-between">
                                <span>{user.nickname} ({user.email})</span>
                                <button onClick={() => handleBlacklist(user.id)} className="btn-danger px-4 py-2 text-sm">블랙리스트하기</button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {userList.length === 0 && !loading && (
                <div className="mt-6 text-center text-[var(--text-soft)]">검색된 유저가 없습니다.</div>
            )}
        </div>
    );
};

export default AdminUserSearchPage;
