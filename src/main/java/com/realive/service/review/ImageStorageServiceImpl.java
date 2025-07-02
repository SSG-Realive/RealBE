package com.realive.service.review;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageStorageServiceImpl implements ImageStorageService {

    private final String uploadDir = "C:/uploads/reviews/";

    @Override
    public String uploadFile(MultipartFile file, String filename) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ 디렉토리 생성됨: " + uploadPath.toAbsolutePath());
            }

            Path filePath = uploadPath.resolve(filename);
            System.out.println("📁 저장 시도 경로: " + filePath.toAbsolutePath());
            System.out.println("🧾 파일명: " + file.getOriginalFilename() + ", 크기: " + file.getSize());

            file.transferTo(filePath.toFile());

            // 클라이언트가 접근할 수 있는 URL 경로 반환
            return "/uploads/reviews/" + filename;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
        }
    }



    @Override
    public void deleteFile(String imageUrl) {
        try {
            // imageUrl이 절대경로나 상대경로일 수 있으니 실제 경로로 변환 필요
            // 예) /uploads/reviews/abc.jpg -> uploads/reviews/abc.jpg
            String filePathStr = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
            File file = new File(filePathStr);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }
}

