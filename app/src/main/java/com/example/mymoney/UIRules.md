# UI Rules & Guidelines

## Quy Tắc UI & Hướng Dẫn

---

## 1. Layout Rules

### English
- **MUST** use `Column`, `Row`, `Box` as primary layout containers
- **MUST NOT** nest layouts deeper than 3 levels
- **MUST** provide meaningful spacing between elements using consistent values
- Complex layouts **MUST** include detailed comments explaining the structure

### Tiếng Việt
- **PHẢI** sử dụng `Column`, `Row`, `Box` làm các container layout chính
- **KHÔNG ĐƯỢC** lồng layout sâu hơn 3 cấp
- **PHẢI** cung cấp khoảng cách có ý nghĩa giữa các phần tử bằng các giá trị nhất quán
- Các layout phức tạp **PHẢI** có chú thích chi tiết giải thích cấu trúc

> **Lưu ý:** Layout sâu gây hiệu suất kém và khó bảo trì. Hãy tái cấu trúc nếu cần thiết.

---

## 2. Responsive Design

### English
- **MUST NOT** use hardcoded fixed sizes (except for icons and specific UI elements)
- **MUST** use `fillMaxWidth()`, `fillMaxHeight()`, `weight()` for responsive behavior
- **MUST** support multiple screen sizes: Phone (< 600dp), Tablet (600-900dp), Large (> 900dp)
- **SHOULD** test on devices with different screen densities
- **MUST** use `WindowSizeClass` or `currentWindowAdaptiveInfo()` for adaptive layouts

### Tiếng Việt
- **KHÔNG ĐƯỢC** sử dụng kích thước cố định (ngoại trừ biểu tượng và các yếu tố UI cụ thể)
- **PHẢI** sử dụng `fillMaxWidth()`, `fillMaxHeight()`, `weight()` để có hành vi responsive
- **PHẢI** hỗ trợ nhiều kích thước màn hình: Điện thoại (< 600dp), Tablet (600-900dp), Lớn (> 900dp)
- **NÊN** kiểm tra trên các thiết bị có mật độ màn hình khác nhau
- **PHẢI** sử dụng `WindowSizeClass` hoặc `currentWindowAdaptiveInfo()` cho các layout thích ứng

> **Cảnh báo:** Kích thước cố định phá vỡ trải nghiệm trên các thiết bị khác nhau.

---

## 3. State Management

### English
- **MUST** keep UI completely stateless whenever possible
- **MUST** use `StateFlow` or `State` to manage UI state in ViewModel
- **MUST** implement state hoisting: move state to parent composable or ViewModel
- **MUST NOT** include business logic inside Composables
- **MUST** use `collectAsStateWithLifecycle()` to collect flows in UI layer

### Tiếng Việt
- **PHẢI** giữ UI hoàn toàn stateless nếu có thể
- **PHẢI** sử dụng `StateFlow` hoặc `State` để quản lý trạng thái UI trong ViewModel
- **PHẢI** triển khai state hoisting: di chuyển state sang composable cha hoặc ViewModel
- **KHÔNG ĐƯỢC** đặt logic kinh doanh bên trong Composables
- **PHẢI** sử dụng `collectAsStateWithLifecycle()` để thu thập flows trong UI layer

> **Quy tắc vàng:** Composables chỉ hiển thị, không lưu trữ logic hoặc dữ liệu.

---

## 4. Architecture Separation

### English
- **MUST** follow strict layering: `Presentation (UI) → ViewModel → Domain (UseCase) → Data (Repository)`
- **MUST NOT** call API or database directly from UI or ViewModel
- **MUST NOT** instantiate Repository or UseCase in Composables
- **MUST** use Dependency Injection to provide dependencies
- UI layer **MUST** only communicate through ViewModel
- ViewModel **MUST NOT** contain UI logic (formatting, styling decisions)

