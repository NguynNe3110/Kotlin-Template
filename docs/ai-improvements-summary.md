# Báo cáo tổng hợp toàn bộ các cải tiến kiến trúc Kotlin MVVM Base

> **Dành cho App Developer / Product Engineer:** Tài liệu này tóm tắt toàn bộ những gì AI và các phiên làm việc trong quá khứ đã xây dựng và cải tiến trong dự án. Bản tóm tắt được viết dưới góc nhìn luồng ứng dụng (**App Flow**), giải thích: *Vấn đề trước đây là gì? AI đã giải quyết thế nào? Trong code đang có những class nào? Và khi bạn làm tính năng mới thì sử dụng ra sao?*

---

## 1. Bản đồ tổng quan dự án hiện tại

Hiện tại, codebase được tổ chức thành **5 module độc lập** với ranh giới nghiêm ngặt được kiểm tra tự động qua task `verifyModuleBoundaries`:

```
                 ┌────────────────────────────────┐
                 │          :app (Android)        │
                 │ UI, Compose, ViewModels, DI    │
                 └──────┬───────────────┬─────────┘
                        │               │
       ┌────────────────▼──────┐ ┌──────▼───────────────┐
       │   :domain (Pure JVM)  │ │  :resources (Android)│
       │ Models, UseCase IO,   │ │ Themes, Colors,      │
       │ Repository Interfaces │ │ Strings (en / vi)    │
       └──────────────▲────────┘ └──────────────────────┘
                      │
       ┌──────────────┴────────┐
       │    :data (Pure JVM)   │
       │ REST, GraphQL, Firebase│
       │ Supabase Fake Adapters │
       └──────────────┬────────┘
                      │
       ┌──────────────▼────────┐
       │   :shared (Pure JVM)  │
       │ Concurrency, Action,  │
       │ Dispatchers, Errors   │
       └───────────────────────┘
```

- **Tổng số Unit Test hiện có:** **140 tests PASS (0 failures, 0 errors)** bao phủ cả 5 modules và 4 build variants.

---

## 2. Toàn bộ lịch sử cải tiến qua các phiên (Past to Present)

### Giai đoạn 1: Thiết lập nền móng Clean Architecture (Commit `e61e593`)
- **Trước đó:** Chưa có cấu trúc dự án chuẩn, các thành phần bị phụ thuộc lộn xộn.
- **AI đã làm:**
  1. Tách dự án thành 5 module: `:app`, `:domain`, `:data`, `:shared`, `:resources`.
  2. Tạo task `verifyModuleBoundaries` trong root `build.gradle.kts` để chặn việc import chéo trái phép giữa các tầng.
  3. Xây dựng use case mẫu (`AddTodoUseCase`, `GetTodosUseCase`, `GetUsersUseCase`, v.v.).
  4. Triển khai tầng Data với Retrofit (`JsonPlaceholderApi`), InMemory Cache, và DataStore Session Manager để tự động lưu/khôi phục phiên đăng nhập.
  5. Setup Jetpack Compose với giao diện mẫu Todo và Users.

---

### Giai đoạn 2: Chuẩn hóa hợp đồng Input/Output (Commit `85b6743`)
- **Vấn đề trước đây:**
  - Use case nhận tham số tự do (ví dụ `AddTodoUseCase.invoke(title: String)`) hoặc trả về entity thô.
  - Khi nghiệp vụ mở rộng thêm tham số (ví dụ: title, description, priority), chữ ký hàm bị vỡ, phải sửa nhiều nơi.
- **AI đã làm:**
  1. Đưa vào hợp đồng `BaseInput`, `BaseOutput`, `NoInput`, `NoOutput` trong `domain/usecase/base/`.
  2. Mọi use case đều có Input/Output rõ ràng: `AddTodoInput(title)` ➔ `AddTodoOutput(todo)`.
  3. Tách biệt `ValidationError` khỏi ngôn ngữ hiển thị: lỗi nghiệp vụ chỉ mang mã code (`EMPTY_TITLE`, `FIELD_REQUIRED`) và metadata, không chứa câu chữ cứng.

