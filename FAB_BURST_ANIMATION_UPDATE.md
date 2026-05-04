# 🎆 FAB Burst Animation - Cập nhật Animation

## 📋 Thay đổi

Đã **thay đổi hoàn toàn** animation của expandable FAB menu từ dạng **"staggered slide with labels"** sang **"burst out circles"** giống ảnh tham khảo.

## ✨ Animation Mới

### Trước (Old):
```
FAB ở giữa
   ↓ Click
2 nút hình chữ nhật có label trượt từ dưới lên
┌──────────────┐
│  ✨  AI      │  ← Slide up với delay 0ms
└──────────────┘
┌──────────────┐
│  📷  Camera  │  ← Slide up với delay 50ms
└──────────────┘
```

### Sau (New):
```
                 ⭕ AI
                ↗️
        ➕ FAB     
               ↖️
                 ⭕ Camera

2 nút tròn "bung ra" từ vị trí FAB theo hướng chéo
với spring animation bouncy
```

## 🎬 Chi tiết Animation

### 1. **Position Animation** (Vị trí)
```kotlin
// Y-axis: Di chuyển lên trên 90dp
subOffsetY: 0dp → -90dp (khi menu mở)

// X-axis: Bung ra 2 bên
cameraOffsetX: 0dp → -110dp (sang trái)
aiOffsetX:     0dp → +110dp (sang phải)

// Animation spec: Spring bouncy
dampingRatio = Spring.DampingRatioMediumBouncy
stiffness = Spring.StiffnessMediumLow
```

### 2. **Scale Animation** (Phóng to)
```kotlin
subScale: 0f → 1f
// Nút con xuất hiện từ kích thước 0 và phóng to lên với spring
```

### 3. **Alpha Animation** (Độ mờ)
```kotlin
subAlpha: 0f → 1f
// Fade in nhanh: 180ms khi mở, 120ms khi đóng
```

### 4. **FAB Rotation** (Xoay nút chính)
```kotlin
fabRotation: 0° → 45°
// Icon + → × khi menu mở
```

## 🎨 Visual Specifications

### Sub-buttons (Nút con):
```
Size: 56dp (giống FAB chính)
Shape: Circle
No labels (chỉ icon)

Camera button:
  - Color: #E91E8C (Hồng đậm)
  - Icon: CameraAlt (26dp)
  - Position: Trái trên, offset (-110dp, -90dp)

AI button:
  - Color: #D63384 (Hồng tím)
  - Icon: AutoAwesome (26dp)
  - Position: Phải trên, offset (+110dp, -90dp)
```

### Layout khi mở:
```
         ⭕ AI (#D63384)
        ↗️
    ➕ FAB (Primary)     Y = -90dp
       ↖️                ↑
        ⭕ Camera        │
       (#E91E8C)         │
                        FAB center
 ←────110dp────┼────110dp────→
               X-axis
```

## ⚙️ Implementation Details

### Z-order (Thứ tự render):
```
1. Bottom bar background (subtract.xml)
2. Tab navigation items (Home, Budget, Saving, Other)
3. Camera sub-button      ← render SAU để không bị che
4. AI sub-button          ← render SAU để không bị che
5. Main FAB               ← render CUỐI để luôn ở trên cùng
```

### Click behavior:
```kotlin
Sub-buttons chỉ clickable khi isMenuOpen = true:
onClick = { if (isMenuOpen) { onMenuToggle(); onAIClick() } }

→ Tránh ghost clicks khi sub-buttons đang ẩn
```

## 📊 Animation Timeline

### Mở menu (Open):
```
0ms:     User tap FAB
0ms:     isMenuOpen = true
0ms:     FAB rotation bắt đầu (0° → 45°)
0ms:     FAB icon transition (+ → ×)
0-300ms: Sub-buttons spring ra (position + scale + alpha)
  - Position: Spring animation với bouncy effect
  - Scale: 0 → 1 với spring
  - Alpha: Fade in linear 180ms
200ms:   FAB rotation complete
~300ms:  Spring animation settle (có thể bounce 1-2 lần)
```

### Đóng menu (Close):
```
0ms:     User tap FAB hoặc overlay
0ms:     isMenuOpen = false
0ms:     FAB rotation reverse (45° → 0°)
0ms:     FAB icon transition (× → +)
0-300ms: Sub-buttons spring về (position + scale + alpha)
  - Position: Spring về 0
  - Scale: 1 → 0
  - Alpha: Fade out 120ms (nhanh hơn fade in)
200ms:   FAB rotation complete
~300ms:  Spring animation settle
```

