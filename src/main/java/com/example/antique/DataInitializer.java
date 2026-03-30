package com.example.antique;

import com.example.antique.entity.Role;
import com.example.antique.entity.User;
import com.example.antique.repository.RoleRepository;
import com.example.antique.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Khởi tạo dữ liệu mặc định khi ứng dụng khởi động lần đầu.
 * CommandLineRunner: interface của Spring Boot, run() được gọi sau khi
 * ApplicationContext đã sẵn sàng (tức là sau khi Hibernate đã tạo xong bảng).
 *
 * Dữ liệu được tạo:
 *   - 2 Role: ROLE_ADMIN, ROLE_NHAN_VIEN
 *   - 2 User mặc định: admin / nhanvien1
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initUsers();
    }

    /**
     * Tạo dữ liệu Role nếu chưa tồn tại.
     * existsByName() để tránh tạo duplicate khi restart ứng dụng.
     */
    private void initRoles() {
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            roleRepository.save(new Role("ROLE_ADMIN"));
            log.info(">>> [DataInitializer] Tạo role: ROLE_ADMIN");
        }
        if (!roleRepository.existsByName("ROLE_NHAN_VIEN")) {
            roleRepository.save(new Role("ROLE_NHAN_VIEN"));
            log.info(">>> [DataInitializer] Tạo role: ROLE_NHAN_VIEN");
        }
    }

    /**
     * Tạo tài khoản mặc định nếu chưa có user nào trong DB.
     */
    private void initUsers() {
        if (userRepository.existsByUsername("admin")) {
            return; // Đã có dữ liệu, bỏ qua
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ROLE_ADMIN"));
        Role staffRole = roleRepository.findByName("ROLE_NHAN_VIEN")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ROLE_NHAN_VIEN"));

        // Tạo tài khoản Admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123")); // Mã hóa BCrypt
        admin.setFullName("Quản trị viên");
        admin.setEmail("admin@antiquewh.com");
        admin.setPhone("0901234567");
        admin.setActive(true);
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
        log.info(">>> [DataInitializer] Tạo tài khoản admin (admin/admin123)");

        // Tạo tài khoản Nhân viên mẫu
        User staff = new User();
        staff.setUsername("nhanvien1");
        staff.setPassword(passwordEncoder.encode("123456"));
        staff.setFullName("Nguyễn Văn Kho");
        staff.setEmail("nhanvien@antiquewh.com");
        staff.setPhone("0912345678");
        staff.setActive(true);
        staff.getRoles().add(staffRole);
        userRepository.save(staff);
        log.info(">>> [DataInitializer] Tạo tài khoản nhanvien1 (nhanvien1/123456)");
    }

}