---

### Giai đoạn 3: Cuộc cách mạng tiến hóa toàn diện (Commit `e031db8`)
Đây là phiên làm việc lớn nhất, giải quyết toàn bộ các yêu cầu cao cấp tương đương các base Flutter/Android xịn nhất:

#### 3.1. Build Variants & Cấu hình môi trường (P1)
- **Vấn đề trước đây:** App chỉ có `debug` và `release`. URL server bị hardcode trong `DataModule`. Bản test cài đè lên bản dùng thật.
- **AI đã làm:**
  - Tạo 4 Build Variants với `Application ID` riêng biệt:
    - `devDebug` (`com.example.base.dev.debug`): Lập trình hàng ngày với backend Dev.
    - `devRelease` (`com.example.base.dev`): Test hiệu năng release với backend Dev.
    - `prodDebug` (`com.example.base.debug`): Bật log chẩn đoán lỗi trên backend Production.
    - `prodRelease` (`com.example.base`): Bản phát hành chính thức cho người dùng.
  - Có thể cài song song cả 2 bản Dev và Prod trên cùng 1 điện thoại mà không bị ghi đè.
  - Tạo `AppConfig` (`app/config/AppConfig.kt`) tự động validate URL (yêu cầu http/https, có dấu `/` cuối) và inject động qua Hilt, xóa bỏ 100% hardcode URL.

#### 3.2. Toolkit ViewModel độc lập - Không ép kế thừa (P3)
- **Vấn đề trước đây:** Mọi ViewModel muốn có loading, hiển thị dialog lỗi hay retry đều buộc phải kế thừa `BaseViewModel`. Nếu một ViewModel cần kế thừa class khác hoặc muốn viết đơn giản thì bị bế tắc.
- **AI đã làm:**
  - Tách lõi xử lý thành các helper dùng độc lập:
    - `ActionRunner` (`shared/action/ActionRunner.kt`): Chuyên chạy tác vụ bất đồng bộ, quản lý loading, bắt lỗi và cơ chế retry.
    - `LoadingManager` (`shared/action/LoadingManager.kt`): Đếm số lượng tác vụ đang chạy theo `LoadingKey`. Khi có 3 tác vụ chạy cùng lúc, chỉ khi cả 3 xong thì loading overlay mới tắt.
    - `ScreenUiController` & `ErrorPresentationMode` (`app/ui/base/`): Màn hình có thể chọn cách hiển thị lỗi linh hoạt: Hiện Dialog, Snackbar, báo đỏ ngay dưới ô nhập (Inline), hoặc hiện toàn màn hình (ScreenError).
  - Tạo `UserViewModel` (`app/ui/user/UserViewModel.kt`) kế thừa trực tiếp `androidx.lifecycle.ViewModel` chuẩn của Android nhưng vẫn dùng trọn vẹn toàn bộ tính năng trên qua composition.

#### 3.3. Xử lý luồng Concurrency tương đương RxDart (P5)
- **Vấn đề trước đây:** Kotlin coroutines thiếu các chính sách luồng tiện dụng như trong RxDart / Flutter BLoC (như click tặc bị gọi 2 lần, gõ ô tìm kiếm bị race condition response cũ ghi đè response mới).
- **AI đã làm:** Bổ sung bộ công cụ trong `shared/flow/`:
  - `throttleFirst`: Nhận sự kiện đầu tiên, bỏ qua các lần bấm tiếp theo trong khoảng thời gian định trước (chống click đúp mở 2 màn hình).
  - `droppable`: Khi tác vụ đang chạy thì bỏ qua toàn bộ lần gọi mới cho đến khi xong hẳn.
  - `debounceWithLatest`: Gõ phím tìm kiếm chờ ngừng gõ mới gọi API, và tự hủy request trước nếu có ký tự mới.
  - `sequential`: Hàng đợi xử lý tuần tự từng tác vụ một.

