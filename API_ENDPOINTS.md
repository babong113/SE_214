# API Endpoints - Badminton Management

Tài liệu này mô tả các API đã được triển khai trong mã nguồn hiện tại của dự án.

## 1. Tổng quan

| Thuộc tính | Giá trị |
|---|---|
| Base URL mặc định | `http://localhost:8080` |
| Content-Type | `application/json` |
| Controller hiện có | `AuthController` |
| Prefix endpoint | `/auth` |
| Cơ chế bảo mật | Spring Security, stateless, HTTP Basic |
| Database | PostgreSQL |

Port mặc định là `8080` và có thể thay đổi bằng biến môi trường `SERVER_PORT`.

Các endpoint công khai hiện tại:

```text
/auth/register
/auth/login
/auth/forgot-password
/auth/reset-password
/error
```

> API đăng nhập hiện chỉ xác thực và trả thông tin người dùng. Hệ thống chưa phát hành JWT, access token, refresh token hoặc session đăng nhập.

## 2. Danh sách endpoint

| Method | Endpoint | Chức năng | Xác thực | Thành công |
|---|---|---|---|---|
| `POST` | `/auth/register` | Đăng ký tài khoản khách hàng | Public | `201 Created` |
| `POST` | `/auth/login` | Đăng nhập bằng email hoặc số điện thoại | Public | `200 OK` |
| `POST` | `/auth/forgot-password` | Tạo và gửi OTP đặt lại mật khẩu qua email | Public | `200 OK` |
| `POST` | `/auth/reset-password` | Kiểm tra OTP và đổi mật khẩu | Public | `200 OK` |

Các bảng sân, bảng giá, booking, dịch vụ và FAQ đã có trong database migration nhưng chưa có controller/API tương ứng.

## 3. Cấu trúc response chung

### Response có dữ liệu

```json
{
  "success": true,
  "message": "Thông báo kết quả",
  "data": {}
}
```

### Response không có dữ liệu

```json
{
  "success": true,
  "message": "Thông báo kết quả"
}
```

### Response lỗi nghiệp vụ

```json
{
  "success": false,
  "message": "Nội dung lỗi"
}
```

| Trường | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `success` | `boolean` | Kết quả xử lý request |
| `message` | `string` | Thông báo từ backend |
| `data` | `object` | Dữ liệu trả về; bị lược bỏ nếu có giá trị `null` |

`data` bị lược bỏ khi `null` vì `ApiResponse` sử dụng `@JsonInclude(JsonInclude.Include.NON_NULL)`.

---

## 4. Đăng ký tài khoản

### `POST /auth/register`

Tạo tài khoản mới với quyền `CUSTOMER` và trạng thái `ACTIVE`.

### Request

```http
POST /auth/register
Content-Type: application/json
```

```json
{
  "fullName": "Nguyễn Văn A",
  "phoneNumber": "0912345678",
  "email": "nguyenvana@example.com",
  "password": "123456"
}
```

| Trường | Kiểu | Bắt buộc | Validation |
|---|---|---:|---|
| `fullName` | `string` | Có | Không được `null`, rỗng hoặc chỉ chứa khoảng trắng |
| `phoneNumber` | `string` | Có | Đúng 10 chữ số và bắt đầu bằng `0`; regex `^(0[0-9]{9})$` |
| `email` | `string` | Có | Không được để trống và phải đúng định dạng email |
| `password` | `string` | Có | Không được để trống và có ít nhất 6 ký tự |

### Quy trình xử lý

- `fullName` và `phoneNumber` được loại bỏ khoảng trắng ở hai đầu.
- `email` được loại bỏ khoảng trắng ở hai đầu và chuyển thành chữ thường.
- Backend kiểm tra email và số điện thoại chưa tồn tại.
- Mật khẩu được mã hóa bằng BCrypt trước khi lưu.
- User mới luôn nhận `role = CUSTOMER` và `status = ACTIVE`.

### Response thành công

Status: `201 Created`

```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0912345678",
    "email": "nguyenvana@example.com",
    "role": "CUSTOMER",
    "status": "ACTIVE"
  }
}
```

### Lỗi nghiệp vụ

Status: `400 Bad Request`

Email đã tồn tại:

```json
{
  "success": false,
  "message": "Email đã tồn tại"
}
```

Số điện thoại đã tồn tại:

```json
{
  "success": false,
  "message": "Số điện thoại đã tồn tại"
}
```

### Ví dụ cURL

```bash
curl -X POST "http://localhost:8080/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0912345678",
    "email": "nguyenvana@example.com",
    "password": "123456"
  }'
```

---

## 5. Đăng nhập

### `POST /auth/login`

Xác thực bằng email hoặc số điện thoại kết hợp với mật khẩu.

