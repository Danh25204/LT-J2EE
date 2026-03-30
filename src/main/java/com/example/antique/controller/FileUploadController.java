package com.example.antique.controller;

import com.example.antique.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller cho upload file qua AJAX.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Upload ảnh qua AJAX (trước khi submit form).
     * Return tên file đã lưu.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("→ POST /api/files/upload: {}", file.getOriginalFilename());

        try {
            // Validate
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File không được để trống"));
            }

            // Upload
            String filename = fileStorageService.storeFile(file);
            
            log.info("✓ Uploaded file: {} → {}", file.getOriginalFilename(), filename);

            // Return JSON
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", filename);
            response.put("url", "/uploads/images/" + filename);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("✗ Upload failed", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xóa ảnh đã upload (nếu user cancel).
     */
    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        log.info("→ DELETE /api/files/delete/{}", filename);

        try {
            fileStorageService.deleteFile(filename);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("✗ Delete failed", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
