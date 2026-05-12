import { useState } from 'react';
import KakaoMap from './KakaoMap';
import { createVote } from '@/app/api/vote';

interface VoteLocation {
    location: string;
    address: string;
    latitude: number;
    longitude: number;
}

interface VoteModalProps {
    isOpen: boolean;
    onClose: () => void;
    groupId: number;
    onVoteCreated: (location: VoteLocation) => void;
}

export default function VoteModal({ isOpen, onClose, groupId, onVoteCreated }: VoteModalProps) {
    const [location, setLocation] = useState<string>("");
    const [selectedLocation, setSelectedLocation] = useState<Omit<VoteLocation, 'location'> | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string>("");

    const handleLocationSelect = (locationData: Omit<VoteLocation, 'location'>) => {
        setSelectedLocation(locationData);
        setError("");
    };

    const handleSubmit = async () => {
        try {
            if (!location.trim()) {
                setError("장소명을 입력해주세요.");
                return;
            }
            if (!selectedLocation) {
                setError("지도에서 위치를 선택해주세요.");
                return;
            }

            setLoading(true);

            const voteLocation: VoteLocation = {
                location: location.trim(),
                address: selectedLocation.address,
                latitude: selectedLocation.latitude,
                longitude: selectedLocation.longitude
            };
            //
            // await createVote(groupId, {
            //     location: location.trim(),
            //     address: selectedLocation.address,
            //     latitude: selectedLocation.latitude,
            //     longitude: selectedLocation.longitude
            // });

            onVoteCreated(voteLocation);
            handleClose();
        } catch (error) {
            setError("투표 장소 생성 중 오류가 발생했습니다.");
            console.error("투표 생성 오류:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setLocation("");
        setSelectedLocation(null);
        setError("");
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/35 p-4 backdrop-blur-sm">
            <div className="page-card w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-xl font-bold text-[var(--text-main)]">투표 장소 추가</h3>
                    <button
                        onClick={handleClose}
                        className="text-[var(--text-soft)] hover:text-[var(--accent-strong)]"
                    >
                        ✕
                    </button>
                </div>

                <div className="mb-4">
                    <label className="mb-2 block font-medium text-[var(--text-soft)]">
                        장소명 *
                    </label>
                    <input
                        type="text"
                        value={location}
                        onChange={(e) => setLocation(e.target.value)}
                        className="ui-input"
                        placeholder="예) 스타벅스 강남점"
                    />
                </div>

                <div className="mb-4">
                    <label className="mb-2 block font-medium text-[var(--text-soft)]">
                        위치 선택 *
                    </label>
                    <div className="h-96 overflow-hidden rounded-xl border border-[var(--line)]">
                        <KakaoMap onLocationSelect={handleLocationSelect} />
                    </div>
                </div>

                <div className="mb-6">
                    <label className="mb-2 block font-medium text-[var(--text-soft)]">
                        선택된 주소
                    </label>
                    <input
                        type="text"
                        value={selectedLocation?.address || ''}
                        readOnly
                        className="ui-input bg-emerald-50"
                        placeholder="지도에서 위치를 선택하세요"
                    />
                </div>

                {error && (
                    <div className="mb-4 text-red-500 text-sm">
                        {error}
                    </div>
                )}

                <div className="flex justify-end gap-2">
                    <button onClick={handleClose} className="btn-secondary" disabled={loading}>
                        취소
                    </button>
                    <button onClick={handleSubmit} className="btn-primary" disabled={loading}>
                        {loading ? '처리 중...' : '추가하기'}
                    </button>
                </div>
            </div>
        </div>
    );
}