### Tiếng Việt
- **PHẢI** tuân theo phân lớp nghiêm ngặt: `Presentation (UI) → ViewModel → Domain (UseCase) → Data (Repository)`
- **KHÔNG ĐƯỢC** gọi API hoặc cơ sở dữ liệu trực tiếp từ UI hoặc ViewModel
- **KHÔNG ĐƯỢC** tạo Repository hoặc UseCase trong Composables
- **PHẢI** sử dụng Dependency Injection để cung cấp phụ thuộc
- UI layer **PHẢI** chỉ giao tiếp thông qua ViewModel
- ViewModel **KHÔNG ĐƯỢC** chứa logic UI (định dạng, quyết định styling)

> **Ghi nhớ:** Không vi phạm ranh giới lớp. Nó dẫn đến test khó khăn và code khó bảo trì.

---

## 5. Theming System

### English
- **MUST NOT** hardcode colors, typography, or spacing values
- **MUST** use `MaterialTheme.colorScheme` for colors
- **MUST** use `MaterialTheme.typography` for text styles
- **MUST** support Dark Mode and Light Mode automatically
- **SHOULD** support dynamic color (Material You) on Android 12+
- **MUST** define custom theme in `Theme.kt` using Material3 components
- **MUST** use `LocalContentColor`, `LocalTextStyle` for consistent styling

### Tiếng Việt
- **KHÔNG ĐƯỢC** mã hóa các giá trị màu, kiểu chữ hoặc khoảng cách
- **PHẢI** sử dụng `MaterialTheme.colorScheme` cho các màu
- **PHẢI** sử dụng `MaterialTheme.typography` cho các kiểu văn bản
- **PHẢI** hỗ trợ Dark Mode và Light Mode tự động
- **NÊN** hỗ trợ dynamic color (Material You) trên Android 12+
- **PHẢI** định nghĩa theme tùy chỉnh trong `Theme.kt` bằng các thành phần Material3
- **PHẢI** sử dụng `LocalContentColor`, `LocalTextStyle` để định kiểu nhất quán

> **Cảnh báo:** Hardcoded colors phá vỡ dark mode và tính nhất quán của design system.

---

## 6. Component Design

### English
- **MUST** create reusable components for repeated UI patterns
- **MUST NOT** duplicate UI code across screens
- **SHOULD** keep composables small and modular (max 150 lines)
- **MUST** extract complex logic into separate composable functions
- **MUST** pass UI state as parameters, never fetch inside composable
- **SHOULD** create generic components that can be customized via parameters

### Tiếng Việt
- **PHẢI** tạo các thành phần tái sử dụng cho các mẫu UI lặp lại
- **KHÔNG ĐƯỢC** sao chép mã UI trên các màn hình
- **NÊN** giữ các composables nhỏ và mô-đun hóa (tối đa 150 dòng)
- **PHẢI** trích xuất logic phức tạp thành các hàm composable riêng biệt
- **PHẢI** truyền trạng thái UI dưới dạng tham số, không bao giờ lấy bên trong composable
- **NÊN** tạo các thành phần chung có thể tùy chỉnh thông qua các tham số

> **Ghi nhớ:** Các thành phần tái sử dụng giúp bảo trì dễ dàng và nhất quán.

---

## 7. Spacing & Sizing

### English
- **MUST** use consistent spacing system: `4dp`, `8dp`, `12dp`, `16dp`, `24dp`, `32dp`
- **MUST NOT** use random or arbitrary spacing values
- **SHOULD** define spacing constants in `Dimensions.kt`
- **MUST** apply consistent padding/margin across all screens
- **MUST** follow Material3 spacing guidelines

### Tiếng Việt
- **PHẢI** sử dụng hệ thống khoảng cách nhất quán: `4dp`, `8dp`, `12dp`, `16dp`, `24dp`, `32dp`
- **KHÔNG ĐƯỢC** sử dụng các giá trị khoảng cách ngẫu nhiên hoặc tùy tiện
- **NÊN** định nghĩa các hằng số khoảng cách trong `Dimensions.kt`
- **PHẢI** áp dụng padding/margin nhất quán trên tất cả các màn hình
- **PHẢI** tuân theo hướng dẫn khoảng cách Material3

