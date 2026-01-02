-- Migration script: Xóa trạng thái toàn cục khỏi ghế, quản lý theo showtime
-- Chạy script này để cập nhật database theo đúng nghiệp vụ

-- Bước 1: Thêm cột showtime_id vào booking_items
ALTER TABLE booking_items ADD COLUMN showtime_id BIGINT;

-- Bước 2: Thêm foreign key constraint
ALTER TABLE booking_items 
ADD CONSTRAINT fk_booking_items_showtime 
FOREIGN KEY (showtime_id) REFERENCES showtimes(id);

-- Bước 3: Xóa cột status khỏi seats (sau khi đã backup data nếu cần)
-- CẢNH BÁO: Thao tác này không thể hoàn tác!
ALTER TABLE seats DROP COLUMN status;

-- Bước 4: Tạo index để tăng tốc query
CREATE INDEX idx_booking_items_seat_showtime ON booking_items(seat_id, showtime_id);
CREATE INDEX idx_seat_holds_seat_showtime ON seat_holds(seat_id, show_time_id);

-- Ghi chú:
-- - Trước khi chạy script, hãy backup database!
-- - Sau khi chạy script, restart ứng dụng Spring Boot
-- - Nếu dùng spring.jpa.hibernate.ddl-auto=update, Hibernate sẽ tự động thêm cột mới
