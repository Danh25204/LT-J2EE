package com.example.antique.repository;

import com.example.antique.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity Role.
 * Spring Data JPA tự sinh các câu query dựa trên tên phương thức.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Tìm role theo tên (dùng khi gán role cho user mới)
    Optional<Role> findByName(String name);

    // Kiểm tra role có tồn tại chưa (dùng khi khởi tạo dữ liệu)
    boolean existsByName(String name);

}
