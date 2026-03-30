package com.example.antique.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình bảo mật cho toàn bộ ứng dụng.
 *
 * @EnableWebSecurity: kích hoạt Spring Security cho ứng dụng Web
 * @EnableMethodSecurity: cho phép dùng @PreAuthorize trên method/class
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Bean mã hóa mật khẩu bằng BCrypt (thuật toán mạnh, có salt tự động).
     * BCrypt hash cùng 1 password sẽ ra kết quả khác nhau mỗi lần → bảo mật cao.
     * Strength = 12 (số vòng lặp, càng cao càng chậm + an toàn hơn; mặc định = 10)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Cấu hình authentication provider:
     * kết nối UserDetailsService với PasswordEncoder
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager: dùng trong các trường hợp cần xác thực thủ công
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cấu hình SecurityFilterChain - TRUNG TÂM của Spring Security:
     * Xác định URL nào cần xác thực, cấu hình form login, logout, ...
     *
     * Thứ tự phân quyền URL quan trọng: rule càng cụ thể đặt càng trước.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Cấu hình phân quyền theo URL
            .authorizeHttpRequests(auth -> auth
                // Các URL không cần đăng nhập
                .requestMatchers(
                    "/auth/**",        // trang đăng nhập
                    "/css/**",         // static files
                    "/js/**",
                    "/images/**",
                    "/uploads/**",     // ảnh đồ cổ đã upload
                    "/error/**"        // trang lỗi
                ).permitAll()
                // Chỉ ADMIN mới vào được /admin/**
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Tất cả URL còn lại đều cần đăng nhập
                .anyRequest().authenticated()
            )

            // Cấu hình form đăng nhập
            .formLogin(form -> form
                .loginPage("/auth/login")               // URL trang đăng nhập tùy chỉnh
                .loginProcessingUrl("/auth/login")      // URL Spring Security xử lý POST đăng nhập
                .defaultSuccessUrl("/dashboard", true)  // Sau khi đăng nhập thành công
                .failureUrl("/auth/login?error=true")   // Khi sai mật khẩu
                .usernameParameter("username")          // Tên field username trong form
                .passwordParameter("password")          // Tên field password trong form
                .permitAll()
            )

            // Cấu hình đăng xuất
            .logout(logout -> logout
                .logoutUrl("/auth/logout")                   // URL kích hoạt logout
                .logoutSuccessUrl("/auth/login?logout=true") // Redirect sau khi logout
                .invalidateHttpSession(true)                 // Xóa session
                .clearAuthentication(true)                   // Xóa authentication
                .deleteCookies("JSESSIONID")                 // Xóa cookie session
                .permitAll()
            )

            // Xử lý khi truy cập không có quyền
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            )

            // Gắn authentication provider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

}
