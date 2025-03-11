"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { getMembersByNickName, blacklistMember } from "@/app/api";

interface User {
    id: string;
    nickname: string;
    email: string;
}

const AdminUserSearchPage = () => {
    const [nickname, setNickname] = useState("");
    const [userList, setUserList] = useState<User[]>([]); // 유저 리스트 타입 수정
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const router = useRouter();

    // 유저 검색 처리
    const handleSearch = async () => {
        setLoading(true);
        try {
            const users = (await getMembersByNickName(nickname)).map((user) => ({
                id: user.id,
                nickname: user.nickname,
                email: user.email || "", // 기본값 처리
            }));
            setUserList(users); // 반환된 data 부분에 맞게 처리
            setError(null);
        } catch (error) {
            setError("유저 검색에 실패했습니다. 다시 시도해 주세요.");
            setUserList([]);
        }
        setLoading(false);
    };

    // 블랙리스트 처리
    const handleBlacklist = async (memberId: string) => {
        const confirmed = window.confirm("정말로 블랙리스트 처리를 하시겠습니까?");
        if (confirmed) {
            try {
                await blacklistMember(memberId);
                alert("유저가 블랙리스트 처리되었습니다.");
                setUserList(userList.filter((user) => user.id !== memberId)); // 블랙리스트 처리 후 리스트에서 제거
            } catch (error) {
                alert("블랙리스트 처리 실패. 다시 시도해 주세요.");
            }
        }
    };

    return (
        <div className="max-w-lg mx-auto mt-12 p-8 bg-white rounded-xl shadow-xl text-lg">
            <h1 className="text-2xl font-bold text-center mb-6">유저 검색 및 블랙리스트 처리</h1>

            <div className="mb-6">
                <input
                    type="text"
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                    placeholder="닉네임으로 검색"
                    className="w-full p-3 border border-gray-300 rounded-lg mb-4"
                />
                <button
                    onClick={handleSearch}
                    className="w-full bg-blue-500 text-white py-3 rounded-lg text-lg hover:bg-blue-600"
                >
                    유저 검색
                </button>
            </div>

            {loading && <div className="text-center mt-4">검색 중...</div>}

            {error && <div className="text-red-500 text-center mt-4">{error}</div>}

            {userList.length > 0 && (
                <div>
                    <h2 className="text-xl font-semibold mb-4">검색된 유저</h2>
                    <ul>
                        {userList.map((user) => (
                            <li key={user.id} className="flex justify-between items-center mb-4">
                                <span>{user.nickname} ({user.email})</span> {/* email을 표시 */}
                                <button
                                    onClick={() => handleBlacklist(user.id)}
                                    className="bg-red-500 text-white py-2 px-4 rounded-lg text-sm hover:bg-red-600"
                                >
                                    블랙리스트하기
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {userList.length === 0 && !loading && (
                <div className="text-center text-gray-500 mt-6">검색된 유저가 없습니다.</div>
            )}
        </div>
    );
};

export default AdminUserSearchPage;
