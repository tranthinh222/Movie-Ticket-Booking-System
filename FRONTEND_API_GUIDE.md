# 📘 Hướng dẫn Frontend - API Booking Ghế Theo Showtime

## 🔄 Thay đổi quan trọng

**TRƯỚC ĐÂY:** Ghế có `status` toàn cục (AVAILABLE/HOLD/BOOKED) - SAI ❌

**BÂY GIỜ:** Ghế không có status, trạng thái được quản lý theo từng **showtime** - ĐÚNG ✅

---

## 📋 Luồng API Frontend (theo thứ tự)

### **Bước 1: Lấy danh sách showtime**

```http
GET /api/v1/showtimes?filmId={filmId}&date={date}
```

**Response:**

```json
{
  "data": [
    {
      "id": 1,
      "filmId": 1,
      "auditoriumId": 1,
      "date": "2026-01-02",
      "startTime": "10:00",
      "endTime": "12:00"
    }
  ]
}
```

---

### **Bước 2: Lấy trạng thái ghế theo showtime** ⭐ API MỚI

```http
GET /api/v1/showtimes/{showtimeId}/seats
```

**Response:**

```json
{
  "data": [
    {
      "seatId": 1,
      "seatRow": "A",
      "number": 1,
      "status": "AVAILABLE",
      "seatVariantId": 1,
      "seatVariantName": "REG",
      "basePrice": 50000,
      "bonus": 0,
      "totalPrice": 50000
    },
    {
      "seatId": 2,
      "seatRow": "A",
      "number": 2,
      "status": "HOLD",
      "seatVariantId": 1,
      "seatVariantName": "REG",
      "basePrice": 50000,
      "bonus": 0,
      "totalPrice": 50000
    },
    {
      "seatId": 3,
      "seatRow": "A",
      "number": 3,
      "status": "BOOKED",
      "seatVariantId": 1,
      "seatVariantName": "REG",
      "basePrice": 50000,
      "bonus": 0,
      "totalPrice": 50000
    }
  ]
}
```

**Giải thích trạng thái:**

- `AVAILABLE`: Ghế trống, có thể chọn
- `HOLD`: Ghế đang được giữ bởi người khác (5 phút)
- `BOOKED`: Ghế đã được đặt

---

### **Bước 3: Hold ghế**

```http
POST /api/v1/seat-holds
Authorization: Bearer {token}

Body:
{
  "showtimeId": 1,
  "seatIds": [1, 2, 3]
}
```

**Response:** `201 Created`

**Lưu ý:**

- Ghế sẽ bị hold trong 5 phút
- Sau 5 phút tự động giải phóng
- Mỗi user chỉ hold được cho 1 showtime tại 1 thời điểm

---

### **Bước 4: Booking**

```http
POST /api/v1/bookings
Authorization: Bearer {token}

Body:
{
  "paymentMethod": "CASH" // hoặc "CARD", "VNPAY"
}
```

**Response:**

```json
{
  "statusCode": 201,
  "data": {
    "userId": 1,
    "username": "admin1",
    "price": 150000.0,
    "createdAt": "2026-01-02 10:30:24 AM",
    "paymentId": 5
  }
}
```

**Lưu ý:**

- Tự động lấy user từ JWT
- Tự động lấy các ghế đang hold của user
- Chuyển ghế từ HOLD → BOOKED
- Tạo payment record

---

### **Bước 5: Hủy hold**

```http
DELETE /api/v1/seat-holds
Authorization: Bearer {token}
```

**Response:** `204 No Content`

---

## 🎨 Gợi ý hiển thị Frontend

```jsx
// React example
const SeatMap = ({ showtimeId }) => {
  const [seats, setSeats] = useState([]);

  useEffect(() => {
    fetch(`/api/v1/showtimes/${showtimeId}/seats`)
      .then((res) => res.json())
      .then((data) => setSeats(data.data));
  }, [showtimeId]);

  return (
    <div className="seat-map">
      {seats.map((seat) => (
        <div
          key={seat.seatId}
          className={`seat seat-${seat.status.toLowerCase()}`}
          onClick={() => seat.status === "AVAILABLE" && selectSeat(seat)}
        >
          {seat.seatRow}
          {seat.number}
        </div>
      ))}
    </div>
  );
};
```

**CSS:**

```css
.seat-available {
  background: green;
  cursor: pointer;
}
.seat-hold {
  background: yellow;
  cursor: not-allowed;
}
.seat-booked {
  background: red;
  cursor: not-allowed;
}
```

---

## ⏱️ Polling để cập nhật realtime

Để hiển thị trạng thái ghế realtime, gọi API mỗi 5-10 giây:

```javascript
setInterval(() => {
  fetch(`/api/v1/showtimes/${showtimeId}/seats`)
    .then((res) => res.json())
    .then((data) => setSeats(data.data));
}, 5000); // 5 giây
```

---

## ✅ Checklist cho Frontend Developer

- [ ] Xóa code dựa vào `Seat.status` toàn cục
- [ ] Gọi API `/api/v1/showtimes/{id}/seats` để lấy trạng thái ghế
- [ ] Hiển thị 3 trạng thái: AVAILABLE (xanh), HOLD (vàng), BOOKED (đỏ)
- [ ] Chỉ cho phép chọn ghế AVAILABLE
- [ ] Hold ghế với `showtimeId` + `seatIds`
- [ ] Countdown 5 phút sau khi hold
- [ ] Tự động refresh trạng thái ghế mỗi 5-10 giây
- [ ] Xử lý error khi ghế đã bị hold/booked bởi người khác

---

## 🐛 Error Handling

**Khi hold ghế:**

```json
{
  "error": "Seat 3 is already held for this showtime"
}
```

**Khi booking:**

```json
{
  "error": "Ghế giữ quá thời gian hoặc không khả dụng"
}
```

➡️ **Giải pháp:** Refresh trạng thái ghế và yêu cầu user chọn lại
