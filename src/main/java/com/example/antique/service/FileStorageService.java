package com.example.antique.service;

import com.example.antique.config.FileStorageProperties;
import com.example.antique.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service xử lý upload, lưu trữ và xóa file ảnh.
 * 
 * Luồng hoạt động:
 * 1. Nhận file từ MultipartFile (form upload)
 * 2. Validate: kiểm tra loại file (jpg, png, jpeg), kích thước
 * 3. Tạo tên file unique (UUID + extension gốc)
 * 4. Lưu vào thư mục uploads/images/
 * 5. Trả về tên file đã lưu (để lưu vào DB)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private Path fileStorageLocation;

    /**
     * Khởi tạo thư mục lưu file khi service được tạo.
     * @PostConstruct: chạy sau khi DI hoàn tất
     */
    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getDir())
                .toAbsolutePath()
                .normalize();
        
        try {
            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(this.fileStorageLocation);
            log.info("✓ Thư mục upload đã sẵn sàng: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Không thể tạo thư mục upload!", ex);
        }
    }

    /**
     * Lưu file ảnh và trả về tên file đã lưu.
     * 
     * @param file MultipartFile từ form upload
     * @return Tên file đã lưu (uuid.jpg) - lưu vào DB
     * @throws FileStorageException nếu có lỗi
     */
    public String storeFile(MultipartFile file) {
        // Validate file không rỗng
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File upload rỗng!");
        }

        // Lấy tên file gốc và validate
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new FileStorageException("Tên file không hợp lệ!");
        }
        originalFilename = StringUtils.cleanPath(originalFilename);
        
        // Validate extension (chỉ cho phép ảnh)
        String extension = getFileExtension(originalFilename);
        if (!isValidImageExtension(extension)) {
            throw new FileStorageException(
                "Chỉ chấp nhận file ảnh (jpg, jpeg, png). File của bạn: " + extension
            );
        }

        // Tạo tên file unique: uuid + extension gốc
        // VD: 123e4567-e89b-12d3-a456-426614174000.jpg
        String fileName = UUID.randomUUID().toString() + "." + extension;

        try {
            // Kiểm tra tên file không chứa ký tự đặc biệt nguy hiểm
            if (fileName.contains("..")) {
                throw new FileStorageException(
                    "Tên file không hợp lệ: " + fileName
                );
            }

            // Đường dẫn đích để lưu file
            Path targetLocation = this.fileStorageLocation.resolve(fileName);

            // Copy file vào thư mục đích
            // REPLACE_EXISTING: ghi đè nếu file đã tồn tại (khó xảy ra với UUID)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("✓ Đã lưu file: {} (size: {} bytes)", fileName, file.getSize());
            return fileName;

        } catch (IOException ex) {
            throw new FileStorageException("Lỗi khi lưu file: " + fileName, ex);
        }
    }

    /**
     * Xóa file ảnh (khi xóa đồ cổ hoặc update ảnh mới).
     * 
     * @param fileName Tên file cần xóa
     * @return true nếu xóa thành công, false nếu file không tồn tại
     */
    public boolean deleteFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                log.info("✓ Đã xóa file: {}", fileName);
            } else {
                log.warn("⚠ File không tồn tại: {}", fileName);
            }
            
            return deleted;
        } catch (IOException ex) {
            log.error("✗ Lỗi khi xóa file: {}", fileName, ex);
            return false;
        }
    }

    /**
     * Lấy extension từ tên file.
     * VD: "anh.jpg" -> "jpg"
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Kiểm tra extension có phải là ảnh hợp lệ không.
     */
    private boolean isValidImageExtension(String extension) {
        return extension.equals("jpg") || 
               extension.equals("jpeg") || 
               extension.equals("png");
    }

    /**
     * Lấy đường dẫn tuyệt đối của thư mục upload.
     * Dùng để serve static files qua WebMvcConfig.
     */
    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}
