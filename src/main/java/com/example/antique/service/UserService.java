package com.example.antique.service;

import com.example.antique.dto.UserDTO;
import com.example.antique.entity.Role;
import com.example.antique.entity.User;
import com.example.antique.repository.RoleRepository;
import com.example.antique.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer cho User - chứa toàn bộ business logic.
 * @Transactional ở class level: tất cả method đều được bao bởi 1 transaction.
 * Nếu method nào ném exception → Spring tự ROLLBACK toàn bộ thay đổi.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== READ OPERATIONS (không cần Transactional write) =====

    /**
     * Lấy tất cả người dùng, sắp xếp từ mới nhất → cũ nhất
     */
    @Transactional(readOnly = true) // readOnly = true: Spring tối ưu hóa truy vấn đọc
    public List<User> findAll() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Tìm user theo ID, ném lỗi nếu không tồn tại
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
    }

    /**
     * Kiểm tra username đã tồn tại chưa
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Đếm tổng số người dùng (dùng cho dashboard)
     */
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    // ===== WRITE OPERATIONS =====

    /**
     * Tạo mới người dùng từ UserDTO.
     * Quy trình:
     *   1. Kiểm tra username chưa bị trùng
     *   2. Mã hóa password bằng BCrypt
     *   3. Tìm và gán Role
     *   4. Lưu vào DB
     */
    public User createUser(UserDTO dto) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập '" + dto.getUsername() + "' đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(dto.getUsername());

        // Mã hóa password TRƯỚC KHI lưu - KHÔNG BAO GIỜ lưu password raw
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setActive(dto.getActive() != null ? dto.getActive() : true);

        // Tìm Role trong DB và gán cho user
        Role role = roleRepository.findByName(dto.getRoleName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + dto.getRoleName()));
        user.getRoles().add(role);

        return userRepository.save(user);
    }

    /**
     * Cập nhật thông tin người dùng.
     * Password chỉ được cập nhật nếu người dùng nhập mật khẩu mới.
     */
    public User updateUser(Long id, UserDTO dto) {
        User user = findById(id);

        // Kiểm tra username nếu đã thay đổi
        if (!user.getUsername().equals(dto.getUsername()) &&
                userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập '" + dto.getUsername() + "' đã được sử dụng!");
        }

        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setActive(dto.getActive() != null ? dto.getActive() : true);

        // CHỈ cập nhật password nếu có nhập mật khẩu mới
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        // Cập nhật role: xóa role cũ, gán role mới
        user.getRoles().clear();
        Role role = roleRepository.findByName(dto.getRoleName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + dto.getRoleName()));
        user.getRoles().add(role);

        return userRepository.save(user);
    }

    /**
     * Bật/tắt trạng thái active của người dùng.
     * Thay vì xóa cứng, ta chỉ đặt active = false (xóa mềm).
     */
    public void toggleActive(Long id) {
        User user = findById(id);
        user.setActive(!user.getActive());
        userRepository.save(user);
    }

    /**
     * Vô hiệu hóa tài khoản (soft delete - không xóa DB record).
     * Nên dùng soft delete vì user có thể liên kết với phiếu nhập/xuất.
     */
    public void deleteUser(Long id) {
        User user = findById(id);
        user.setActive(false);
        userRepository.save(user);
    }

}
