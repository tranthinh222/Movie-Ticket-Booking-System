# Suất chiếu demo local

Profile mặc định là `local`. Chạy `TicketbookingApplication` trực tiếp trong IDE hoặc chạy trong thư mục backend:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 bash gradlew bootRun --console=plain
```

Profile `local` bổ sung suất chiếu cho phim có trạng thái `NOW_SHOWING`, từ hôm nay đến 6 ngày tiếp theo (giờ Việt Nam). Suất đã qua trong hôm nay không được tạo. Lịch phân bổ phim theo phòng và ngày, có 20 phút nghỉ giữa các suất.

Seeder chạy sau các seeder phim và phòng. Không sửa hoặc xóa suất cũ, không đổi ngày vé đã đặt, và bỏ qua khoảng giờ đã có suất trong cùng phòng. Khởi động lại với cùng dữ liệu không thêm lịch trùng. Nếu ngày mới đã vượt khoảng demo, khởi động lại backend để bổ sung lịch mới.

Khi triển khai thật, đặt `SPRING_PROFILES_ACTIVE=prod` để thay thế profile mặc định và không tạo suất chiếu demo.
