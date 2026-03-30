package com.example.antique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Class khởi động ứng dụng Spring Boot chính
 * @SpringBootApplication bao gồm:
 *   - @Configuration: đánh dấu class cấu hình
 *   - @EnableAutoConfiguration: tự động cấu hình Spring
 *   - @ComponentScan: quét toàn bộ package để tìm bean
 */
@SpringBootApplication
public class AntiqueWarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntiqueWarehouseApplication.class, args);
    }

}