> **Lưu ý:** Khoảng cách không nhất quán tạo ra thiết kế lộn xộn và không chuyên nghiệp.

---

## 8. Naming Convention

### English
- **Screen Composables**: `XxxScreen` (e.g., `HomeScreen`, `ProfileScreen`)
- **Component Composables**: `XxxCard`, `XxxButton`, `XxxRow` (e.g., `UserCard`, `PrimaryButton`)
- **UI State Data Class**: `XxxUiState` (e.g., `HomeUiState`)
- **UI Events**: `XxxEvent` sealed class (e.g., `HomeEvent`)
- **Side Effects**: `XxxEffect` sealed class (e.g., `HomeEffect`)
- **ViewModel**: `XxxViewModel` (e.g., `HomeViewModel`)
- **UseCase**: `XxxUseCase` (e.g., `GetUserUseCase`)
- **Repository**: `XxxRepository` (e.g., `UserRepository`)

### Tiếng Việt
- **Screen Composables**: `XxxScreen` (ví dụ: `HomeScreen`, `ProfileScreen`)
- **Component Composables**: `XxxCard`, `XxxButton`, `XxxRow` (ví dụ: `UserCard`, `PrimaryButton`)
- **UI State Data Class**: `XxxUiState` (ví dụ: `HomeUiState`)
- **UI Events**: `XxxEvent` sealed class (ví dụ: `HomeEvent`)
- **Side Effects**: `XxxEffect` sealed class (ví dụ: `HomeEffect`)
- **ViewModel**: `XxxViewModel` (ví dụ: `HomeViewModel`)
- **UseCase**: `XxxUseCase` (ví dụ: `GetUserUseCase`)
- **Repository**: `XxxRepository` (ví dụ: `UserRepository`)

> **Quy tắc:** Tên rõ ràng giúp các lập trình viên khác hiểu mục đích ngay lập tức.

---

## 9. Accessibility (A11y)

### English
- **MUST** provide `contentDescription` for all `Image`, `Icon`, `Button` elements
- **MUST** ensure text contrast ratio meets WCAG AA standards (4.5:1 for text)
- **MUST** use semantic composables (`Button`, `IconButton`) instead of `Box` with `onClick`
- **MUST** support screen reader navigation
- **SHOULD** test with Android Accessibility Scanner
- **MUST** ensure touch targets are at least 48dp x 48dp

### Tiếng Việt
- **PHẢI** cung cấp `contentDescription` cho tất cả các phần tử `Image`, `Icon`, `Button`
- **PHẢI** đảm bảo tỷ lệ độ tương phản văn bản đáp ứng tiêu chuẩn WCAG AA (4.5:1 cho văn bản)
- **PHẢI** sử dụng các composables ngữ nghĩa (`Button`, `IconButton`) thay vì `Box` với `onClick`
- **PHẢI** hỗ trợ điều hướng trình đọc màn hình
- **NÊN** kiểm tra bằng Android Accessibility Scanner
- **PHẢI** đảm bảo mục tiêu cảm ứng có kích thước ít nhất 48dp x 48dp

> **Cảnh báo:** Không có contentDescription hoặc click target nhỏ phạt người dùng khuyết tật.

---

## 10. Performance

### English
- **MUST** avoid unnecessary recomposition by using `remember`, `mutableStateOf`
- **SHOULD** use `derivedStateOf` for computed values instead of calculating in body
- **MUST NOT** create new objects (lambdas, objects) repeatedly inside composable bodies
- **SHOULD** use `.also`, `.apply` to modify objects inline when necessary
- **MUST** avoid large list renders without `LazyColumn` or `LazyRow`
- **SHOULD** use `key()` when rendering dynamic lists to prevent recomposition
- **MUST NOT** call suspend functions directly in composable body