#### 3.4. Đa ngôn ngữ i18n xuyên suốt (P4)
- **Vấn đề trước đây:** Lỗi từ server hoặc domain hay bị hardcode tiếng Anh hoặc tiếng Việt lẫn lộn.
- **AI đã làm:**
  - Tạo `UiText` (`UiText.StringResource` / `UiText.DynamicString`): Trì hoãn việc lấy chuỗi cho đến khi ra tới màn hình Compose. Khi người dùng đổi ngôn ngữ máy, chuỗi tự động đổi theo mà không cần khởi động lại logic.
  - Tạo `UiErrorMessageMapper`: Tự động dịch các mã lỗi (`network_error`, `session_expired`, `validation_error`) sang string tương ứng trong `resources/values/strings.xml` và `resources/values-vi/strings.xml`.

#### 3.5. Hợp đồng backend ngoài REST (P6)
- **Vấn đề trước đây:** Tầng data bị dính chặt với Retrofit HTTP REST API.
- **AI đã làm:**
  - Tạo fake clients và adapters trong `data/integration/`:
    - `GraphQLAdapter`: Xử lý query/mutation, phân biệt lỗi transport vs lỗi partial data.
    - `SupabaseAdapter`: Xử lý CRUD, Auth và lỗi Row Level Security (RLS).
    - `FirebaseAdapter`: Xử lý Firestore CRUD và tự dọn dẹp realtime listener khi ViewModel bị hủy.
  - Viết 29 unit tests chứng minh tầng Domain có thể đổi backend sang GraphQL/Firebase/Supabase mà không cần sửa 1 dòng code nghiệp vụ nào.

#### 3.6. CI Automation (P8)
- **AI đã làm:** Tạo file `.github/workflows/ci.yml` tự động kiểm tra: ranh giới module, chạy unit test trên cả 5 module, và build thử các variant khi có PR.

---

### Giai đoạn 4: Hoàn thiện 5 điểm thực chiến chuyên sâu (Commit `7a5cce0` & hiện tại)
Dựa trên phản hồi thực tế từ bạn, AI đã sửa dứt điểm 5 vấn đề cốt lõi:

1. **Sửa hàng đợi `Sequential.kt`:** Bổ sung suspend function `enqueue(...)`/`send(...)` để khi buffer đầy thì coroutine tự động tạm dừng (suspend) chờ chỗ trống, không còn tình trạng bị rơi mất tác vụ âm thầm như khi dùng `trySend()`.
2. **Chặn release nhầm backend demo (`build.gradle.kts`):** Tạo task `checkProdReleaseConfig`. Nếu bạn build `prodRelease` mà quên cấu hình URL thật (vẫn để fallback về JSONPlaceholder), Gradle sẽ lập tức ném lỗi chặn lại ngay, trừ khi bạn cố ý bật cờ `-PallowDemoProd=true`.
3. **i18n đi tới tận giao diện:** Đảm bảo `AppEffectHandler`, `BaseScreen`, `UsersScreen` đều nhận `Context` để dịch thông báo snackbar/dialog theo đúng ngôn ngữ đang kích hoạt. Đưa thông báo lỗi rỗng ô tìm kiếm vào file `strings.xml`.
4. **Sửa lỗi nhấp nháy/restart snackbar (`BaseScreen.kt`):** Chỉnh `LaunchedEffect` chỉ lắng nghe ID của effect đầu tiên (`pendingEffects.firstOrNull()?.id`). Khi có snackbar thứ 2 tới, snackbar thứ 1 đang hiện không bị hủy rồi nhảy lại từ đầu.
5. **Chống race-condition tìm kiếm (`UserViewModel.kt`):** Thêm biến `latestRequestId` (`AtomicLong`). Khi người dùng gõ chữ nhanh hoặc vừa gõ vừa bấm refresh: request cũ đang chạy sẽ bị hủy ngay, và nếu kết quả cũ có lỡ trả về muộn hơn thì cũng bị loại bỏ, bảo đảm trên màn hình luôn hiển thị đúng kết quả của lần tìm kiếm mới nhất.

---

