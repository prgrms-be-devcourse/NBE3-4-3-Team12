"use client";

import {useEffect, useState} from "react";
import {useRouter} from "next/navigation";
import {adminDeleteGroup, checkAdminAuth, getGroups} from "@/app/api";

const AdminGroupsPage = () => {
    const [groups, setGroups] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    useEffect(() => {
        const fetchAdmin = async () => {
            const adminData = await checkAdminAuth();
            if (!adminData) {
                alert("관리자 로그인이 필요합니다.");
                router.push("/admin/login");
                return;
            }
            const data = await getGroups();
            setGroups(data);
            setLoading(false);
        };

        fetchAdmin();
    }, [router]);

    const handleDeleteGroup = async (groupId: number) => {
        if (!confirm("정말 삭제하시겠습니까?")) return;

        try {
            await adminDeleteGroup(groupId);
            alert("모임이 삭제되었습니다.");
            setGroups((prev) => prev.filter((group) => group.id !== groupId));
        } catch (error: any) {
            alert(error.message);
        }
    };

    if (loading) {
        return <div className="mt-10 text-center">로딩 중...</div>;
    }

    return (
        <div className="app-shell mt-10 page-card">
            <h1 className="mb-6 text-center text-2xl font-bold">모임 관리</h1>
            <div className="table-shell">
                <table className="w-full border-collapse">
                    <thead>
                    <tr className="bg-emerald-100">
                        <th className="border border-[var(--line)] px-4 py-2">제목</th>
                        <th className="border border-[var(--line)] px-4 py-2">작성자</th>
                        <th className="border border-[var(--line)] px-4 py-2">상태</th>
                        <th className="border border-[var(--line)] px-4 py-2">생성일</th>
                        <th className="border border-[var(--line)] px-4 py-2">삭제</th>
                    </tr>
                    </thead>
                    <tbody>
                    {groups.length === 0 ? (
                        <tr><td colSpan={5} className="py-4 text-center">등록된 모임이 없습니다.</td></tr>
                    ) : (
                        groups.map((group) => (
                            <tr key={group.id} className="text-center">
                                <td className="border border-[var(--line)] px-4 py-2">{group.title}</td>
                                <td className="border border-[var(--line)] px-4 py-2">{group.author}</td>
                                <td className="border border-[var(--line)] px-4 py-2">{group.status}</td>
                                <td className="border border-[var(--line)] px-4 py-2">{new Date(group.createdAt).toISOString().split("T")[0]}</td>
                                <td className="border border-[var(--line)] px-4 py-2">
                                    <button onClick={() => handleDeleteGroup(group.id)} className="btn-danger px-3 py-1">삭제</button>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>

            <div className="mt-6 text-center">
                <button onClick={() => router.push("/admin")} className="btn-primary">관리자 메인으로</button>
            </div>
        </div>
    );
};

export default AdminGroupsPage;