### Tiếng Việt
- **PHẢI** tránh recomposition không cần thiết bằng cách sử dụng `remember`, `mutableStateOf`
- **NÊN** sử dụng `derivedStateOf` cho các giá trị tính toán thay vì tính toán trong phần thân
- **KHÔNG ĐƯỢC** tạo các đối tượng mới (lambdas, objects) lặp đi lặp lại bên trong phần thân composable
- **NÊN** sử dụng `.also`, `.apply` để sửa đổi các đối tượng inline khi cần thiết
- **PHẢI** tránh render danh sách lớn mà không có `LazyColumn` hoặc `LazyRow`
- **NÊN** sử dụng `key()` khi render danh sách động để ngăn recomposition
- **KHÔNG ĐƯỢC** gọi các hàm suspend trực tiếp trong phần thân composable

> **Lưu ý:** Recomposition quá nhiều gây giảm hiệu suất và pin thoát nhanh.

---

## 11. Preview & Testing

### English
- **MUST** provide `@Preview` composable for every Screen and Component
- **MUST** show multiple UI states: Loading, Error, Success, Empty
- **SHOULD** create separate preview functions for different screen sizes
- **SHOULD** use `@PreviewFontScale`, `@PreviewDynamicColors` for theme testing
- **MUST** test previews on Light and Dark themes
- **MUST NOT** preview with hardcoded or mock data directly

### Tiếng Việt
- **PHẢI** cung cấp `@Preview` composable cho mỗi Screen và Component
- **PHẢI** hiển thị nhiều trạng thái UI: Loading, Error, Success, Empty
- **NÊN** tạo các hàm preview riêng biệt cho các kích thước màn hình khác nhau
- **NÊN** sử dụng `@PreviewFontScale`, `@PreviewDynamicColors` để kiểm tra theme
- **PHẢI** kiểm tra preview trên các theme Light và Dark
- **KHÔNG ĐƯỢC** xem trước bằng dữ liệu hardcoded hoặc mock trực tiếp

> **Ghi nhớ:** Previews tiếp dây là cách tốt nhất để kiểm tra UI nhanh chóng.

---

## 12. Unidirectional Data Flow (UDF)

### English
- **MUST** follow strict UDF pattern: `UI sends Event → ViewModel processes → State updates → UI recomposes`
- **MUST** define `sealed class UiEvent` for all user actions
- **MUST** define `data class UiState` (immutable) for UI state
- **SHOULD** define `sealed class UiEffect` for side effects (Navigation, Toast)
- **MUST NOT** send data back from UI to ViewModel except through events
- ViewModel **MUST** not expose mutable state directly

### Tiếng Việt
- **PHẢI** tuân theo mẫu UDF nghiêm ngặt: `UI gửi Event → ViewModel xử lý → State cập nhật → UI recomposes`
- **PHẢI** định nghĩa `sealed class UiEvent` cho tất cả các hành động người dùng
- **PHẢI** định nghĩa `data class UiState` (immutable) cho trạng thái UI
- **NÊN** định nghĩa `sealed class UiEffect` cho các side effects (Navigation, Toast)
- **KHÔNG ĐƯỢC** gửi dữ liệu trở lại từ UI sang ViewModel ngoài các events
- ViewModel **KHÔNG ĐƯỢC** hiển thị trạng thái có thể thay đổi trực tiếp

> **Quy tắc vàng:** UDF là nền tảng của ứng dụng có thể test được và bảo trì tốt.

---

## 13. Single Source of Truth (SSOT)

### English
- **MUST** store all UI state in ViewModel, never in Composable
- **MUST NOT** duplicate state between ViewModel and Composable
- **MUST** use `StateFlow` or `LiveData` to expose state from ViewModel
- **MUST** ensure state is only modified by ViewModel
- If UI needs to cache data, it **MUST** still come from ViewModel

