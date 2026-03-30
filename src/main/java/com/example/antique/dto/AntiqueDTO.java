package com.example.antique.dto;

import com.example.antique.entity.TinhTrang;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * DTO cho form tạo/sửa đồ cổ.
 * 
 * Tách biệt Entity và Form để:
 * - Validation chỉ ở tầng web (không ảnh hưởng entity)
 * - Xử lý upload file (MultipartFile không lưu vào DB)
 * - Linh hoạt thêm/bớt trường mà không sửa entity
 */
@Getter
@Setter
@NoArgsConstructor
public class AntiqueDTO {
    
    private Long id; // Có giá trị khi edit, null khi create
    
    // Mã đồ cổ (tự động sinh, hiển thị khi edit)
    private String maDocCo;
    
    @NotBlank(message = "Tên đồ cổ không được để trống")
    @Size(max = 200, message = "Tên đồ cổ tối đa 200 ký tự")
    private String tenDocCo;
    
    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId; // ID của category (sẽ load Category trong service)
    
    @Min(value = 1, message = "Năm sản xuất phải >= 1")
    @Max(value = 9999, message = "Năm sản xuất không hợp lệ")
    private Integer namSanXuat; // Nullable - không biết năm
    
    @Size(max = 100, message = "Triều đại tối đa 100 ký tự")
    private String trieuDai;
    
    @Size(max = 100, message = "Xuất xứ tối đa 100 ký tự")
    private String xuatXu;
    
    @Size(max = 100, message = "Chất liệu tối đa 100 ký tự")
    private String chatLieu;
    
    @Size(max = 100, message = "Kích thước tối đa 100 ký tự")
    private String kichThuoc;
    
    @NotNull(message = "Vui lòng chọn tình trạng")
    private TinhTrang tinhTrang = TinhTrang.TOT;
    
    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String moTa;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá nhập phải > 0")
    @Digits(integer = 13, fraction = 2, message = "Giá nhập không hợp lệ")
    private BigDecimal giaNhap;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán dự kiến phải > 0")
    @Digits(integer = 13, fraction = 2, message = "Giá bán không hợp lệ")
    private BigDecimal giaBanDuKien;
    
    @Min(value = 1, message = "Số lượng mặc định phải >= 1")
    @Max(value = 1000, message = "Số lượng mặc định tối đa 1000")
    private Integer soLuongMacDinh = 1;
    
    // File ảnh upload qua multipart form (dùng cho EDIT)
    private MultipartFile anhChinhFile;
    
    // Tên file ảnh đã upload qua AJAX (dùng cho CREATE)
    private String anhChinhFilename;
    
    // Tên file ảnh hiện tại (dùng khi edit, để giữ ảnh cũ nếu không upload mới)
    private String anhChinhCurrent;
}
