# 🔧 Hướng dẫn Debug Lỗi API Key

## 🚨 Vấn đề: "API key không hợp lệ" mặc dù đã điền

---

## ✅ Bước 1: Kiểm tra API key trong Google AI Studio

### 1.1 Truy cập Google AI Studio
- Vào https://aistudio.google.com/app/apikey
- **Nếu chưa đăng nhập:** Đăng nhập bằng tài khoản Google (phải là tài khoản cá nhân, không phải tài khoản công ty)
- Nếu lỗi "Access Denied", thử:
  - Đăng xuất toàn bộ account Google
  - Đăng nhập lại

### 1.2 Kiểm tra API keys
**Hãy tìm API key của bạn trong danh sách:**
```
API Key: AIzaSyBrKu38duMQXwamNzh8B0DxWq7pqxa2UQY
Name: myMoney
Status: ⚠️ Nếu thấy status khác, xem bên dưới
```

### 1.3 Các status có thể gặp:

| Status | Ý nghĩa | Cách sửa |
|--------|---------|----------|
| ✅ Active (xanh) | API key hoạt động bình thường | Không cần làm gì |
| ❌ Restricted (vàng) | API key bị hạn chế hoặc hết quota | Xóa và tạo key mới |
| ❌ Disabled (đỏ) | API key đã bị disable | Xóa và tạo key mới |
| ⚠️ Expired | API key đã hết hạn | Xóa và tạo key mới |

---

## ✅ Bước 2: Nếu API key hết quota hoặc bị restrict

### 2.1 Xóa API key cũ
- Vào https://aistudio.google.com/app/apikey
- Tìm API key của bạn
- Nhấn **"Delete key"** (icon xóa)
- Xác nhận xóa

### 2.2 Tạo API key mới
- Nhấn **"Create API key"**
- Chọn project hoặc tạo project mới
- Copy API key mới
- **QUAN TRỌNG:** Đừng share API key này công khai!

### 2.3 Cập nhật vào local.properties
```ini
GEMINI_API_KEY=your-new-api-key-here
```

---

## ✅ Bước 3: Kiểm tra `local.properties`

### 3.1 Đảm bảo file tồn tại
```
📁 E:\school\hoc ki 2 2025-2026\DACS3\MyMoney\
   └── local.properties  ✅ File này phải tồn tại
```

### 3.2 Kiểm tra nội dung chính xác
**File local.properties phải có:**
```ini
sdk.dir=C:\\Users\\lequo\\AppData\\Local\\Android\\Sdk
GEMINI_API_KEY=AIzaSyBrKu38duMQXwamNzh8B0DxWq7pqxa2UQY
```

**Hãy kiểm tra:**
- ✅ Không có khoảng trắng thừa trước `GEMINI_API_KEY`
- ✅ Không có khoảng trắng thừa sau dấu `=`
- ✅ API key không bị cắt ngắn
- ✅ Không có `""` quanh API key

**Ví dụ sai:**
```ini
GEMINI_API_KEY = AIzaSyBrKu... ❌ (khoảng trắng)
GEMINI_API_KEY="AIzaSyBrKu..." ❌ (có ngoặc kép)
GEMINI_API_KEY=AIzaSyBrKu ❌ (API key bị cắt)
```

---

## ✅ Bước 4: Rebuild Project

### 4.1 Đóng và mở lại Android Studio
- **File → Close Project**
- Mở lại project

### 4.2 Clean Build
```bash
# Trong Terminal của Android Studio
./gradlew clean build
```

Hoặc:
- **Build → Clean Project**
- **Build → Rebuild Project**

### 4.3 Chạy lại app
- **Run → Run 'app'** (Ctrl+R)

---

## ✅ Bước 5: Kiểm tra debug logs

### 5.1 Mở Android Studio Logcat
- **Shift+Alt+6** hoặc **View → Tool Windows → Logcat**

### 5.2 Tìm kiếm logs từ GeminiService
```
Filter: GeminiService
```

**Hãy tìm logs:**
```
GeminiService: API Key length: 39
GeminiService: API Key is empty: false
```

**Nếu thấy:**
```
❌ GeminiService: API Key length: 0
❌ GeminiService: API Key is empty: true
```
→ **API key không được đọc từ local.properties!**

---

## ✅ Bước 6: Nếu API key vẫn không hoạt động

### 6.1 Kiểm tra BuildConfig
- **Build → Generate BuildConfig**

### 6.2 Xem BuildConfig được generate
Đường dẫn:
```
app/build/generated/source/buildConfig/debug/com/example/mymoney/BuildConfig.java
```

Hãy kiểm tra:
```java
public static final String GEMINI_API_KEY = "AIzaSyBrKu38duMQXwamNzh8B0DxWq7pqxa2UQY";
```

**Nếu thấy:**
```java
public static final String GEMINI_API_KEY = "";  ❌ Trống!
```
→ local.properties không được đọc đúng

---

## ✅ Bước 7: Kiểm tra Google AI API Quotas

### 7.1 Truy cập Google Cloud Console
- Vào https://console.cloud.google.com
- Chọn project (nếu có nhiều)
- Vào **APIs & Services → Quotas**

### 7.2 Tìm Generative Language API
- Tìm "Generative Language API"
- Kiểm tra quota usage:
  - ✅ Nếu < 15 requests/phút: OK
  - ❌ Nếu = 15 requests/phút: Đã hết quota, đợi 1 phút

---

## 🎯 Các hành động cần thực hiện theo thứ tự

1. **Kiểm tra API key status** (Bước 1-3)
2. **Rebuild project** (Bước 4)
3. **Xem debug logs** (Bước 5)
4. **Kiểm tra BuildConfig** (Bước 6)
5. **Kiểm tra Google Cloud quotas** (Bước 7)

---

## 💡 Mẹo

- **Gemini API miễn phí:** 15 requests/phút
- **Nếu dùng hết quota:** Đợi 60 giây, rồi thử lại
- **Tạo API key mới:** Thường sẽ reset quota
- **Lỗi "something unexpected happened":** Thường là vì API key hết quota hoặc không hợp lệ

---

## 📞 Nếu vẫn có vấn đề

Hãy kiểm tra lại:
1. API key có active không? (Bước 1)
2. local.properties đúng không? (Bước 3)
3. BuildConfig được generate đúng không? (Bước 6)
4. Logcat in ra gì? (Bước 5)

Nếu cần tìm hiểu thêm:
- Google AI Studio: https://aistudio.google.com/app
- Google Cloud Console: https://console.cloud.google.com
