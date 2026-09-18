# Tin tức

Hibernate tạo bảng `news` khi khởi động (cấu hình hiện tại `ddl-auto=update`). Bảng lưu tiêu đề, tóm tắt, nội dung văn bản, URL ảnh, danh mục, cờ nổi bật, cờ xuất bản, ngày tạo và ngày cập nhật.

Profile `local` bổ sung các bài mẫu còn thiếu từ `src/main/resources/data/news-demo.json` (27 bài). `seedKey` ổn định giúp không thêm trùng khi khởi động lại hoặc khi admin đổi tiêu đề. Bảy bài cũ được liên kết theo tiêu đề nếu chưa có `seedKey`, không ghi đè nội dung đã sửa. Hai mươi bài mới có nội dung tự viết và ảnh minh họa đã upload lên Cloudinary. Nguồn ảnh và tác giả được ghi trong `src/main/resources/data/news-image-sources.json`. Seeder chỉ đọc URL đã lưu, không download hoặc upload ảnh mỗi lần chạy. Không có dữ liệu bài viết viết sẵn trong frontend nữa.

API được bọc theo cấu trúc phản hồi chung `{statusCode, data}`:

- `GET /api/v1/news?page=1&size=10&category=...&search=...`: chỉ bài đã xuất bản; ưu tiên bài nổi bật, sau đó ngày tạo và ID giảm dần.
- `GET /api/v1/news/{id}`: nội dung chi tiết bài đã xuất bản.
- `GET /api/v1/admin/news`: admin xem cả bản nháp, có phân trang và tìm tiêu đề.
- `POST /api/v1/news`: admin tạo bài.
- `PUT /api/v1/news/{id}`: admin sửa bài.
- `DELETE /api/v1/news/{id}`: admin xóa bài.

Payload tạo/sửa: `title`, `summary`, `content`, `image`, `category`, `featured`, `published`. Danh mục hợp lệ: `Tin mới`, `Review phim`, `Sắp chiếu`, `Diễn viên`. Ảnh là URL HTTP/HTTPS hoặc để trống. Nội dung được hiển thị như văn bản, giữ xuống dòng.

Frontend `/news` đọc danh sách và chi tiết bằng API, có tìm kiếm, lọc, xem thêm, loading, empty, error và thử lại. `/admin/news` cung cấp tạo/sửa/xóa, xuất bản/bản nháp và chọn bài nổi bật. Tải lại trang công khai để lấy thay đổi từ admin. Danh sách bài đã lưu vẫn dùng localStorage trên trình duyệt, chưa đồng bộ theo tài khoản.
