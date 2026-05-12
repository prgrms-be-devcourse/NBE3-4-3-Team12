// components/KakaoMap.tsx
'use client';

import { useEffect, useRef } from 'react';

interface KakaoMapProps {
    onLocationSelect: (location: {
        address: string;
        latitude: number;
        longitude: number;
    }) => void;
    selectedLocations?:{address:string; latitude:number; longitude:number;}[];
}

export default function KakaoMap({ onLocationSelect, selectedLocations }: KakaoMapProps) {
    const mapRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!mapRef.current) return;
        if (typeof window === "undefined") return;

        const kakao = window.kakao;
        if (!kakao || !kakao.maps) {
            // SDK가 아직 로드되지 않은 경우 현재 렌더 사이클에서는 초기화하지 않음
            return;
        }

        // 카카오맵 로드
        kakao.maps.load(() => {
            const options = {
                center: new kakao.maps.LatLng(37.566826, 126.978656),
                level: 3
            };

            const map = new kakao.maps.Map(mapRef.current, options);
            const geocoder = new kakao.maps.services.Geocoder();

            const markers = [];

            // 여러 마커를 표시
            if (selectedLocations && selectedLocations.length > 0) {
                // 바운드 객체 생성
                const bounds = new kakao.maps.LatLngBounds();

                selectedLocations.forEach((location) => {
                    const position = new kakao.maps.LatLng(location.latitude, location.longitude);
                    const marker = new kakao.maps.Marker({
                        position,
                        map
                    });

                    markers.push(marker);

                    // 바운드에 마커 위치 추가
                    bounds.extend(position);
                });

                // 모든 마커가 보이도록 지도 범위 설정
                map.setBounds(bounds);

                // 최소/최대 레벨 제한 적용 (선택사항)
                // 마커가 너무 가까우면 일정 수준 이상 확대되지 않도록
                if (map.getLevel() < 3) {
                    map.setLevel(3);
                }
                // 마커가 너무 멀면 일정 수준 이상 축소되지 않도록
                else if (map.getLevel() > 10) {
                    map.setLevel(10);
                }
            }


            kakao.maps.event.addListener(map, 'click', (mouseEvent: any) => {
                const latlng = mouseEvent.latLng;

                geocoder.coord2Address(
                    latlng.getLng(),
                    latlng.getLat(),
                    (result, status) => {
                        if (status === kakao.maps.services.Status.OK && result[0]) {
                            const address = result[0].address.address_name;
                            onLocationSelect({
                                address,
                                latitude: latlng.getLat(),
                                longitude: latlng.getLng()
                            });
                        }
                    }
                );
            });
        });
    }, [onLocationSelect, selectedLocations]);

    return <div ref={mapRef} className="w-full h-[400px] rounded-lg" />;
}
