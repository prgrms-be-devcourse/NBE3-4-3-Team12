"use client";

import {useEffect, useState} from "react";
import {addCategory, deleteCategory, getCategories, modifyCategory} from "@/app/api";

const CATEGORY_TYPES = ["EXERCISE", "STUDY", "HOBBY"];

const AdminCategoriesPage = () => {
    const [categories, setCategories] = useState<{ id: number; type: string; name: string }[]>([]);
    const [type, setType] = useState("EXERCISE");
    const [name, setName] = useState("");
    const [editCategoryId, setEditCategoryId] = useState<number | null>(null);
    const [editType, setEditType] = useState("EXERCISE");
    const [editName, setEditName] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchCategories = async () => {
            const data = await getCategories();
            setCategories(data);
            setLoading(false);
        };
        fetchCategories();
    }, []);

    const handleAddCategory = async () => {
        if (!name.trim()) {
            alert("카테고리 이름을 입력해주세요.");
            return;
        }

        const newCategory = await addCategory(type, name);
        if (newCategory) {
            setCategories([...categories, newCategory]);
            setName("");
        }
    };

    const handleDeleteCategory = async (categoryId: number) => {
        if (!confirm("정말 삭제하시겠습니까?")) return;

        const success = await deleteCategory(categoryId);
        if (success) {
            setCategories(categories.filter((cat) => cat.id !== categoryId));
        }
    };

    const handleEditCategory = async () => {
        if (!editName.trim()) {
            alert("수정할 카테고리 이름을 입력해주세요.");
            return;
        }

        const updatedCategory = await modifyCategory(editCategoryId!, editType, editName);
        if (updatedCategory) {
            setCategories(
                categories.map((category) =>
                    category.id === editCategoryId
                        ? {...category, type: editType, name: editName}
                        : category
                )
            );
            setEditCategoryId(null);
            setEditType("EXERCISE");
            setEditName("");
        }
    };

    if (loading) {
        return <div className="mt-10 text-center">로딩 중...</div>;
    }

    return (
        <div className="app-shell mt-10 max-w-4xl page-card">
            <h1 className="mb-6 text-center text-2xl font-bold">카테고리 관리</h1>

            <div className="mb-6 flex gap-4">
                <select value={type} onChange={(e) => setType(e.target.value)} className="ui-select">
                    {CATEGORY_TYPES.map((t) => (
                        <option key={t} value={t}>{t}</option>
                    ))}
                </select>
                <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="카테고리 이름"
                    className="ui-input flex-1"
                />
                <button onClick={handleAddCategory} className="btn-primary">추가</button>
            </div>

            {editCategoryId !== null && (
                <div className="mb-6 flex gap-4">
                    <select value={editType} onChange={(e) => setEditType(e.target.value)} className="ui-select">
                        {CATEGORY_TYPES.map((t) => (
                            <option key={t} value={t}>{t}</option>
                        ))}
                    </select>
                    <input
                        type="text"
                        value={editName}
                        onChange={(e) => setEditName(e.target.value)}
                        placeholder="수정할 카테고리 이름"
                        className="ui-input flex-1"
                    />
                    <button onClick={handleEditCategory} className="btn-primary">수정</button>
                    <button onClick={() => { setEditCategoryId(null); setEditType("EXERCISE"); setEditName(""); }} className="btn-secondary">취소</button>
                </div>
            )}

            <div className="table-shell">
                <table className="w-full border-collapse">
                    <thead>
                    <tr className="bg-emerald-100">
                        <th className="border border-[var(--line)] px-4 py-2">유형</th>
                        <th className="border border-[var(--line)] px-4 py-2">이름</th>
                        <th className="border border-[var(--line)] px-4 py-2">수정</th>
                        <th className="border border-[var(--line)] px-4 py-2">삭제</th>
                    </tr>
                    </thead>
                    <tbody>
                    {categories.length === 0 ? (
                        <tr><td colSpan={4} className="py-4 text-center">등록된 카테고리가 없습니다.</td></tr>
                    ) : (
                        categories.map((category) => (
                            <tr key={category.id} className="text-center">
                                <td className="border border-[var(--line)] px-4 py-2">{category.type}</td>
                                <td className="border border-[var(--line)] px-4 py-2">{category.name}</td>
                                <td className="border border-[var(--line)] px-4 py-2">
                                    <button onClick={() => { setEditCategoryId(category.id); setEditType(category.type); setEditName(category.name); }} className="rounded-lg bg-amber-500 px-3 py-1 text-white hover:bg-amber-600">수정</button>
                                </td>
                                <td className="border border-[var(--line)] px-4 py-2">
                                    <button onClick={() => handleDeleteCategory(category.id)} className="btn-danger px-3 py-1">삭제</button>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default AdminCategoriesPage;
