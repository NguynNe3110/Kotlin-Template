# Review và kế hoạch logging theo môi trường

Ngày: **17/11/2026**. Trạng thái: **Đã triển khai và kiểm chứng local** — chỉ devDebug bật developer logs; các giới hạn ghi ở mục 7.

[Review v1](base-v1-review-plan.md) · [Wiring DI hiện tại](di-modularization-plan.md)

## 1. Yêu cầu và kết luận

Người dùng chỉ có một backend URL/database, muốn bản phát triển có log để chẩn đoán, prod không ghi developer logs. Không cần tạo thêm backend dev hoặc database dev để làm được điều này.

Trước sửa, DI luôn cấp ConsoleLogger. Hiện AppConfig.developerLoggingEnabled = environment.isDev && isDebug; AppModule chọn ConsoleLogger chỉ cho devDebug, NoOpLogger cho các variant còn lại.

Đã bỏ email/tên và raw throwable khỏi log AuthRepositoryImpl. ConsoleLogger chỉ in loại exception, không in message/cause/stack trace. Đã bỏ dump ProductFilter (có search query) và tham số route khỏi diagnostic logs; metadata còn lại vẫn cần được kiểm soát khi thêm call site mới.

Đã sửa code và chạy kiểm tra. Không đổi endpoint/database/toolchain; test HTTP chỉ dùng MockWebServer local và dữ liệu giả. Chưa kiểm Logcat hoặc UI trên thiết bị.

## 2. Bằng chứng source trước khi sửa

| Vị trí | Hiện trạng |
| --- | --- |
| app/.../di/AppModule.kt:42 | logger(): AppLogger luôn trả ConsoleLogger(), không nhận AppConfig |
| shared/.../logging/AppLogger.kt | Có ConsoleLogger và NoOpLogger; ConsoleLogger gọi println ở mọi level |
| app/.../config/AppConfig.kt | Có environment và isDebug, nhưng chưa có logging policy |
| app/build.gradle.kts | Có environment flavors và DEBUG theo build type; chưa có logging flag/policy; release minify=false |
| composition/.../network/NetworkModule.kt | Public OkHttp client không có HttpLoggingInterceptor |
| composition/.../auth/AuthNetworkModule.kt | Protected client kế thừa public client và thêm AuthInterceptor; chưa thêm HTTP logging |
| data/.../repository/AuthRepositoryImpl.kt | Login log email; register log email/name; failure messages cũng có email; chưa thấy password/token bị in trực tiếp trong các dòng đã đọc |
| data/.../repository/TodoRepositoryImpl.kt | Log titleLength/id; không log nguyên title |
| Source search main Kotlin | Không tìm thấy HttpLoggingInterceptor, Timber, android.util.Log hoặc System.out ngoài logger println đã đọc |

Default NoOpLogger trong constructor không tự quyết định policy runtime. Việc chọn logger hiện nằm ở AppModule/AppConfig, không dựa vào R8 hoặc giả định chữ release tự xóa println.

## 3. Phân biệt hai trục cấu hình

- dev/prod là product flavor: có thể quyết định endpoint, feature/config và logging policy.
- debug/release là build type: quyết định khả năng debug, signing và các lựa chọn build.
- Chỉ kiểm BuildConfig.DEBUG sẽ bật log cả prodDebug; điều đó không đáp ứng chính sách mọi bản prod đều tắt log.
- Cùng URL vẫn có thể dùng config/logging/app ID khác nhau. App ID khác nhau tách app/local storage nhưng không tách dữ liệu backend hoặc biến mutation trên backend chung thành an toàn.
- Không tự xóa flavors hoặc tạo thêm deployment/database trong lượt này.
- Base còn hai sample contract JSONPlaceholder và Practice API. Không đổi mọi client về một URL bằng cách thay chuỗi, vì DTO/contract khác nhau.

