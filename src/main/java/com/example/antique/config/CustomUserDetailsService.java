package com.example.antique.config;

import com.example.antique.entity.User;
import com.example.antique.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Cầu nối giữa Spring Security và database users.
 * Spring Security gọi loadUserByUsername() khi người dùng đăng nhập.
 * Class này tìm user trong DB và convert sang định dạng UserDetails
 * mà Spring Security hiểu được.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Được gọi tự động bởi Spring Security khi người dùng submit form đăng nhập.
     * @param username tên đăng nhập từ form
     * @return UserDetails chứa thông tin xác thực
     * @throws UsernameNotFoundException nếu không tìm thấy hoặc tài khoản bị vô hiệu
     */
    @Override
    @Transactional(readOnly = true) // chỉ đọc DB, không ghi → tối ưu performance
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Tìm user trong database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy người dùng: " + username));

        // Kiểm tra tài khoản bị vô hiệu hóa
        if (!user.getActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + username);
        }

        // Chuyển Set<Role> của entity thành Collection<GrantedAuthority> của Spring Security
        // GrantedAuthority = đơn vị quyền mà Spring Security nhận diện
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        // Trả về UserDetails (Spring Security dùng object này để xác thực)
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),   // đã được BCrypt hash
                authorities
        );
    }

}
