"use client";

import React, {useState} from "react";
import {useRouter} from "next/navigation";
import {adminLogin} from "@/app/api";

const AdminLoginPage = () => {
    const [adminName, setAdminName] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const router = useRouter();

    // 입력값 변경 핸들러
    const handleChange = (setter) => (e) => {
        setter(e.target.value);
    };

    // 로그인 처리
    const handleLogin = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await adminLogin({adminName, password});
            alert("관리자 로그인 성공!");
            router.push("/admin"); // 로그인 성공 시 대시보드로 이동
        } catch (err) {
            setError("로그인 실패: 아이디 또는 비밀번호를 확인하세요.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center p-4">
            <div className="page-card w-full max-w-md">
                <h1 className="text-2xl font-bold text-center mb-6">관리자 로그인</h1>
                {error && <p className="text-red-500 text-center mb-4">{error}</p>}
                <form onSubmit={handleLogin}>
                    <div className="mb-4">
                        <label className="mb-2 block text-[var(--text-soft)]">아이디</label>
                        <input
                            type="text"
                            value={adminName}
                            onChange={handleChange(setAdminName)}
                            className="ui-input"
                            placeholder="아이디를 입력하세요"
                            required
                        />
                    </div>
                    <div className="mb-6">
                        <label className="mb-2 block text-[var(--text-soft)]">비밀번호</label>
                        <input
                            type="password"
                            value={password}
                            onChange={handleChange(setPassword)}
                            className="ui-input"
                            placeholder="비밀번호를 입력하세요"
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        disabled={loading}
                        className={`w-full py-3 text-lg ${loading ? "btn-secondary" : "btn-primary"}`}
                    >
                        {loading ? "로그인 중..." : "로그인"}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default AdminLoginPage;