### Giai đoạn 5: Quy hoạch Bộ Collection UI & Phân trang dùng chung (P9)
- **Vấn đề trước đây:**
  - Các màn hình `TodoScreen` và `UsersScreen` đang gọi `LazyColumn` trực tiếp trong từng màn hình, chưa có bộ UI collection dùng chung có thể tái sử dụng.
  - Khi phân trang (`Users`), app dùng nút "Load More" thủ công; nếu việc tải trang tiếp theo bị lỗi thì chỉ bắn snackbar rời rạc mà không có nút "Thử lại" ngay tại cuối danh sách (footer retry).
  - Khi người dùng đổi từ khóa tìm kiếm nhanh, nếu không có cơ chế `generationId` đồng bộ, kết quả trang cũ có thể bị nối nhầm vào danh sách mới.
- **AI đã quy hoạch trong P9:**
  1. **Bộ 4 Composable Collection dùng chung (`app/ui/components/collection/`):**
     - `AppList<T>` & `AppGrid<T>`: Dành cho danh sách/lưới thường, tích hợp sẵn pull-to-refresh, empty slot, loading slot và error retry slot.
     - `AppPagedList<T>` & `AppPagedGrid<T>`: Dành cho danh sách/lưới phân trang lớn, tự động phát hiện cuộn gần cuối để tải thêm (scroll prefetch), có footer loading indicator, inline footer error retry và slot báo hết trang.
  2. **Mô hình trạng thái `PagedListUiState<T>`:** Quản lý độc lập 3 pha tải (`firstLoadState`, `refreshState`, `appendLoadState`) cùng `generationId` để triệt tiêu race condition.
  3. **Kiến trúc Dual-Track Paging Engine:** Chọn Track 1 (Pure State Machine trên Coroutines) làm mặc định cho core để giữ base nhẹ, không phụ thuộc thư viện ngoài; đồng thời mở cổng Track 2 (Adapter cho Jetpack Paging 3) khi cần mở rộng offline cache lớn.
  4. **Chống tràn bộ nhớ Android:** Không lưu `List<T>` lớn vào `SavedStateHandle` (tránh `TransactionTooLargeException`), chỉ lưu `page`, `query`, và `scrollOffset`.

---

## 3. "Cheat Sheet" cho App Developer: Khi làm app flow thì xài thế nào?

Bạn không cần nhớ hết 140 bài test hay chi tiết bên dưới. Khi cần viết tính năng mới, hãy dùng theo các mẫu sau:

### Cách 1: Tạo UseCase mới
Tạo trong `domain/src/main/java/com/example/base/domain/usecase/`:
```kotlin
// 1. Định nghĩa Input và Output
data class LoginInput(val email: String, val pass: String) : BaseInput
data class LoginOutput(val token: String, val user: User) : BaseOutput

// 2. Kế thừa BaseUseCase
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    dispatcherProvider: DispatcherProvider,
    logger: AppLogger
) : BaseUseCase<LoginInput, LoginOutput>(dispatcherProvider, logger) {
    override suspend fun execute(input: LoginInput): Result<LoginOutput> {
        if (input.email.isBlank()) {
            return Result.failure(ValidationError(field = "email", code = "REQUIRED"))
        }
        return authRepository.login(input.email, input.pass)
    }
}
```

---

### Cách 2: Viết ViewModel và gọi UseCase có Loading & Error
Bạn có thể chọn 1 trong 2 phong cách:

