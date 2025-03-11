"use client";

import React from 'react';
import MainMenu from "@/app/components/MainMenu";
import { useEffect, useState, useRef } from "react";
import { useParams, useRouter } from "next/navigation";
import { deleteGroup, getCurrentUser, getGroup, joinGroup, getVoteResult } from "@/app/api";
import KakaoMap from "@/app/components/KakaoMap";
import VoteProgressModal from "@/app/components/VoteProgressModal";
import { isMemberInGroup } from "@/app/api/member";

type Category = {
    id: number;
    type: string;
    name: string;
};

type GroupDetail = {
    id: number;
    title: string;
    author: string;
    status: string;
    memberId: number;
    description: string;
    field: Category[];
    viewCount: number;
};

export default function GroupDetailPage() {
    const { id } = useParams();
    const router = useRouter();
    const [group, setGroup] = useState<GroupDetail | null>(null);
    const [currentUser, setCurrentUser] = useState<{ username: string, id: number } | null>(null);
    const [isVoteModalOpen, setIsVoteModalOpen] = useState(false);
    const [selectedLocations, setSelectedLocations] = useState<
        { address: string; latitude: number; longitude: number }[]
    >([]);
    const [isMember, setIsMember] = useState(false);
    const [wsConnected, setWsConnected] = useState(false);

    // WebSocket을 위한 상태 관리
    const [messages, setMessages] = useState<{ sender: string, content: string }[]>([]);
    const [newMessage, setNewMessage] = useState("");
    const socketRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        async function fetchCurrentUser() {
            try {
                const currentUser = await getCurrentUser();
                setCurrentUser(currentUser.data);
                console.log("현재 사용자 정보:", currentUser.data);

                // 멤버 여부 확인
                const memberId = currentUser.data.id;  // 현재 로그인한 사용자의 ID

                const memberCheck = await isMemberInGroup(Number(id), Number(memberId));  // groupId와 memberId 함께 전달
                setIsMember(memberCheck);
            } catch (error) {
                console.error("현재 사용자 정보를 불러오는 중 오류 발생:", error);
                // 로그인 안된 상태에서는 카카오 로그인 페이지로 리디렉션
                window.location.href = "http://localhost:8080/auth/kakao/login";
            }
        }

        fetchCurrentUser();
    }, []);

    const handleDelete = async () => {
        if (group) {
            try {
                await deleteGroup(group.id);
                alert("그룹이 삭제되었습니다.");
                window.location.href = "/";
            } catch (error) {
                console.error("그룹 삭제 중 오류 발생:", error);
                alert("그룹 삭제 중 오류가 발생했습니다.");
            }
        }
    }

    const handleJoin = async () => {
        if (!group) {
            alert("유효한 그룹이 없습니다.")
            return;
        }
        try {
            await joinGroup(group.id);
            alert("그룹 참가 성공!");
        } catch (error: any) {
            const errorMessage = error?.response?.data?.message || "그룹 참가에 실패했습니다.";
            alert(errorMessage);
        }
    }

    useEffect(() => {
        async function fetchGroup() {
            try {
                if (id) {
                    const groupData = await getGroup(Number(id));
                    console.log("API 응답 데이터:", groupData); // ID를 숫자로 변환해서 전달
                    setGroup({
                        ...groupData,
                        field: Array.isArray(groupData.category)
                            ? groupData.category
                            : [groupData.category],
                        viewCount: groupData.viewCount || 0,
                    });
                }
            } catch (error) {
                console.error("그룹 상세 정보를 불러오는 중 오류 발생:", error);
            }
        }

        async function fetchCurrentUser() {
            try {
                const currentUser = await getCurrentUser();
                setCurrentUser(currentUser.data);
                console.log("현재 사용자 정보:", currentUser.data);
            } catch (error) {
                console.error("현재 사용자 정보를 불러오는 중 오류 발생:", error);
            }
        }

        async function fetchVoteResult() {
            try {
                const result = await getVoteResult(Number(id));
                if (result && result.mostVotedLocations) {
                    setSelectedLocations(result.mostVotedLocations); // 여러 위치로 설정
                }
            } catch (error) {
                console.error(error);
            }
        }

        if (id) {
            fetchGroup();
            fetchCurrentUser();
            fetchVoteResult();
        }
    }, [id]);

    // WebSocket 연결 설정
    useEffect(() => {
        // 그룹과 현재 사용자 정보가 모두 로드되었는지 확인
        if (!isMember || !id || !currentUser) return;

        // console.log(`WebSocket 연결 시도: ws://localhost:8080/chat/${id}`);
        console.log(`WebSocket 연결 시도: ws://localhost:8080/ws/chat`);

        // 채팅방 생성 확인 또는 생성 요청 (필요한 경우)
        // const socket = new WebSocket(`ws://localhost:8080/chat/${id}`);
        const socket = new WebSocket(`ws://localhost:8080/ws/chat`);
        socketRef.current = socket;

        socket.onopen = () => {
            console.log("WebSocket 연결됨");
            setWsConnected(true);
        };
    
        socket.onclose = () => {
            console.log("WebSocket 연결 끊김");
            setWsConnected(false);
        };

        socket.onmessage = (event) => {
            try {
                const receivedMessage = JSON.parse(event.data);
                setMessages((prevMessages) => [...prevMessages, receivedMessage]);
            } catch (e) {
                console.error("메시지 파싱 오류:", e);
            }
        };

        return () => {
            socket.close();
        };
    }, [isMember, id, currentUser]);

    // 메시지 전송 함수를 컴포넌트 내부에서 정의
    const sendMessage = () => {
        if (socketRef.current && newMessage.trim()) {
            const messageData = { sender: currentUser?.username || "익명", content: newMessage };
            socketRef.current.send(JSON.stringify(messageData));
            setNewMessage("");
        }
    };

    if (!group) return <p className="text-center text-gray-500">로딩 중...</p>;

    const isGroupOwner = currentUser && currentUser.id && group.memberId === currentUser.id;
    console.log("그룹 소유자 여부:", isGroupOwner);

    return (
        <div className="min-h-screen bg-gray-50">
            <MainMenu />
            <div className="min-h-screen bg-gray-100">
                <main className="max-w-4xl mx-auto bg-white p-8 mt-10 rounded-lg shadow-lg">
                    {/* 제목 */}
                    <h2 className="text-4xl font-extrabold">{group.title}</h2>
                    <div className="flex justify-between items-center mt-4">
                        <span className="text-gray-700 font-bold">{group.author}</span>
                        <span
                            className={`ml-2 px-2 py-1 text-sm rounded-full ${group.status === "RECRUITING"
                                ? "bg-green-500 text-white"
                                : "bg-red-500 text-white"
                                }`}
                        >
                            {group.status}
                        </span>
                    </div>
                    {/* 상태 */}
                    <hr className="my-4 border-gray-300" />

                    {/* 모집 정보 */}
                    <div className="mt-4 space-y-4">
                        {/* 모집 구분 */}
                        <div className="flex items-center space-x-2">
                            <span className="font-semibold text-gray-900">모집 구분</span>
                            <div className="flex flex-wrap gap-2">
                                {group.field.map((field) => (
                                    <span
                                        key={field.id}
                                        className="px-3 py-1 text-sm font-medium bg-blue-200 text-blue-700 rounded-full"
                                    >
                                        {field.type}
                                    </span>
                                ))}
                            </div>
                        </div>

                        {/* 모집 분야 */}
                        <div className="flex items-center space-x-2">
                            <span className="font-semibold text-gray-900">모집 분야</span>
                            <div className="flex flex-wrap gap-2">
                                {group.field.map((field) => (
                                    <span
                                        key={field.id}
                                        className="px-3 py-1 text-sm font-medium border border-gray-400 text-gray-700 rounded-full"
                                    >
                                        {field.name}
                                    </span>
                                ))}
                            </div>
                        </div>
                    </div>

                    {/* 조회수 표시 */}
                    <div className="flex items-center space-x-2 mt-4">
                        <span className="font-semibold text-gray-900">조회수</span>
                        <span className="text-gray-700">{group.viewCount}회</span> {/* 조회수 출력 */}
                    </div>

                    {/* 프로젝트 설명 */}
                    <div className="mt-6">
                        <span className="text-gray-700 font-semibold">프로젝트 내용</span>
                        <p className="mt-2 text-gray-800 leading-relaxed">
                            {group.description}
                        </p>
                    </div>
                    {/* 장소 투표 */}
                    <div className="mt-8">
                        <h3 className="text-lg font-semibold">장소 투표</h3>

                        {group.status === "RECRUITING" && (
                            <button
                                className="bg-green-500 hover:bg-green-600 text-white font-medium px-4 py-2 rounded-lg mt-3"
                                onClick={() => setIsVoteModalOpen(true)} // ✅ 모달 열기
                            >
                                투표 참가
                            </button>
                        )}

                        {group.status === "COMPLETED" && (
                            <>
                                {/* 지도 영역 */}
                                <KakaoMap
                                    onLocationSelect={(location) => console.log(location)}
                                    selectedLocations={selectedLocations}
                                />
                                <p className="mt-2 text-gray-700">최종 투표 결과</p>
                            </>
                        )}
                    </div>

                    {/* 하단 버튼 */}
                    <div className="flex justify-end mt-8 space-x-4">
                        {isGroupOwner && (
                            <>
                                <button
                                    className="bg-yellow-500 hover:bg-yellow-600 text-white font-medium px-6 py-2 rounded-lg"
                                    onClick={() => router.push(`/groups/update/${id}`)}
                                >
                                    수정
                                </button>
                                <button
                                    onClick={handleDelete}
                                    className="bg-red-500 hover:bg-red-600 text-white font-medium px-6 py-2 rounded-lg">
                                    삭제
                                </button>
                            </>
                        )}
                        <button onClick={() => router.back()}
                            className="bg-gray-300 hover:bg-gray-400 text-gray-700 font-medium px-6 py-2 rounded-lg">
                            돌아가기
                        </button>
                        <button
                            onClick={handleJoin}
                            className="bg-gray-800 hover:bg-black text-white font-medium px-6 py-2 rounded-lg">
                            참가하기
                        </button>
                    </div>

                    {/* 채팅 UI 추가 */}
                    {isMember && (
                        <div className="mt-8">
                            <h3 className="text-lg font-semibold">그룹 채팅</h3>
                            <div className="bg-gray-100 p-4 h-64 overflow-y-auto rounded-lg">
                                {messages.map((msg, index) => (
                                    <div key={index} className="mb-2">
                                        <span className="font-bold">{msg.sender}:</span> {msg.content}
                                    </div>
                                ))}
                            </div>
                            <div className="mt-4 flex">
                                <input
                                    type="text"
                                    value={newMessage}
                                    onChange={(e) => setNewMessage(e.target.value)}
                                    onKeyPress={(e) => {
                                        if (e.key === 'Enter') {
                                            sendMessage();
                                        }
                                    }}
                                    className="flex-1 border p-2 rounded-lg"
                                    placeholder="메시지를 입력하세요..."
                                />
                                <button
                                    onClick={sendMessage}
                                    className="ml-2 bg-green-500 text-white px-4 py-2 rounded-lg"
                                >
                                    전송
                                </button>
                            </div>
                        </div>
                    )}
                </main>
            </div>
            {isVoteModalOpen && <VoteProgressModal groupId={group.id} onClose={() => setIsVoteModalOpen(false)} />}
        </div>
    );
}