## 🔧 Code Changes

### Removed:
- ❌ `ExpandableMenuItem` composable
- ❌ `AnimatedVisibility` với Column layout
- ❌ `RoundedRectangle` buttons with labels
- ❌ Staggered delay animation (0ms, 50ms)
- ❌ Import: `RoundedCornerShape`, `Surface`, `fillMaxSize`

### Added:
- ✅ 2 separate `FloatingActionButton` cho Camera & AI
- ✅ Spring animation spec với bouncy effect
- ✅ Position offset animation (X, Y độc lập)
- ✅ Scale animation từ 0 → 1
- ✅ Import: `graphicsLayer` cho scale transform

### Modified:
- 🔄 Animation từ "slide up" → "burst out"
- 🔄 Layout từ "vertical stack" → "triangular spread"
- 🔄 Buttons từ "rectangle with label" → "circle icon only"

## 🎯 Benefits

### 1. **Visual Impact**
- Hiệu ứng "bung ra" ấn tượng hơn slide up đơn giản
- Spring bouncy tạo cảm giác playful, vui mắt

### 2. **Space Efficiency**
- Không cần label → nút nhỏ gọn hơn
- Layout tam giác tận dụng không gian tốt

### 3. **User Experience**
- Animation rõ ràng: user dễ hiểu 2 nút từ đâu ra
- Bouncy effect làm UI cảm thấy responsive

### 4. **Code Simplicity**
- Không cần `AnimatedVisibility` wrapper
- Không cần `ExpandableMenuItem` helper composable
- Logic đơn giản hơn: 2 FAB + animation states

## 🐛 Edge Cases Handled

### 1. **Ghost clicks khi đóng**
```kotlin
onClick = { if (isMenuOpen) { ... } }
// Chỉ xử lý click khi menu đang mở
```

### 2. **Z-order đúng**
```kotlin
// Render order: Camera → AI → Main FAB
// Đảm bảo FAB chính luôn trên cùng
```

### 3. **Animation sync**
```kotlin
// Tất cả animations dùng cùng isMenuOpen state
// → Luôn đồng bộ với nhau
```

## 📱 Testing

### Test scenarios:
1. ✅ Tap FAB → 2 nút bung ra mượt
2. ✅ Tap FAB lại → 2 nút thu về mượt
3. ✅ Tap overlay → Menu đóng
4. ✅ Tap Camera khi mở → Navigate đúng
5. ✅ Tap AI khi mở → Navigate đúng
6. ✅ Không click được sub-buttons khi đóng
7. ✅ Spring bounce natural (không quá nhiều)
8. ✅ FAB rotation smooth
9. ✅ Icon transition + → × rõ ràng

## 📐 Tuning Parameters

Nếu muốn điều chỉnh animation:

```kotlin
// Tăng/giảm độ bouncy:
dampingRatio = Spring.DampingRatioMediumBouncy  // Thử: Low, Medium, High

// Tăng/giảm tốc độ:
stiffness = Spring.StiffnessMediumLow  // Thử: Low, Medium, High

// Thay đổi khoảng cách bung ra:
cameraOffsetX: -110.dp  // Giảm = gần hơn, tăng = xa hơn
subOffsetY: -90.dp      // Giảm = thấp hơn, tăng = cao hơn

// Thay đổi tốc độ fade:
tween(durationMillis = 180)  // Giảm = nhanh hơn
```

## 🎨 Customization

### Thay đổi màu:
```kotlin
// Camera button
containerColor = Color(0xFFE91E8C)  // Hồng đậm

// AI button
containerColor = Color(0xFFD63384)  // Hồng tím

// Có thể đổi thành:
MaterialTheme.colorScheme.tertiary
MaterialTheme.colorScheme.secondary
// Hoặc custom color khác
```

### Thay đổi icon:
```kotlin
Icons.Filled.CameraAlt     // → Icons.Filled.PhotoCamera
Icons.Filled.AutoAwesome   // → Icons.Filled.SmartToy
```

---

**Version:** 2.0 (Burst Animation)  
**Updated:** 2026-05-03  
**Previous:** Staggered slide with labels

