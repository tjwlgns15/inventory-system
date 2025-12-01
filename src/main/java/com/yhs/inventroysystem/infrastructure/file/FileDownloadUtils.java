package com.yhs.inventroysystem.infrastructure.file;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FileDownloadUtils {

    /**
     * 파일명을 브라우저 호환 형식으로 인코딩
     * - Chrome, Edge, Safari: UTF-8 인코딩
     * - IE: URLEncoding
     */
    public static String encodeFileName(String fileName, String userAgent) {
        try {
            if (userAgent != null && userAgent.contains("MSIE")) {
                // IE 브라우저
                return URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                        .replaceAll("\\+", "%20");
            } else {
                // 최신 브라우저 (Chrome, Firefox, Safari, Edge 등)
                return new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            }
        } catch (UnsupportedEncodingException e) {
            return fileName;
        }
    }

    public static String createContentDisposition(String fileName, String userAgent) {

        // UTF-8 인코딩 (RFC 5987 방식)
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        // ASCII fallback용 파일명 (ISO-8859-1 변환)
        String fallbackFileName = new String(
                fileName.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.ISO_8859_1
        );

        return String.format(
                "attachment; filename=\"%s\"; filename*=UTF-8''%s",
                fallbackFileName,
                encodedFileName
        );
    }

}