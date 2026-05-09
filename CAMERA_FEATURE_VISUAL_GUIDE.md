# 🎨 Hướng dẫn Sử dụng - Camera Feature với Expandable FAB

## 📱 Giao diện sau khi triển khai

### Màn hình chính - FAB đóng (Ảnh 1)
```
┌─────────────────────────────────────┐
│  ⚙️  Trang chủ           🔍  📅   │
├─────────────────────────────────────┤
│                                     │
│   [Ví chính]    [Ví thứ 2]         │
│   Số dư:        Số dư:              │
│   450.000 vnđ   810.000 vnđ         │
│                                     │
│   Lịch sử giao dịch:                │
│   ┌─────────────────────────────┐  │
│   │ Ngày  Tuần  Tháng  Năm ...  │  │
│   └─────────────────────────────┘  │
│                                     │
│   Hôm nay, 2 tháng 5                │
│   Thu nhập  Chi tiêu   Số dư        │
│      0      50.000    400.000       │
│                                     │
│   💵 Ăn chào     -50.000             │
│   18:28, 02/05/2026                 │
│                                     │
├─────────────────────────────────────┤
│                                     │
│ 🏠      💰       ➕      💎     ⋯   │
│ Trang chủ Ngân sách    Tiết kiệm Khác│
│            (FAB ở giữa)             │
└─────────────────────────────────────┘
```

### Màn hình chính - FAB mở (Ảnh 2)
```
┌─────────────────────────────────────┐
│  ⚙️  Trang chủ           🔍  📅   │
├─────────────────────────────────────┤
│                                     │
│░░░░░░░░░░░░░OVERLAY░░░░░░░░░░░░░░░░│
│░░░░░░░░░(Click to close)░░░░░░░░░░░│
│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░│
│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░│
│░░░░        ┌─────────┐     ░░░░░░░░│
│░░░░        │ ✨  AI  │     ░░░░░░░░│
│░░░░        └─────────┘     ░░░░░░░░│
│░░░░                        ░░░░░░░░│
│░░░░        ┌─────────┐     ░░░░░░░░│
│░░░░        │ 📷 Camera│    ░░░░░░░░│
│░░░░        └─────────┘     ░░░░░░░░│
│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░│
│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░│
├─────────────────────────────────────┤
│                                     │
│ 🏠      💰       ❌      💎     ⋯   │
│ Trang chủ Ngân sách  (X) Tiết kiệm Khác│
│        (FAB xoay 45° thành X)       │
└─────────────────────────────────────┘
```

### Màn hình Camera - Locket Style (Ảnh 3)
```
┌─────────────────────────────────────┐
│███████████████████████████████████████
│███ ❌             🔦 (Flash) ████████
│███                              ████████
│███  ╔═══════════════════════╗  ████████
│███  ║                       ║  ████████
│███  ║                       ║  ████████
│███  ║   CAMERA PREVIEW      ║  ████████
│███  ║   (Rounded 32dp)      ║  ████████
│███  ║                       ║  ████████
│███  ║                       ║  ████████
│███  ║                       ║  ████████
│███  ║                       ║  ████████
│███  ╚═══════════════════════╝  ████████
│███                              ████████
│███  📷         ⚪️         🏷️   ████████
│███  Lịch sử   (Chụp)   Danh mục ████████
│███                              ████████
│███     ⬆️ Lịch sử chi tiêu       ████████
│███████████████████████████████████████
└─────────────────────────────────────┘
```

## 🔄 User Flow Chi tiết

### Luồng 1: Chụp ảnh chi tiêu
1. User ở màn Trang chủ
2. Tap nút ➕ FAB giữa màn hình
3. Menu mở ra với overlay tối màu
4. Chọn **📷 Camera**
5. Cấp quyền camera (nếu chưa có)
6. Màn hình camera Locket-style xuất hiện
7. [Optional] Tap 🔦 để bật flash
8. [Optional] Tap 🏷️ để chọn category trước
9. Tap nút ⚪️ giữa để chụp
10. TODO: Màn hình nhập số tiền + note

### Luồng 2: Chat AI (hiện tại)
1. User ở màn Trang chủ
2. Tap nút ➕ FAB giữa màn hình
3. Menu mở ra với overlay tối màu
4. Chọn **✨ AI**
5. Màn hình AI Chat hiện ra
6. Nhập "bữa tối 20k"
7. AI tự động tạo giao dịch

## 🎬 Animation Timeline

### Mở Menu (0 → 300ms)
```
0ms:   User tap FAB
0ms:   Overlay fade in bắt đầu (0 → 0.5 alpha)
0ms:   FAB rotation bắt đầu (0° → 45°)
0ms:   Icon FAB transition (+ → ×)
50ms:  AI button slide in từ dưới + fade in
100ms: Camera button slide in từ dưới + fade in
200ms: Overlay fade complete
200ms: FAB rotation complete
250ms: Buttons animation complete
```

