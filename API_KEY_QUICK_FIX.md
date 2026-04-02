# ⚡ Cách khắc phục lỗi "API key không hợp lệ"

## 🔍 Nguyên nhân khả năng

1. **API key hết quota** (15 requests/phút)
2. **API key bị disable hoặc hết hạn**
3. **local.properties không được load đúng**
4. **API key trống hoặc sai**

---

## 🚀 Các bước khắc phục nhanh

### Bước 1: Kiểm tra API key mới
- Vào https://aistudio.google.com/app/apikey
- Xóa API key cũ (Delete key)
- Tạo API key mới (Create API key)
- Copy API key

### Bước 2: Cập nhật local.properties
```ini
GEMINI_API_KEY=your-new-api-key-here
```

### Bước 3: Rebuild project
```bash
./gradlew clean build
```

Hoặc dùng Android Studio:
- **Build → Clean Project**
- **Build → Rebuild Project**
- **Run → Run 'app'**

### Bước 4: Kiểm tra Logcat
- Mở: **Shift+Alt+6**
- Filter: `GeminiService`
- Tìm: `API Key length:` → Nếu = 0, API key không được load!

---

## ✅ Nếu vẫn lỗi

Hãy xem file `API_KEY_DEBUG_GUIDE.md` để debug chi tiết.