### Request bằng email

```json
{
  "login": "nguyenvana@example.com",
  "password": "123456"
}
```

### Request bằng số điện thoại

```json
{
  "login": "0912345678",
  "password": "123456"
}
```

| Trường | Kiểu | Bắt buộc | Validation |
|---|---|---:|---|
| `login` | `string` | Có | Không được `null`, rỗng hoặc chỉ chứa khoảng trắng |
| `password` | `string` | Có | Không được `null`, rỗng hoặc chỉ chứa khoảng trắng |

### Quy trình xử lý

- `login` được loại bỏ khoảng trắng ở hai đầu và chuyển thành chữ thường.
- Nếu `login` chứa `@`, backend tìm tài khoản theo email.
- Nếu `login` không chứa `@`, backend tìm theo số điện thoại.
- Spring Security xác thực mật khẩu với BCrypt hash trong database.

### Response thành công

Status: `200 OK`

```json
{
  "success": true,
  "message": "Đăng nhập thành công ",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@example.com",
    "phoneNumber": "0912345678",
    "role": "CUSTOMER"
  }
}
```

> Chuỗi `message` trong code hiện có một dấu cách ở cuối sau chữ `công`.

### Sai tài khoản hoặc mật khẩu

Status: `400 Bad Request`

```json
{
  "success": false,
  "message": "Tài khoản hoặc mật khẩu không đúng"
}
```

Implementation hiện chuyển lỗi sai thông tin đăng nhập thành `400 Bad Request`, không phải `401 Unauthorized`.

### Ví dụ cURL

```bash
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "login": "nguyenvana@example.com",
    "password": "123456"
  }'
```

---

## 6. Quên mật khẩu - Gửi OTP

### `POST /auth/forgot-password`

Tạo OTP gồm 6 chữ số, lưu vào tài khoản và gửi OTP tới email người dùng.

### Request

```http
POST /auth/forgot-password
Content-Type: application/json
```

```json
{
  "email": "nguyenvana@example.com"
}
```

| Trường | Kiểu | Bắt buộc | Validation |
|---|---|---:|---|
| `email` | `string` | Có | Không được để trống và phải đúng định dạng email |

### Quy trình xử lý

1. Email được loại bỏ khoảng trắng ở hai đầu và chuyển thành chữ thường.
2. Backend tìm user theo email.
3. Backend sinh OTP 6 chữ số bằng `SecureRandom`.
4. OTP được lưu trực tiếp vào cột `users.reset_otp`.
5. User được lưu vào database.
6. OTP được gửi bằng email qua Spring Mail.

OTP được định dạng bằng `%06d`, vì vậy các mã bắt đầu bằng `0` vẫn luôn đủ 6 chữ số, ví dụ `001234`.

Nếu người dùng yêu cầu OTP mới, giá trị mới ghi đè OTP cũ trong cùng cột `reset_otp`.

### Response thành công

Status: `200 OK`

```json
{
  "success": true,
  "message": "Gửi yêu cầu đổi mật khẩu thành công"
}
```

API không trả OTP trong response. OTP chỉ được gửi đến email.

### Email không tồn tại

Status: `400 Bad Request`

```json
{
  "success": false,
  "message": "Email không tồn tại"
}
```

### Lỗi validation

Status: `400 Bad Request`

Các thông báo có thể xuất hiện:

| Trường | Thông báo |
|---|---|
| `email` | `Email không được để trống` |
| `email` | `Email không đúng định dạng` |

### Ví dụ cURL

```bash
curl -X POST "http://localhost:8080/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nguyenvana@example.com"
  }'
```

---

## 7. Đặt lại mật khẩu

### `POST /auth/reset-password`

Kiểm tra OTP đã lưu trên user, cập nhật mật khẩu mới và xóa OTP sau khi thành công.

### Request

```http
POST /auth/reset-password
Content-Type: application/json
```

