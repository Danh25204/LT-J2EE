package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity đại diện cho người dùng hệ thống.
 * Quan hệ Many-to-Many với Role (một user có thể có nhiều role,
 * nhưng trong đồ án này ta chỉ gán 1 role/user để đơn giản).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên đăng nhập - phải unique, không được rỗng
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    // Mật khẩu đã được mã hóa bằng BCrypt
    @Column(nullable = false, length = 255)
    private String password;

    // Họ tên đầy đủ
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    // Trạng thái tài khoản (true = đang hoạt động, false = bị vô hiệu)
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    // Thời điểm tạo tài khoản - tự động điền bởi Hibernate
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Quan hệ Many-to-Many với Role.
     * FetchType.EAGER: load roles ngay khi load user
     * → Cần thiết vì Spring Security cần đọc roles khi xác thực.
     *
     * Bảng trung gian: user_roles (user_id, role_id)
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Phương thức tiện ích: lấy tên role đầu tiên (hiển thị trên UI)
     */
    public String getPrimaryRoleName() {
        return roles.stream()
                .map(Role::getName)
                .findFirst()
                .orElse("Không xác định");
    }

}