### Đóng Menu (0 → 200ms)
```
0ms:   User tap overlay hoặc FAB
0ms:   Buttons fade out
0ms:   FAB rotation reverse (45° → 0°)
0ms:   Icon FAB transition (× → +)
150ms: Buttons fade complete
200ms: FAB rotation complete
200ms: Overlay fade out complete
```

## 📐 Layout Specifications

### Expandable Menu Items
```kotlin
Position: 180dp above FAB center
Size: 120dp width × 56dp height
Corner radius: 28dp
Spacing between items: 16dp
Shadow elevation: 4dp

Item structure:
┌──────────────────────┐
│  [Icon 24dp] [Text]  │  56dp height
└──────────────────────┘
   120dp width
```

### Camera Screen Controls
```kotlin
Top controls bar:
  Margin top: 48dp
  
❌ Close button:
  Size: 48dp circle
  Position: Top-left (24dp margin)
  Background: Black alpha 0.5
  Icon size: 24dp

🔦 Flash button:
  Size: 48dp circle
  Position: Top-right (24dp margin)
  Background: Black alpha 0.5
  Icon size: 24dp
  Tint: Yellow khi ON

Camera preview:
  Margin: 16dp all sides
  Corner radius: 32dp
  
Bottom controls:
  Margin bottom: 48dp
  
📷 History button:
  Size: 48dp circle
  Position: Left third
  
⚪️ Shutter button:
  Size: 72dp (outer) / 64dp (inner)
  Position: Center
  Color: White
  
🏷️ Category button:
  Size: 48dp circle
  Position: Right third
```

## 🎯 Component Props Reference

### CustomBottomBar
```kotlin
@Composable
fun CustomBottomBar(
    currentRoute: String?,           // Route hiện tại
    onTabSelected: (BottomTab) -> Unit,  // Chọn tab
    isMenuOpen: Boolean,             // Menu đang mở?
    onMenuToggle: () -> Unit,        // Toggle menu
    onAIClick: () -> Unit,           // Click AI button
    onCameraClick: () -> Unit,       // Click Camera button
    modifier: Modifier = Modifier
)
```

### CameraCaptureScreen
```kotlin
@Composable
fun CameraCaptureScreen(
    walletId: Long,                  // ID ví hiện tại
    onNavigateBack: () -> Unit,      // Back button
    onPhotoTaken: (Uri) -> Unit,     // Callback khi chụp xong
    modifier: Modifier = Modifier
)
```

## 🐛 Troubleshooting

### Lỗi: Camera preview không hiện
**Kiểm tra:**
1. Permission đã được cấp chưa?
2. CameraX dependencies đã sync chưa?
3. Device có camera không?

**Fix:**
```kotlin
// Check permission
if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
    == PackageManager.PERMISSION_GRANTED) {
    // OK
}
```

### Lỗi: FAB menu không mở
**Kiểm tra:**
1. `isFabMenuOpen` state có update không?
2. Overlay có render không?

**Debug:**
```kotlin
Log.d("FAB", "Menu state: $isFabMenuOpen")
```

### Lỗi: Animation bị giật
**Nguyên nhân:** Recomposition quá nhiều

**Fix:** Sử dụng `remember` và `derivedStateOf` đúng cách

## 📱 Testing Scenarios

### Test 1: FAB Menu Animation
1. Tap FAB → Menu mở smooth
2. Tap overlay → Menu đóng smooth
3. Tap FAB khi đang mở → Menu đóng
4. Kiểm tra stagger animation của 2 buttons

### Test 2: Camera Permission
1. Mở camera lần đầu → Request permission dialog
2. Deny → Hiện UI yêu cầu cấp quyền
3. Cấp quyền → Camera preview hiện ra

### Test 3: Camera Functions
1. Flash toggle hoạt động
2. Capture lưu ảnh đúng
3. Back button quay về màn hình chính
4. Multiple captures không crash

### Test 4: Integration
1. From Home → FAB → AI → Works
2. From Home → FAB → Camera → Works
3. Photo URI được truyền đúng
4. WalletId được truyền đúng

## 🎨 Theme Support

### Light Mode
- FAB: Primary color (Blue)
- Menu items: Primary container (Light blue)
- Overlay: Black 50% alpha
- Camera controls: Black 50% alpha

### Dark Mode
- FAB: Primary color (Light blue)
- Menu items: Primary container (Dark blue)
- Overlay: Black 50% alpha
- Camera controls: Black 50% alpha

---

**Document Version:** 1.0  
**Last Updated:** 2026-05-03