```json
{
  "email": "nguyenvana@example.com",
  "otp": "123456",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

| Trường | Kiểu | Bắt buộc | Validation |
|---|---|---:|---|
| `email` | `string` | Có | Không được để trống và phải đúng định dạng email |
| `otp` | `string` | Có | Đúng 6 chữ số; regex `^[0-9]{6}$` |
| `newPassword` | `string` | Có | Không được để trống và có ít nhất 6 ký tự |
| `confirmPassword` | `string` | Có | Không được để trống; phải giống `newPassword` theo kiểm tra nghiệp vụ |

### Quy trình xử lý

1. Email được loại bỏ khoảng trắng ở hai đầu và chuyển thành chữ thường.
2. Backend tìm user theo email.
3. Backend kiểm tra `newPassword` giống `confirmPassword`.
4. Backend lấy OTP từ `users.reset_otp`.
5. OTP nhận từ request được loại bỏ khoảng trắng ở hai đầu rồi so sánh với OTP trong database.
6. Nếu OTP đúng, mật khẩu mới được mã hóa bằng BCrypt.
7. Backend đặt `reset_otp = null` để OTP không thể dùng lại.
8. User được lưu lại vào database.

### Response thành công

Status: `200 OK`

```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công"
}
```

### Email không tồn tại

Status: `400 Bad Request`

```json
{
  "success": false,
  "message": "Email không tồn tại"
}
```

### Mật khẩu xác nhận không khớp

Status: `400 Bad Request`

```json
{
  "success": false,
  "message": "Mật khẩu xác nhận không khớp"
}
```

### OTP sai, không tồn tại hoặc đã sử dụng

Status: `400 Bad Request`

```json
{
  "success": false,
  "message": "Mã otp không hợp lệ"
}
```

### Lỗi validation

Status: `400 Bad Request`

Các thông báo đúng theo code hiện tại:

| Trường | Thông báo |
|---|---|
| `email` | `email không được để trống` |
| `email` | `email không đúng định dạng` |
| `otp` | `Mã xác thực không được để trống` |
| `otp` | `Mã OTP phải gồm đúng 6 chữ số` |
| `newPassword` | `Mật khẩu mới không được để trống` |
| `newPassword` | `Mật khẩu phải có ít nhất 6 ký tự` |
| `confirmPassword` | `Mật khẩu xác nhận không được để trống` |

Ví dụ response validation:

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "otp": "Mã OTP phải gồm đúng 6 chữ số",
    "newPassword": "Mật khẩu phải có ít nhất 6 ký tự"
  }
}
```

### Ví dụ cURL

```bash
curl -X POST "http://localhost:8080/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nguyenvana@example.com",
    "otp": "123456",
    "newPassword": "newPassword123",
    "confirmPassword": "newPassword123"
  }'
```

---

## 8. Response validation dùng chung

Khi DTO vi phạm validation, `GlobalExceptionHandler` trả:

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "fieldName": "Thông báo lỗi"
  }
}
```

Nếu một field vi phạm nhiều annotation, response giữ thông báo đầu tiên được thu thập cho field đó.

## 9. Bảo mật và phân quyền hiện tại

- CSRF đang bị tắt.
- Session policy là `STATELESS`.
- Bốn endpoint trong `AuthController` đều public.
- Mọi URL khác đều yêu cầu HTTP Basic authentication.
- Các mảng URL dành cho `MANAGER`, `STAFF` và `CUSTOMER` hiện chưa có endpoint.
- Role được đưa vào Spring Security dưới dạng `ROLE_<role>`, ví dụ `ROLE_CUSTOMER`.
- Trạng thái `BLOCKED` có trong enum/database nhưng chưa được ánh xạ thành thuộc tính disabled/locked trong `CustomUserDetailsService`.

## 10. Mã trạng thái HTTP

| Status | Trường hợp |
|---|---|
| `200 OK` | Đăng nhập, gửi OTP hoặc đổi mật khẩu thành công |
| `201 Created` | Đăng ký thành công |
| `400 Bad Request` | Validation thất bại hoặc lỗi nghiệp vụ được biểu diễn bằng `InvalidDataException` |
| `401 Unauthorized` | Truy cập URL được bảo vệ mà không có HTTP Basic credentials hợp lệ |
| `500 Internal Server Error` | Lỗi chưa được `GlobalExceptionHandler` xử lý, ví dụ một số lỗi gửi mail hoặc database |

## 11. Cấu hình môi trường

Ứng dụng đọc cấu hình từ `.env` thông qua `spring-dotenv`.

### Database

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

### Email SMTP

```text
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD
MAIL_SMTP_AUTH
MAIL_SMTP_STARTTLS_ENABLE
```

File `.env` đã có rule trong `.gitignore` và hiện không còn được Git theo dõi.

## 12. Giới hạn của implementation hiện tại

- OTP được lưu trực tiếp dưới dạng chuỗi trong cột `users.reset_otp`, không được hash.
- OTP không có thời gian hết hạn.
- Không giới hạn số lần nhập sai OTP.
- Không giới hạn tần suất yêu cầu gửi OTP.
- Một yêu cầu OTP mới ghi đè OTP cũ.
- OTP được xóa bằng cách đặt `reset_otp = null` sau khi đổi mật khẩu thành công.
- Chưa có JWT, access token, refresh token hoặc API đăng xuất.
- Chưa có controller/API cho sân, giá, booking, dịch vụ và FAQ.
- `GlobalExceptionHandler` chỉ chuẩn hóa `InvalidDataException` và lỗi validation; các exception khác dùng response mặc định của Spring Boot.
