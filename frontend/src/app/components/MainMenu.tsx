"use client";

import Link from "next/link";
import {getCurrentUser, kakaoLogin, kakaoLogout} from "@/app/api";
import {useEffect, useState} from "react";
import {useRouter} from "next/navigation";

const MainMenu = () => {
    const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
    const [keyword, setKeyword] = useState("");
    const router = useRouter();

    useEffect(() => {
        const checkLoginStatus = async () => {
            try {
                const user = await getCurrentUser();
                if (user) {
                    setIsLoggedIn(true);
                }
            } catch (error) {
                setIsLoggedIn(false);
            }
        };

        checkLoginStatus();
    }, []);

    const handleKakaoLogin = async () => {
        kakaoLogin();
        setIsLoggedIn(true);
    };

    const handleKakaoLogout = async () => {
        kakaoLogout();
        setIsLoggedIn(false);
    };

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        router.push(keyword.trim() ? `/?q=${encodeURIComponent(keyword.trim())}` : "/");
    };

    return (
        <nav
            className="glass-surface sticky top-0 z-20 mx-auto mt-3 flex w-[95%] max-w-6xl items-center justify-between rounded-2xl px-5 py-4 shadow-[0_10px_30px_rgba(33,152,93,0.12)]">
            <Link href="/">
                <h1 className="text-xl font-black tracking-wide text-[var(--text-main)]">모두모여</h1>
            </Link>
            <form onSubmit={handleSearch} className="mx-6 hidden flex-1 items-center gap-2 md:flex">
                <input
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    placeholder="모임 검색 (제목, 키워드)"
                    className="ui-input py-2"
                />
                <button type="submit" className="btn-secondary whitespace-nowrap">검색</button>
            </form>
            <div className="flex items-center gap-4">
                {isLoggedIn && (
                    <Link href="/groups/create">
                        <button
                            className="rounded-xl bg-[var(--accent)] px-4 py-2 font-semibold text-white transition hover:-translate-y-0.5 hover:bg-[var(--accent-strong)]">
                            모임 만들기
                        </button>
                    </Link>
                )}
                {isLoggedIn && (
                    <Link href="/profile"
                          className="font-medium text-[var(--text-soft)] transition hover:text-[var(--accent-strong)]">
                        내정보
                    </Link>
                )}
                {isLoggedIn ? (
                    <button onClick={handleKakaoLogout}
                            className="font-medium text-[var(--text-soft)] transition hover:text-[var(--accent-strong)]">
                        로그아웃
                    </button>
                ) : (
                    <button onClick={handleKakaoLogin}
                            className="rounded-xl border border-[var(--line)] bg-[var(--surface)] px-3 py-2 font-medium text-[var(--text-soft)] transition hover:border-[var(--accent)] hover:text-[var(--accent-strong)]">
                        로그인
                    </button>
                )}
            </div>
        </nav>
    );
};

export default MainMenu;
