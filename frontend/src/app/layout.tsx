import "@/app/styles/tailwind.css";
//KakaoMap api사용위한 import
import Script from 'next/script';


export default function RootLayout({
                                       children,
                                   }: {
    children: React.ReactNode;
}) {
    return (
        <html lang="ko">
        <head>
            <Script
                src={`https://dapi.kakao.com/v2/maps/sdk.js?appkey=eeb7063b014c24de4bf12e2750facc65&libraries=services,clusterer&autoload=false`}
                strategy="beforeInteractive"
            />
        </head>
        <body>{children}</body>
        </html>
    );
}
