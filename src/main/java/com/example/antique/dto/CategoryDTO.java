package com.example.antique.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO cho form tạo mới / chỉnh sửa danh mục đồ cổ.
 */
@Getter
@Setter
@NoArgsConstructor
public class CategoryDTO {

    private Long id; // null khi tạo mới

    /**
     * Mã loại: chỉ cho phép chữ cái IN HOA và số, không dấu, 2-20 ký tự.
     * VD: GOM, GO, KIM_LOAI, TRANH
     */
    @NotBlank(message = "Mã loại không được để trống")
    @Size(min = 2, max = 20, message = "Mã loại phải từ 2-20 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$",
             message = "Mã loại chỉ được chứa chữ HOA, số và dấu gạch dưới")
    private String maLoai;

    @NotBlank(message = "Tên loại không được để trống")
    @Size(min = 2, max = 100, message = "Tên loại phải từ 2-100 ký tự")
    private String tenLoai;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String moTa;

}
