"use client";

import { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { getGroup, updateGroup } from "@/app/api/group";
import MainMenu from "@/app/components/MainMenu";

type GroupStatus = "RECRUITING" | "NOT_RECRUITING" | "COMPLETED" | "VOTING" | "DELETED";

export default function EditGroupPage() {
    const router = useRouter();
    const { id } = useParams();
    const [title, setTitle] = useState("");
    const [maxParticipants, setMaxParticipants] = useState<number | "">("");
    const [description, setDescription] = useState("");
    const [status, setStatus] = useState<GroupStatus>("RECRUITING");
    const [errorMessage, setErrorMessage] = useState("");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        async function fetchGroup() {
            try {
                const data = await getGroup(Number(id));
                setTitle(data.title);
                setDescription(data.description);
                setMaxParticipants(data.maxParticipants);
                setStatus(data.groupStatus);
            } catch (error) {
                console.error("그룹 데이터를 불러오는 중 오류 발생:", error);
            }
        }
        fetchGroup();
    }, [id]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!title.trim() || !maxParticipants) {
            setErrorMessage("모든 필수 항목을 입력해주세요.");
            return;
        }

        setLoading(true);
        setErrorMessage("");

        try {
            await updateGroup(Number(id), {
                title,
                description,
                maxParticipants: Number(maxParticipants),
                groupStatus: status,
            });
            alert("그룹 정보가 성공적으로 수정되었습니다.");
            router.push("/");
        } catch (error) {
            console.error("수정 요청 실패:", error);
            setErrorMessage("그룹 수정 중 오류가 발생했습니다. 다시 시도해주세요.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen pb-10">
            <MainMenu />
            <div className="app-shell mt-8">
                <div className="page-card">
                    <form className="space-y-6" onSubmit={handleSubmit}>
                        <div>
                            <label className="block font-semibold text-[var(--text-soft)]">제목</label>
                            <input
                                type="text"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                className="ui-input"
                                placeholder="제목을 입력하세요"
                            />
                        </div>

                        <div>
                            <label className="block font-semibold text-[var(--text-soft)]">그룹 상태</label>
                            <select
                                className="ui-select"
                                value={status}
                                onChange={(e) => setStatus(e.target.value as GroupStatus)}
                            >
                                <option value="RECRUITING">모집 중</option>
                                <option value="NOT_RECRUITING">모집 마감</option>
                                <option value="COMPLETED">투표 완료</option>
                            </select>
                        </div>

                        <div className="flex items-center space-x-2">
                            <label className="block font-semibold text-[var(--text-soft)]">최대 인원</label>
                            <input
                                type="number"
                                value={maxParticipants}
                                onChange={(e) => setMaxParticipants(Number(e.target.value))}
                                className="ui-input w-24 text-center"
                            />
                        </div>

                        <div>
                            <label className="block font-semibold text-[var(--text-soft)]">내용</label>
                            <textarea
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                className="ui-textarea h-32"
                                placeholder="내용을 입력하세요"
                            ></textarea>
                        </div>

                        {errorMessage && <p className="text-red-500">{errorMessage}</p>}

                        <div className="mt-6 flex justify-end space-x-4">
                            <button type="button" onClick={() => router.back()} className="btn-secondary px-6">
                                돌아가기
                            </button>
                            <button type="submit" className="btn-primary px-6" disabled={loading}>
                                {loading ? "수정 중..." : "수정하기"}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
