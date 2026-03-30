package com.example.antique.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties cho file storage, đọc từ application.properties.
 * 
 * Đọc các property có prefix "app.upload"
 */
@Configuration
@ConfigurationProperties(prefix = "app.upload")
@Getter
@Setter
public class FileStorageProperties {
    
    /**
     * Thư mục lưu file upload.
     * Mapping với: app.upload.dir trong application.properties
     */
    private String dir = "uploads/images";
}