### Tiếng Việt
- **PHẢI** lưu trữ tất cả trạng thái UI trong ViewModel, không bao giờ trong Composable
- **KHÔNG ĐƯỢC** sao chép trạng thái giữa ViewModel và Composable
- **PHẢI** sử dụng `StateFlow` hoặc `LiveData` để hiển thị trạng thái từ ViewModel
- **PHẢI** đảm bảo trạng thái chỉ được sửa đổi bởi ViewModel
- Nếu UI cần lưu trữ dữ liệu, nó **PHẢI** vẫn đến từ ViewModel

> **Cảnh báo:** Trạng thái trùng lặp dẫn đến bugs khó tìm và dữ liệu không đồng bộ.

---

## 14. Error & Loading Handling

### English
- **MUST** handle three states: Loading, Success, Error
- **MUST** show loading indicator (Progress, Shimmer) when `uiState.loading == true`
- **MUST** display error message with retry option when error occurs
- **MUST** show empty state when data list is empty
- **MUST NOT** show loading and data simultaneously
- Error messages **MUST** be user-friendly and actionable

### Tiếng Việt
- **PHẢI** xử lý ba trạng thái: Loading, Success, Error
- **PHẢI** hiển thị chỉ báo loading (Progress, Shimmer) khi `uiState.loading == true`
- **PHẢI** hiển thị thông báo lỗi với tùy chọn thử lại khi lỗi xảy ra
- **PHẢI** hiển thị trạng thái trống khi danh sách dữ liệu trống
- **KHÔNG ĐƯỢC** hiển thị loading và dữ liệu cùng một lúc
- Thông báo lỗi **PHẢI** thân thiện với người dùng và có thể hành động được

> **Ghi nhớ:** Xử lý lỗi tốt là dấu hiệu của ứng dụng chất lượng cao.

---

## 15. Immutable State

### English
- **MUST** define `UiState` as immutable `data class`
- **MUST** use `copy()` function to update state properties
- **MUST NOT** use mutable properties inside state
- **MUST** treat state as read-only in UI layer
- If mutation is needed, delegate to ViewModel

### Tiếng Việt
- **PHẢI** định nghĩa `UiState` là immutable `data class`
- **PHẢI** sử dụng hàm `copy()` để cập nhật các thuộc tính state
- **KHÔNG ĐƯỢC** sử dụng các thuộc tính có thể thay đổi bên trong state
- **PHẢI** coi state là read-only trong UI layer
- Nếu cần mutation, phó thác cho ViewModel

> **Lưu ý:** Immutable state giúp tránh bugs do thay đổi ngoài mong đợi.

---

## 16. Dependency Injection

### English
- **MUST** use DI framework (Hilt or Koin) for all dependencies
- **MUST NOT** instantiate ViewModel manually
- **MUST** inject dependencies into ViewModel constructor
- ViewModel **MUST NOT** create Repository, UseCase, or DataSource manually
- **SHOULD** use `@HiltViewModel` annotation for ViewModel
- **MUST** define DI modules in separate files
- All layers **MUST** receive dependencies via constructor injection

### Tiếng Việt
- **PHẢI** sử dụng DI framework (Hilt hoặc Koin) cho tất cả các phụ thuộc
- **KHÔNG ĐƯỢC** tạo ViewModel theo cách thủ công
- **PHẢI** inject phụ thuộc vào constructor của ViewModel
- ViewModel **KHÔNG ĐƯỢC** tạo Repository, UseCase hoặc DataSource theo cách thủ công
- **NÊN** sử dụng `@HiltViewModel` annotation cho ViewModel
- **PHẢI** định nghĩa các DI modules trong các tệp riêng biệt
- Tất cả các lớp **PHẢI** nhận phụ thuộc thông qua constructor injection

> **Quy tắc:** DI làm code dễ test và thay thế dependencies.

---

## 17. Project Structure

### English
**MUST** organize code into strict layers:

