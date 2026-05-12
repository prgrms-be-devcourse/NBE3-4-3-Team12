"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { getCategories } from "@/app/api/categories";
import { createGroup } from "@/app/api/group";
import MainMenu from "@/app/components/MainMenu";
// votemodal 추가
import VoteModal from "@/app/components/VoteModal";
import {createVote} from "@/app/api/vote";


type Category = {
    id: number;
    type: string;
    name: string;
};

interface VoteLocation {
    location: string;
    address: string;
    latitude: number;
    longitude: number;
}

export default function CreateGroupPage() {
    const router = useRouter();
    const [title, setTitle] = useState("");
    const [categories, setCategories] = useState<Category[]>([]);
    const [selectedCategories, setSelectedCategories] = useState<Category[]>([]);
    const [categoryIds, setCategoryIds] = useState<number[]>([]);
    const [maxParticipants, setMaxParticipants] = useState<number | "">("");
    const [description, setDescription] = useState("");
    const [errorMessage, setErrorMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [voteLocations, setVoteLocations] = useState<VoteLocation[]>([]);

    // const [newLocation, setNewLocation] = useState("");
    // const [locations, setLocations] = useState<LocationVote[]>([]);

    useEffect(() => {
        async function fetchCategories() {
            try {
                const data = await getCategories();
                setCategories(data);
            } catch (error) {
                console.error("카테고리 데이터를 불러오는 중 오류 발생:", error);
            }
        }
        fetchCategories();
    }, []);

    const categoryTypes = [...new Set(categories.map((c) => c.type))];

    const filteredCategories = (type: string) =>
        categories.filter((c) => c.type === type);

    const handleCategorySelect = (category: Category) => {
        if (!selectedCategories.find((c) => c.id === category.id)) {
            setSelectedCategories([...selectedCategories, category]);
            setCategoryIds([...categoryIds, category.id]);
        }
    };

    const handleCategoryRemove = (categoryId: number) => {
        setSelectedCategories(selectedCategories.filter((c) => c.id !== categoryId));
        setCategoryIds(categoryIds.filter((id) => id !== categoryId));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        //모든 필드 필수로 입력하도록 변경
        if (!title.trim()) {
            setErrorMessage("제목을 입력해주세요.");
            return;
        }
        if (categoryIds.length === 0) {
            setErrorMessage("모집 분야를 선택해주세요.");
            return;
        }
        if (!maxParticipants || maxParticipants <= 0) {
            setErrorMessage("최대 인원을 설정해주세요.");
            return;
        }
        if (!description.trim()) {
            setErrorMessage("내용을 입력해주세요.");
            return;
        }
        if (voteLocations.length === 0) {
            setErrorMessage("최소 1개 이상의 투표 장소를 추가해주세요.");
            return;
        }

        setLoading(true);
        setErrorMessage("");

        const requestData = {
            title,
            description,
            maxParticipants: Number(maxParticipants),
            categoryIds,
            // 투표 장소 정보 포함
            voteLocations: voteLocations,
            status: "RECRUITING",
        };

        try {
            // 1. 먼저 그룹을 생성
            const groupResponse = await createGroup({
                title,
                description,
                maxParticipants: Number(maxParticipants),
                categoryIds,
                status: "RECRUITING",
            });

            // 2. 생성된 그룹의 ID로 vote들 생성
            const createdGroupId = groupResponse.id;
            for (const voteLocation of voteLocations) {
                await createVote(createdGroupId, voteLocation);
            }

            alert("모임이 성공적으로 생성되었습니다!");
            router.push("/");
        } catch (e) {
            setErrorMessage( e + "모임 생성 중 오류가 발생했습니다.");
        }
    };

    const VoteLocationsList = () => (
        <div className="mt-4">
            <h4 className="font-semibold mb-2">추가된 투표 장소</h4>
            {voteLocations.length === 0 ? (
                <p className="text-gray-500">아직 추가된 투표 장소가 없습니다.</p>
            ) : (
                <div className="space-y-2">
                    {voteLocations.map((loc, index) => (
                        <div key={index} className="flex justify-between items-center bg-gray-50 p-3 rounded-md">
                            <div>
                                <div className="font-medium">{loc.location}</div>
                                <div className="text-sm text-gray-600">{loc.address}</div>
                            </div>
                            <button
                                type="button"
                                onClick={() => {
                                    setVoteLocations(voteLocations.filter((_, i) => i !== index));
                                }}
                                className="text-red-500 hover:text-red-700"
                            >
                                ✕
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );

    return (
        <div className="min-h-screen pb-10">
            <MainMenu />
            <div className="app-shell mt-8">
            <div className="page-card">
                <form className="space-y-6" onSubmit={handleSubmit}>
                    {/*  제목 입력 */}
                    <div>
                        <label className="block font-semibold text-[var(--text-soft)]">제목</label>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            className="ui-input"
                            placeholder="제목을 입력하세요."
                        />
                    </div>

                    {/* 모집 구분 선택 */}
                    <div>
                        <label className="block font-semibold text-[var(--text-soft)]">모집 구분</label>
                        <select
                            className="ui-select"
                            onChange={(e) => {
                                const selectedType = e.target.value;
                                if (!selectedType) return;

                                // 중복 추가 방지
                                const newCategories = filteredCategories(selectedType).filter(
                                    (c) => !categories.some((existing) => existing.id === c.id)
                                );

                                setCategories((prev) => [...prev, ...newCategories]);
                            }}
                        >
                            <option value="">선택하세요</option>
                            {categoryTypes.map((type, index) => (
                                <option key={`type-${index}`} value={type}>
                                    {type}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* 모집 분야 선택 */}
                    <div>
                        <label className="block font-semibold text-[var(--text-soft)]">모집 분야</label>
                        <select
                            className="ui-select"
                            onChange={(e) => {
                                const selectedCategory = categories.find(
                                    (c) => c.name === e.target.value
                                );
                                if (selectedCategory && !selectedCategories.some((c) => c.id === selectedCategory.id)) {
                                    setSelectedCategories([...selectedCategories, selectedCategory]);
                                    setCategoryIds([...categoryIds, selectedCategory.id]);
                                }
                            }}
                        >
                            <option value="">모집 분야 선택</option>
                            {categories.map((category) => (
                                <option key={`category-${category.id}-${category.type}`} value={category.name}>
                                    {category.name}
                                </option>
                            ))}
                        </select>

                        {/* 선택된 모집 분야를 라벨로 표시 */}
                        <div className="mt-3 flex flex-wrap gap-2">
                            {selectedCategories.map((category) => (
                                <span key={`selected-${category.id}-${category.type}`} className="rounded-full bg-emerald-100 px-3 py-1 text-sm font-semibold text-emerald-800">
                        {category.type} - {category.name}
                                    <button
                                        onClick={() => handleCategoryRemove(category.id)}
                                        className="ml-2 text-red-600 font-bold"
                                    >
                            ✕
                        </button>
                    </span>
                            ))}
                        </div>
                    </div>

                    {/* 모집 인원 설정 (숫자만 입력 가능) */}
                    <div className="flex items-center space-x-2">
                        <label className="block font-semibold text-[var(--text-soft)]">최대 인원</label>
                        <div className="flex items-center rounded-xl border border-[var(--line)] bg-white/90">
                            {/* 감소 버튼 */}
                            <button
                                type="button"
                                onClick={() => setMaxParticipants((prev) => Math.max(1, Number(prev) - 1))}
                                className="rounded-l-xl bg-emerald-100 px-3 py-2 hover:bg-emerald-200"
                            >
                                -
                            </button>

                            {/* 숫자 입력 (기본 버튼 제거 + 문자 입력 차단) */}
                            <input
                                type="number" // 🔥 "number" 대신 "text"로 변경 (문자 강제 차단)
                                value={maxParticipants}
                                onChange={(e) => {
                                    const value = e.target.value;
                                    const numValue = Number(value);
                                    // 최소값 1 이상으로 설정
                                    if (value === "" || (!isNaN(numValue) && numValue > 0)) {
                                        setMaxParticipants(value === "" ? "" : numValue);
                                    }
                                }}
                                className="w-16 border-none bg-transparent px-2 text-center focus:outline-none"
                                inputMode="numeric"
                                min={1}
                                style={{
                                    appearance: "none", // 기본 UI 제거
                                    MozAppearance: "textfield", // 파이어폭스 대응
                                }}
                            />

                            {/* 증가 버튼 */}
                            <button
                                type="button"
                                onClick={() => setMaxParticipants((prev) => Number(prev) + 1)}
                                className="rounded-r-xl bg-emerald-100 px-3 py-2 hover:bg-emerald-200"
                            >
                                +
                            </button>
                        </div>
                    </div>

                    <style>
                        {`
  /* 기본 숫자 증감 버튼 완전히 숨김 */
  input[type="number"]::-webkit-outer-spin-button,
  input[type="number"]::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
`}
                    </style>


                    {/*/!*  장소 투표 *!/*/}
                    <div>
                        <div className="flex items-center justify-between">
                            <label className="block font-semibold text-[var(--text-soft)]">
                                장소 투표 *
                            </label>
                            <button
                                type="button"
                                onClick={() => setIsModalOpen(true)}
                                className="btn-primary"
                            >
                                장소 추가
                            </button>
                        </div>
                        <VoteLocationsList />

                        <VoteModal
                            isOpen={isModalOpen}
                            onClose={() => setIsModalOpen(false)}
                            groupId={0}
                            onVoteCreated={(location: VoteLocation) => {
                                setVoteLocations([...voteLocations, location]);
                                setIsModalOpen(false);
                            }}
                        />
                    </div>

                        {/*  내용 입력 칸 */}
                        <div>
                            <label className="block font-semibold text-[var(--text-soft)]">내용</label>
                            <textarea
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                className="ui-textarea h-32"
                                placeholder="내용을 입력하세요."
                            ></textarea>
                        </div>

                        {/*  오류 메시지 */}
                        {errorMessage && <p className="text-red-500">{errorMessage}</p>}

                        {/*  "돌아가기 / 등록" 버튼 추가 (하단 정렬) */}
                        <div className="flex justify-end space-x-4 mt-6">
                            <button
                                type="button"
                                onClick={() => router.back()}
                                className="btn-secondary px-6"
                            >
                                돌아가기
                            </button>
                            <button
                                type="submit"
                                className="btn-primary px-6"
                                disabled={loading}
                            >
                                {loading ? "등록 중..." : "등록"}
                            </button>
                        </div>
                </form>
            </div>
            </div>
        </div>
);
}
