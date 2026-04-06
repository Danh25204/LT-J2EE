# Website Quản Lý Kho Đồ Cổ (Antique Warehouse Management)

## Giới thiệu

Website quản lý kho đồ cổ được xây dựng bằng Spring Boot, cho phép quản lý toàn bộ vòng đời của đồ cổ trong kho: nhập kho, xuất kho (bán / cho mượn / điều chuyển), theo dõi tồn kho, lịch sử hoạt động và kiểm tra tình trạng đồ cổ.

## Công nghệ sử dụng

- **Backend:** Spring Boot 3.3.13, Spring Security, Spring Data JPA
- **Frontend:** Thymeleaf, Bootstrap 5, Bootstrap Icons
- **Database:** MySQL 8 (utf8mb4)
- **Build tool:** Maven
- **Java:** 21+

## Chức năng chính

### Nghiệp vụ kho
- **Quản lý danh mục:** Thêm, sửa, xóa danh mục đồ cổ
- **Quản lý đồ cổ:** Thêm, sửa, xóa đồ cổ kèm upload hình ảnh
- **Nhập kho:** Tạo phiếu nhập, xem chi tiết, hủy phiếu (tự động trừ lại tồn kho)
- **Xuất kho:** Tạo phiếu xuất theo 4 lý do — Bán / Cho mượn / Điều chuyển / Khác
  - Phiếu **Cho mượn**: nút "Ghi nhận trả hàng" riêng biệt, tự động tạo yêu cầu kiểm tra tình trạng khi trả
  - Phiếu khác: nút "Hủy phiếu" hoàn lại tồn kho
- **Tồn kho:** Theo dõi số lượng tồn theo từng đồ cổ
- **Kiểm tra đồ cổ:** Quản lý yêu cầu kiểm tra tình trạng (chờ / đã kiểm tra / bỏ qua)
- **Lịch sử hoạt động:** Ghi log toàn bộ sự kiện kho với phân loại màu sắc

### Hệ thống
- **Dashboard:** Thống kê tổng quan — số đồ cổ, phiếu nhập/xuất tháng này (chỉ tính phiếu còn hiệu lực), tổng giá trị kho, cảnh báo kiểm tra và đồ cổ đang cho mượn
- **Quản lý người dùng:** Phân quyền Admin / Nhân viên
- **Bảo mật:** Spring Security, CSRF protection, xác thực form

## Cài đặt

### Yêu cầu

- JDK 21 trở lên
- MySQL 8 (hoặc XAMPP)
- Maven 3.8+

### Bước 1: Tạo database

```sql
CREATE DATABASE antique_warehouse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 2: Cấu hình database

Chỉnh sửa file `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/antique_warehouse
spring.datasource.username=root
spring.datasource.password=your_password
```

### Bước 3: Chạy ứng dụng

```bash
./mvnw spring-boot:run
```

Truy cập: http://localhost:8080

### Bước 4: Nạp dữ liệu mẫu (tuỳ chọn)

```bash
mysql --default-character-set=utf8mb4 -u root antique_warehouse < seed_data.sql
```

Script `seed_data.sql` là **idempotent** — có thể chạy nhiều lần mà không bị lỗi trùng dữ liệu.

### Tài khoản mặc định

| Tài khoản | Mật khẩu    | Quyền     |
|-----------|-------------|-----------|
| admin     | admin123    | Admin     |
| nhanvien1 | nhanvien123 | Nhân viên |

> Tài khoản được tạo tự động bởi `DataInitializer` khi khởi động lần đầu.

## Cấu trúc dự án

```
src/main/java/com/example/antique/
├── config/          # Security, MVC, UserDetailsService
├── controller/      # Request handlers (8 controllers)
├── dto/             # Data Transfer Objects
├── entity/          # JPA Entities + Enums
├── repository/      # Spring Data JPA Repositories
└── service/         # Business logic

src/main/resources/
├── templates/
│   ├── antique-inspection/   # Kiểm tra tình trạng đồ cổ
│   ├── export-receipt/       # Phiếu xuất kho
│   ├── import-receipt/       # Phiếu nhập kho
│   ├── warehouse-activity/   # Lịch sử hoạt động
│   ├── dashboard/
│   ├── fragments/            # Header, sidebar, footer
│   └── ...
├── static/          # CSS, JS
└── application.properties

seed_data.sql        # Dữ liệu mẫu idempotent (12 đồ cổ, 6 danh mục, 6 phiếu nhập/xuất...)
```

## Tác giả

Nguyễn Thành Danh  
Huỳnh Nguyễn Ánh Tuyết

Đồ án môn Lập trình với J2EE