Nguồn: [Android build variants](https://developer.android.com/build/build-variants).

## 4. Policy đã triển khai

Đã áp dụng **developerLoggingEnabled = environment.isDev && isDebug**: chỉ devDebug ghi developer logs, độc lập với URL backend.

| Variant | Trước sửa | Hiện tại |
| --- | --- | --- |
| devDebug | ConsoleLogger | ConsoleLogger với nội dung đã lọc |
| devRelease | ConsoleLogger | NoOpLogger |
| prodDebug | ConsoleLogger | NoOpLogger |
| prodRelease | ConsoleLogger | NoOpLogger |

Nếu sau này cần devRelease có log để kiểm nội bộ, có thể chọn policy environment.isDev. Ghi quyết định và kiểm thử rõ; không dùng DEBUG một mình khi yêu cầu prod luôn tắt log.

- AppConfig sở hữu quyết định môi trường/build type.
- AppModule.logger(config) chọn ConsoleLogger hoặc NoOpLogger từ policy.
- Composition tiếp tục nhận AppLogger; không đọc BuildConfig của library hoặc import app ngược vào composition.
- Domain/data/shared không phụ thuộc BuildConfig hay flavor Android.
- Không thêm logging framework/SDK mới nếu AppLogger hiện có đã đáp ứng.
- Chọn NoOpLogger tắt output của AppLogger, không chứng minh mọi SDK/Android OS đều im lặng trong Logcat.
- Crash/error reporting production nếu có nhu cầu là một hợp đồng riêng; không mặc định thêm Crashlytics hoặc giữ raw console errors ở prod.

## 5. Nội dung log và HTTP

- [x] Bỏ email/tên khỏi string interpolation trong AuthRepositoryImpl; dùng event names và metadata cho phép như operation/error code, không log request DTO/session.
- [x] Không log password, access/refresh token, Authorization/Cookie, auth request/response body hoặc dữ liệu người dùng.
- [x] Không chỉ redact metadata keys: email hiện nằm ngay trong message string, logger chung không biết cấu trúc để tự che chính xác.
- [x] Kiểm throwable.message trước khi đưa ra output: lỗi từ server/thư viện có thể mang dữ liệu ngoài dự kiến.
- [x] Giữ HTTP logging **tắt mặc định**; hiện tại app chưa có HTTP log để bật/tắt theo variant.
- [ ] Nếu bổ sung HTTP diagnostics, dùng opt-in cùng policy; ưu tiên method/path không chứa query nhạy cảm, status và duration, không bật BODY đại trà.
- [ ] Cùng một policy phải áp dụng cho public/protected clients; không để protected client kế thừa interceptor logging đang bật ngoài ý muốn.
- [x] Không dựa hoàn toàn vào R8 để quyết định có log; nếu dùng stripping sau này, kiểm chứng riêng và không xóa side effects ngoài logging.

Nguồn: [Android: log information disclosure](https://developer.android.com/privacy-and-security/risks/log-info-disclosure).

## 6. Các bước triển khai

### LOG-1 — Tắt logger theo policy

- [x] Thêm policy rõ ràng vào AppConfig hoặc helper thuần có input environment/isDebug.
- [x] AppModule cấp đúng ConsoleLogger/NoOpLogger; giữ chữ ký AppLogger cho các layer còn lại.
- [x] Không thay URLs, tạo database hoặc thay logic nghiệp vụ.

### LOG-2 — Loại thông tin cá nhân khỏi log

- [x] Sửa AuthRepositoryImpl log messages và rà call sites dùng AppLogger.
- [x] Dùng metadata có kiểm soát, không đưa credentials thật vào fixtures/output.
- [x] Quyết định HTTP diagnostics sau; không thêm interceptor chỉ để được gọi là base đầy đủ.

### LOG-3 — Kiểm chứng

- [x] Unit test đủ 4 tổ hợp dev/prod × debug/release, nhất là prodDebug không log.
- [x] Test sink/output: devDebug có event chẩn đoán; các variant bị tắt không phát output qua AppLogger.
- [x] Test auth success/failure bằng fake/MockWebServer và giá trị giả: captured logs không chứa email/name/password/token trong message, metadata hoặc throwable output.
- [x] Hilt graph tests chứng minh variant nhận logger theo policy; không chỉ test constructor default.
- [ ] Nếu thêm HTTP diagnostics, kiểm cả public/protected client và header/body/query không rò dữ liệu giả.
- [x] Chạy tests/boundaries và các build phù hợp. Theo ghi nhận DI trước, wrapper 9.4.1 + AGP 8.3.1 có giới hạn release/lint; ghi đúng runner thay vì đổi toolchain ngầm.
- [ ] Nếu khẳng định log thực tế trên thiết bị đã tắt, kiểm Logcat của APK đúng variant; tách AppLogger khỏi log của hệ thống/SDK.

## 7. Kết quả kiểm chứng 17/11/2026

| Kiểm tra | Kết quả | Runner |
| --- | --- | --- |
| Boundaries, shared/domain/data/composition tests | Qua; reports lần lượt shared 39, domain 39, data 57, composition 17 | Wrapper Gradle 9.4.1 |
| App devDebug/prodDebug tests và APK | Mỗi variant 92 tests, 0 failures/skipped; có graph logger test | Wrapper Gradle 9.4.1 |
| App devRelease/prodRelease tests và APK | Mỗi variant 92 tests, 0 failures/ignored; graph nhận NoOpLogger | Executable Gradle 8.11.1 có sẵn |
| App lintDevDebug/lintProdRelease | 0 errors, lần lượt 72/49 warnings | Executable Gradle 8.11.1 |

Có **244 case khác nhau** trong reports: 39 shared + 39 domain + 57 data + 17 composition + 92 app. Các lần chạy theo variant không làm tăng số case khác nhau. Lượt này thêm 10 case so với snapshot composition 234 case.

Các test mới:
- LoggingPolicyTest: 4 tổ hợp environment/debug, cùng URL; kiểm cả config, provider và output debug/info/warning/error.
- ConsoleLoggerTest: 2 case, giữ diagnostic events/metadata hợp lệ và không in exception message/cause/stack trace.
- RepositoryLoggingPrivacyTest: 3 case, toàn bộ 7 auth operations thành công/thất bại không truyền dữ liệu giả nhạy cảm tới logger; query sản phẩm vẫn gửi đến API local nhưng không dump vào log.
- AuthDiGraphTest: thêm 1 case kiểm logger và singleton scope từ graph Hilt thật; đã chạy ở cả 4 variants.

Wrapper/version catalog giữ nguyên. Release/lint dùng executable 8.11.1 để tránh lỗi SelfResolvingDependency giữa wrapper 9.4.1 và AGP 8.3.1 đã ghi ở review DI. Release artifacts được kiểm với -PALLOW_DEMO_PROD_API=true; không coi đây là xác nhận production endpoint/signing.

## 8. Giới hạn và phần để sau

Yêu cầu logging đã được triển khai và kiểm chứng ở mức source, output JVM, Hilt graph và build. Không suy ra toàn bộ Logcat im lặng: OS/SDK có thể ghi log riêng. Chưa chạy thiết bị/emulator, API live, profiling hoặc CI remote.

HTTP diagnostics vẫn tắt; các checkbox về việc bổ sung interceptor là backlog có điều kiện, không phải việc đang thiếu để hoàn thành policy này. Không thêm framework/SDK log mới.

ConsoleLogger không tự phân loại mọi message/metadata tùy ý. Các call site mới vẫn phải dùng nội dung an toàn; thay đổi này không hứa tự động che mọi dữ liệu cá nhân ở mọi integration tương lai.
