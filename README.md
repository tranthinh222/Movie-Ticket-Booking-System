# 🎬 Cinema Ticket Booking System

Hệ thống đặt vé xem phim trực tuyến sử dụng Spring Boot, tích hợp thanh toán VNPay.

## 📋 Yêu cầu hệ thống

- **Java 17** hoặc cao hơn
- **PostgreSQL 17** hoặc Docker
- **Gradle** (đã tích hợp sẵn wrapper)

---

## 🚀 Hướng dẫn cài đặt và chạy

### Cách 1: Chạy với Docker (Khuyến nghị)

```bash
# Clone project
git clone <repository-url>
cd Movie-Ticket-Booking-System

# Chạy với Docker Compose
docker-compose up -d
```

Ứng dụng sẽ chạy tại: `http://localhost:8081`

---

### Cách 2: Chạy trực tiếp

#### Bước 1: Khởi động PostgreSQL

Chạy `docker compose up -d postgres` để tạo database local tự động. Xem [hướng dẫn PostgreSQL và chuyển dữ liệu](POSTGRESQL_MIGRATION.md).

Nếu cài PostgreSQL trực tiếp, tạo database:

```sql
CREATE DATABASE ticketbooking;
```

#### Bước 2: Cấu hình application-local.properties

Cấu hình file riêng `src/main/resources/application-local.properties` (không commit):

```properties
# Database
spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/ticketbooking
spring.datasource.username=cinema
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# VNPay Configuration
vnpay.tmnCode=YOUR_TMN_CODE
vnpay.hashSecret=YOUR_HASH_SECRET
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8081/api/v1/payments/vnpay-callback

# JWT (tự tạo key hoặc dùng mẫu)
ticketbooking.jwt.base64-secret=your-secret-key-base64
ticketbooking.jwt.access-token-validity-in-seconds=3600
ticketbooking.jwt.refresh-token-validity-in-seconds=604800
```

#### Bước 3: Chạy ứng dụng

```bash
# Build và chạy
./gradlew bootRun
```

---

## 🧪 Chạy Unit Tests

```bash
# Chạy tất cả tests
./gradlew test

# Xem báo cáo chi tiết
open build/reports/tests/test/index.html
```

**Kết quả mong đợi:** 182/182 tests PASSED

---

## 💳 Hướng dẫn thanh toán VNPay

### Luồng thanh toán

```
1. Người dùng chọn ghế → Giữ ghế (5 phút)
2. Người dùng nhấn "Thanh toán" → Chọn VNPay
3. Hệ thống tạo booking → Redirect đến VNPay
4. Người dùng nhập thông tin thẻ
5. VNPay callback → Cập nhật trạng thái booking
```

### Thông tin thẻ test VNPay

> ⚠️ **Lưu ý:** Chỉ sử dụng cho môi trường Sandbox

| Thông tin          | Giá trị               |
| ------------------ | --------------------- |
| **Ngân hàng**      | NCB                   |
| **Số thẻ**         | `9704198526191432198` |
| **Tên chủ thẻ**    | `NGUYEN VAN A`        |
| **Ngày phát hành** | `07/15`               |
| **Mật khẩu OTP**   | `123456`              |

### Test thanh toán

**1. Đăng nhập và chọn phim:**

```bash
# Login
POST /api/v1/auth/login
{
  "email": "user@example.com",
  "password": "password"
}
```

**2. Giữ ghế:**

```bash
POST /api/v1/seat-holds
{
  "showtimeId": 1,
  "seatIds": [1, 2]
}
```

**3. Tạo booking với VNPay:**

```bash
POST /api/v1/bookings
{
  "paymentMethod": "VNPAY"
}
# Response chứa paymentUrl → mở trên browser
```

**4. Trên trang VNPay:**

- Chọn ngân hàng **NCB**
- Nhập số thẻ: `9704198526191432198`
- Nhập tên: `NGUYEN VAN A`
- Ngày phát hành: `07/15`
- Nhập OTP: `123456`

**5. Sau khi thanh toán thành công:**

- VNPay redirect về callback URL
- Booking status chuyển thành `COMPLETED`
- QR code được tạo tự động

---

## 📁 Cấu trúc dự án

```
src/main/java/com/cinema/ticketbooking/
├── controller/     # REST API endpoints
├── service/        # Business logic
├── repository/     # Data access layer
├── domain/         # Entities và DTOs
└── util/           # Utilities và constants
```

---

## 🔗 API Endpoints chính

| Method | Endpoint                | Mô tả            |
| ------ | ----------------------- | ---------------- |
| POST   | `/api/v1/auth/login`    | Đăng nhập        |
| POST   | `/api/v1/auth/register` | Đăng ký          |
| GET    | `/api/v1/films`         | Danh sách phim   |
| GET    | `/api/v1/showtimes`     | Lịch chiếu       |
| POST   | `/api/v1/seat-holds`    | Giữ ghế          |
| POST   | `/api/v1/bookings`      | Đặt vé           |
| GET    | `/api/v1/bookings/{id}` | Chi tiết booking |

---

## 🛠️ Troubleshooting

| Lỗi                        | Giải pháp                                     |
| -------------------------- | --------------------------------------------- |
| Database connection failed | Kiểm tra PostgreSQL đang chạy và thông tin kết nối |
| VNPay redirect lỗi         | Kiểm tra `vnpay.returnUrl` đúng host          |
| Ghế không giữ được         | Đảm bảo đã đăng nhập                          |
| Tests fail                 | Chạy `./gradlew clean test`                   |

---

## 📄 License

MIT License - Sử dụng tự do cho mục đích học tập.
