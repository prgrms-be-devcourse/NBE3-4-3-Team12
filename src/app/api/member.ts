import { api } from "./axiosInstance"; // axios 인스턴스 import

// 프로필 수정
export async function updateUserProfile(requestData: any) {
    try {
        const response = await api.put("/members", requestData);
        return response.data;
    } catch (error) {
        console.error("프로필 수정 중 에러 발생:", error);
        throw error;
    }
}

export async function isMemberInGroup(groupId: number, memberId: number): Promise<boolean> {
    // 백엔드 서버 주소 포함
    const apiUrl = `http://localhost:8080/api/groups/${groupId}/isMember?memberId=${memberId}`;
    console.log(`🔍 API 요청: ${apiUrl}`);

    try {
        const response = await fetch(apiUrl);

        if (!response.ok) {
            throw new Error(`멤버 조회 실패 (HTTP ${response.status})`);
        }

        const data = await response.json();
        console.log(`응답 데이터:`, data);

        return data;
    } catch (error) {
        console.error("그룹 멤버 확인 중 오류 발생:", error);
        return false;
    }
}