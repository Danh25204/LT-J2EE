package com.example.antique.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cấu hình Spring MVC.
 * Quan trọng: đăng ký resource handler để phục vụ ảnh đã upload
 * như static file qua URL /uploads/**.
 *
 * Ví dụ: file lưu tại "uploads/images/dc-001.jpg"
 *        → truy cập qua URL: http://localhost:8080/uploads/dc-001.jpg
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Đọc đường dẫn thư mục upload từ application.properties
    @Value("${app.upload.dir:uploads/images}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve đường dẫn tuyệt đối của thư mục upload
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // Map URL pattern /uploads/** → thư mục file thực tế
        // "file:" protocol = file system (không phải classpath)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }

}
