package com.example.antique.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) dùng để nhận dữ liệu từ form HTML.
 * Tách biệt với entity để:
 *   1. Có thể thêm các field form-only (confirmPassword)
 *   2. Kiểm soát validation riêng cho thao tác tạo mới vs chỉnh sửa
 *   3. Không expose trực tiếp entity ra ngoài
 */
@Getter
@Setter
@NoArgsConstructor
public class UserDTO {

    private Long id; // null khi tạo mới, có giá trị khi chỉnh sửa

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    private String username;

    // password KHÔNG có @NotBlank vì khi edit có thể để trống (giữ nguyên pass cũ)
    // Validation manual trong Controller
    private String password;

    private String confirmPassword; // Chỉ dùng trên form, không lưu DB

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    // Trạng thái tài khoản (mặc định là active khi tạo mới)
    private Boolean active = true;

    @NotBlank(message = "Vui lòng chọn vai trò cho người dùng")
    private String roleName; // "ROLE_ADMIN" hoặc "ROLE_NHAN_VIEN"

}
