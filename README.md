# Website Quản Lý Kho Đồ Cổ (Antique Warehouse Management)

## Giới thiệu

Website quản lý kho đồ cổ được xây dựng bằng Spring Boot, cho phép quản lý danh mục, đồ cổ, nhập/xuất kho và tồn kho.

## Công nghệ sử dụng

- **Backend:** Spring Boot 3.3.13, Spring Security, Spring Data JPA
- **Frontend:** Thymeleaf, Bootstrap 5, DataTables
- **Database:** MySQL 8
- **Build tool:** Maven
- **Java:** 21+

## Chức năng chính

- **Quản lý danh mục:** Thêm, sửa, xóa danh mục đồ cổ
- **Quản lý đồ cổ:** Thêm, sửa, xóa đồ cổ kèm upload hình ảnh
- **Nhập kho:** Tạo phiếu nhập kho, quản lý chi tiết nhập
- **Xuất kho:** Tạo phiếu xuất kho theo lý do (bán, trưng bày, bảo trì, khác)
- **Tồn kho:** Theo dõi số lượng tồn kho theo từng đồ cổ
- **Quản lý người dùng:** Phân quyền Admin / Nhân viên
- **Dashboard:** Thống kê tổng quan kho

## Cài đặt

### Yêu cầu

- JDK 21 trở lên
- MySQL 8
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

### Tài khoản mặc định

| Tài khoản   | Mật khẩu      | Quyền      |
|-------------|---------------|------------|
| admin       | admin123      | Admin      |
| nhanvien1   | nhanvien123   | Nhân viên  |

## Cấu trúc dự án

```
src/main/java/com/example/antique/
├── config/          # Cấu hình Security, MVC
├── controller/      # Xử lý request
├── dto/             # Data Transfer Objects
├── entity/          # JPA Entities
├── repository/      # Spring Data JPA Repositories
└── service/         # Business logic

src/main/resources/
├── templates/       # Thymeleaf templates
├── static/          # CSS, JS
└── application.properties
```

## Tác giả
Nguyễn Thành Danh
Huỳnh Nguyễn Ánh Tuyết

Đồ án môn Lập trình với J2EE
