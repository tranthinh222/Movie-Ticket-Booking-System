# Khuyến mãi vé phim

Bảng `discount` lưu mã duy nhất, tiêu đề, mô tả, ảnh, loại giảm (`FIXED`/`PERCENT`), mức giảm, đơn tối thiểu, giảm tối đa, ngày bắt đầu/kết thúc và trạng thái bật/tắt. Ngày hiệu lực tính theo giờ Việt Nam, bao gồm cả ngày bắt đầu và kết thúc.

- `GET /api/v1/discounts`: danh sách ưu đãi đang bật và còn hiệu lực, không cần đăng nhập.
- `POST /api/v1/discounts/quote` với `{code}`: yêu cầu đăng nhập; backend tính tổng từ ghế đang giữ, không nhận giá từ FE.
- `GET /api/v1/admin/discounts`: admin xem toàn bộ.
- `POST /api/v1/discounts`, `PUT /api/v1/discounts/{id}`, `DELETE /api/v1/discounts/{id}`: admin quản lý.
- `POST /api/v1/bookings`: thêm `discountCode` tùy chọn cùng `paymentMethod`.

Mỗi đơn chỉ áp dụng một mã. Backend kiểm tra lại điều kiện lúc tạo đơn, làm tròn phần giảm xuống đồng, không giảm vượt tổng đơn. Đơn lưu `subtotal`, `discountCode`, `discountAmount` và `total_price` sau giảm. VNPay dùng giá sau giảm. Việc xóa hoặc sửa mã không thay đổi giá đã lưu trong đơn cũ.

Frontend: trang khuyến mãi gọi API, lọc theo loại giảm, xem điều kiện, sao chép mã và mở danh sách phim. Trang thanh toán hỗ trợ áp dụng/bỏ mã và hiển thị tổng sau giảm. Admin quản lý ở `/admin/discounts`. Chưa có ưu đãi bắp nước/đối tác, giới hạn lượt sử dụng hay quy tắc riêng theo thành viên.

Profile `local` thêm ba mã demo còn thiếu: `CINE10`, `GIAM20K`, `NHOM15`. Ngày hiệu lực bắt đầu khi tạo lần đầu, kết thúc sau 365 ngày. Seeder không sửa mã đã tồn tại hoặc tự gia hạn ưu đãi admin chỉnh sửa. Hibernate tạo bảng và các cột đơn mới theo cấu hình `ddl-auto=update` hiện tại.
