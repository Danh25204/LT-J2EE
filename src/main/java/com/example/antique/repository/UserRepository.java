package com.example.antique.repository;

import com.example.antique.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity User.
 * Kế thừa JpaRepository để có sẵn: findAll, findById, save, delete,...
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm user theo username (dùng trong Spring Security loadUserByUsername)
    Optional<User> findByUsername(String username);

    // Kiểm tra username đã tồn tại chưa (validate khi tạo mới)
    boolean existsByUsername(String username);

    // Kiểm tra email đã tồn tại chưa (validate khi tạo mới)
    boolean existsByEmail(String email);

    // Tìm tất cả user đang active
    List<User> findByActiveTrue();

    // Tìm tất cả user theo trạng thái, sắp xếp theo tên đăng nhập
    List<User> findAllByOrderByCreatedAtDesc();

}
