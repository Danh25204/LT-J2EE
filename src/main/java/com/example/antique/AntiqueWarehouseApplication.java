package com.example.antique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Class khởi động ứng dụng Spring Boot chính
 * @SpringBootApplication bao gồm:
 *   - @Configuration: đánh dấu class cấu hình
 *   - @EnableAutoConfiguration: tự động cấu hình Spring
 *   - @ComponentScan: quét toàn bộ package để tìm bean
 * @EnableScheduling: kích hoạt @Scheduled cho job kiểm tra đồ cổ
 */
@SpringBootApplication
@EnableScheduling
public class AntiqueWarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntiqueWarehouseApplication.class, args);
    }

}
