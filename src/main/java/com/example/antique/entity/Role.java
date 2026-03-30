package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity đại diện cho vai trò người dùng trong hệ thống.
 * Có 2 vai trò:
 *   - ROLE_ADMIN: Quản trị viên, có toàn quyền
 *   - ROLE_NHAN_VIEN: Nhân viên kho, chỉ thao tác nghiệp vụ kho
 *
 * Tên role PHẢI có tiền tố "ROLE_" để Spring Security nhận diện đúng.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên role: "ROLE_ADMIN" hoặc "ROLE_NHAN_VIEN"
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    // Constructor tiện lợi để khởi tạo role mới
    public Role(String name) {
        this.name = name;
    }

}
