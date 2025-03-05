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

        // 카카오맵 로드
        window.kakao.maps.load(() => {
            const options = {
                center: new window.kakao.maps.LatLng(37.566826, 126.978656),
                level: 3
            };

            const map = new window.kakao.maps.Map(mapRef.current, options);
            const geocoder = new window.kakao.maps.services.Geocoder();

            const markers = [];

            // 여러 마커를 표시
            if (selectedLocations && selectedLocations.length > 0) {
                // 위도, 경도의 합을 저장할 변수 초기화
                let sumLat = 0;
                let sumLng = 0;

                selectedLocations.forEach((location) => {
                    const position = new window.kakao.maps.LatLng(location.latitude, location.longitude);
                    const marker = new window.kakao.maps.Marker({
                        position,
                        map
                    });

                    markers.push(marker);

                    // 위도, 경도 합계 계산
                    sumLat += location.latitude;
                    sumLng += location.longitude;
                });

                // 평균 위도, 경도 계산 (중심점)
                const centerLat = sumLat / selectedLocations.length;
                const centerLng = sumLng / selectedLocations.length;

                // 계산된 중심점으로 지도 중심 설정
                const centerPosition = new window.kakao.maps.LatLng(centerLat, centerLng);
                map.setCenter(centerPosition);

                // 마커가 여러 개일 경우 조금 더 넓게 보여주기 (레벨 값이 클수록 멀리서 보임)
                if (selectedLocations.length > 1) {
                    map.setLevel(6); // 레벨은 상황에 맞게 조정
                } else {
                    map.setLevel(3); // 마커가 하나일 경우 더 가깝게
                }
            }


            window.kakao.maps.event.addListener(map, 'click', (mouseEvent: any) => {
                const latlng = mouseEvent.latLng;

                geocoder.coord2Address(
                    latlng.getLng(),
                    latlng.getLat(),
                    (result, status) => {
                        if (status === window.kakao.maps.services.Status.OK && result[0]) {
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