#### Phong cách A: Dùng `BaseViewModel` (Nhanh, tiện lợi)
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {

    fun onLoginClick(email: String, pass: String) {
        // Tự động bật loading, tự động bắt lỗi hiện Dialog/Snackbar nếu thất bại
        launchAction(
            loadingKey = LoadingKey.Global, // Hoặc LoadingKey.Local("login")
            errorMode = ErrorPresentationMode.Snackbar
        ) {
            val result = loginUseCase(LoginInput(email, pass))
            result.onSuccess { output ->
                sendEffect(UiEffect.Navigate("home"))
            }
        }
    }
}
```

#### Phong cách B: Kế thừa `ViewModel()` thông thường (Composition)
Xem code mẫu chuẩn tại: `app/src/main/java/com/example/base/ui/user/UserViewModel.kt`.
Bạn chỉ cần inject hoặc khởi tạo `ActionRunner`, gọi `actionRunner.launch(...)` là có đầy đủ loading, retry và error handling mà không cần kế thừa `BaseViewModel`.

---

### Cách 3: Chống bấm đúp (Click tặc) trong UI
Trong Compose screen, dùng `throttleFirst` hoặc `droppable`:
```kotlin
// Trong ViewModel:
val onSubmitClick = droppable(viewModelScope) {
    // Nếu user bấm 5 lần liên tục khi hàm này đang chạy, 4 lần sau sẽ bị bỏ qua
    saveDataUseCase(NoInput)
}
```

---

### Cách 4: Hiển thị giao diện màn hình với `BaseScreen`
Trong file UI Compose (`app/src/main/java/com/example/base/ui/...`):
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    BaseScreen(
        viewModel = viewModel, // Hoặc truyền ScreenUiController riêng
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
    ) { paddingValues ->
        // Nội dung màn hình của bạn
        // Loading overlay và dialog lỗi sẽ tự động hiển thị đè lên khi có sự kiện!
    }
}
```

---

### Cách 5: Sử dụng Collection UI & Danh sách Phân trang (P9)

#### A. Danh sách thông thường với `AppList` (Ví dụ Todo, Cài đặt):
```kotlin
AppList(
    items = uiState.todos,
    loadState = uiState.loadState,
    onRefresh = { viewModel.refresh() },
    emptySlot = { Text("Chưa có công việc nào!") },
    errorSlot = { error, retry ->
        Button(onClick = retry) { Text("Thử lại") }
    }
) { todo ->
    TodoItemRow(todo = todo)
}
```

#### B. Danh sách phân trang với `AppPagedList` (Ví dụ Users, Tin tức, Sản phẩm):
```kotlin
AppPagedList(
    pagedState = uiState.pagedUsers, // PagedListUiState<User>
    onLoadMore = { viewModel.loadNextPage() },
    onRefresh = { viewModel.refresh() },
    prefetchDistance = 3, // Tự động tải tiếp khi còn cách cuối danh sách 3 items
    emptySlot = { Text("Không tìm thấy người dùng") },
    appendErrorSlot = { error, retry ->
        // Nút thử lại hiển thị ngay ở cuối danh sách mà không mất các trang trước
        TextButton(onClick = retry) { Text("Tải trang tiếp thất bại. Bấm để thử lại!") }
    },
    endOfPaginationSlot = { Text("Đã hiển thị toàn bộ danh sách.") }
) { user ->
    UserItemRow(user = user)
}
```

---

## 4. Tóm tắt các tài liệu kỹ thuật liên quan trong thư mục `docs/`

- **[docs/base-evolution-plan.md](file:///D:/AppData/Code/base/kotlin_mvvm_base/docs/base-evolution-plan.md):** Kế hoạch kiến trúc chi tiết từ P0 đến P9, phân định rõ ràng các tính năng đã kiểm chứng (140 tests) vs quy hoạch bộ Collection & Pagination UI (P9) và lộ trình tương lai.
- **[docs/architecture.md](file:///D:/AppData/Code/base/kotlin_mvvm_base/docs/architecture.md):** Bản mô tả kiến trúc 5 module, ranh giới dependency và composition root Hilt.
- **[docs/testing.md](file:///D:/AppData/Code/base/kotlin_mvvm_base/docs/testing.md):** Hướng dẫn chạy 140 bài test tự động qua dòng lệnh Gradle trên Windows PowerShell.
- **[docs/product-driven-roadmap.md](file:///D:/AppData/Code/base/kotlin_mvvm_base/docs/product-driven-roadmap.md):** Lộ trình phát triển sản phẩm thực chiến (Room database, single-flight refresh token, KeyStore encryption).