```
app/
├── src/main/java/com/example/mymoney/
│
├── presentation/
│   ├── ui/
│   │   ├── screen/
│   │   │   ├── home/
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── HomeViewModel.kt
│   │   │   │   ├── HomeState.kt
│   │   │   │   └── HomeEvent.kt
│   │   │   └── ...
│   │   ├── component/
│   │   ├── theme/
│   │   └── navigation/
│
├── domain/
│   ├── usecase/
│   ├── repository/
│   └── model/
│
└── data/
    ├── repository/
    ├── datasource/
    └── model/
```

### Tiếng Việt
**PHẢI** tổ chức code thành các lớp nghiêm ngặt như ở trên

> **Ghi nhớ:** Cấu trúc dự án rõ ràng giúp điều hướng code dễ dàng.

---

## 18. Side Effects Management

### English
- **MUST NOT** include navigation, Toast, Snackbar logic in `UiState`
- **MUST** use separate `UiEffect` sealed class for side effects
- **SHOULD** use `SharedFlow` or `Channel` to emit effects
- **MUST** consume effects only once (avoid duplication on recomposition)
- **MUST** launch effects in `LaunchedEffect` with proper key
- Side effects **MUST** not block UI recomposition

### Tiếng Việt
- **KHÔNG ĐƯỢC** đặt logic navigation, Toast, Snackbar trong `UiState`
- **PHẢI** sử dụng `UiEffect` sealed class riêng biệt cho side effects
- **NÊN** sử dụng `SharedFlow` hoặc `Channel` để phát side effects
- **PHẢI** tiêu thụ side effects chỉ một lần (tránh trùng lặp khi recompose)
- **PHẢI** khởi chạy side effects trong `LaunchedEffect` với khóa thích hợp
- Side effects **KHÔNG ĐƯỢC** chặn UI recomposition

> **Cảnh báo:** Không xử lý side effects đúng cách gây hiệu ứng phụ bất ngờ.

---

## Summary Table

| Aspect | Rule | Status |
|--------|------|--------|
| Layout | Use Column, Row, Box; avoid deep nesting | MUST |
| Responsive | No fixed sizes; use fillMaxWidth, weight | MUST |
| State | Stateless UI; use StateFlow in ViewModel | MUST |
| Architecture | Strict layering: UI → VM → UC → Repo | MUST |
| Theme | Use MaterialTheme; no hardcoded colors | MUST |
| Component | Reusable, modular, max 150 lines | MUST |
| Spacing | Use 4dp, 8dp, 16dp, 24dp system | MUST |
| Naming | XxxScreen, XxxCard, XxxUiState pattern | MUST |
| A11y | contentDescription, 48dp touch target | MUST |
| Performance | Avoid recomposition; use remember, key | MUST |
| Preview | @Preview every screen/component | MUST |
| UDF | Event → ViewModel → State → UI | MUST |
| SSOT | All state in ViewModel | MUST |
| Error | Handle Loading, Error, Success, Empty | MUST |
| Immutable | data class + copy() for state updates | MUST |
| DI | Use Hilt/Koin; no manual instantiation | MUST |
| Structure | presentation/domain/data separation | MUST |
| Effects | Use UiEffect; side effects not in UiState | MUST |

---

## Final Reminders

- **Consistency is key:** Follow all rules across all screens and components
- **Code review:** Every PR **MUST** be reviewed against these rules
- **Team alignment:** All team members **MUST** understand and accept these rules
- **Continuous improvement:** Rules **SHOULD** be revisited and updated quarterly
- **Documentation:** Keep UI Rules up-to-date with new patterns and best practices

> **Quy tắc cuối cùng:** Code không tuân thủ UI Rules **KHÔNG ĐƯỢC** merge vào main branch. Nếu cần ngoại lệ, yêu cầu team lead phê duyệt.

---

**Document Version:** 1.0  
**Last Updated:** March 2026  
**Language:** English + Tiếng Việt
