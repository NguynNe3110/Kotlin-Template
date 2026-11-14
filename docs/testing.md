# Testing and verification

[Back to README](../README.md) · [Architecture](architecture.md)

## Toolchain

Use JDK **21**, Android SDK **34**, and the included Gradle wrapper.
Java/Kotlin compilation targets JVM 17. Robolectric tests currently select simulated Android API 36;
Robolectric retrieves its Android runtime artifact independently of the project's compile SDK.

The first run requires dependency downloads. No real authentication backend or API credentials are needed.
MockWebServer tests exercise networking locally.

## Commands

```bash
./gradlew verifyModuleBoundaries
./gradlew :domain:test :data:test :shared:test :app:testDebugUnitTest
./gradlew :app:assembleDebug :app:lintDebug
```

On Windows, replace `./gradlew` with `.\gradlew.bat`.
For a targeted regression test:

```bash
./gradlew :app:testDebugUnitTest --tests com.example.base.session.DataStoreSessionFailureTest
```

Gradle can reuse UP-TO-DATE results. To deliberately re-execute the selected tasks and their prerequisites,
add `--rerun-tasks`. This is useful when validating an environment change or collecting a fresh verification result.

## Test coverage

| Area | Examples protected by tests |
| --- | --- |
| Domain use cases | Thrown failures, cancellation propagation, errors during Flow construction |
| Network boundary | Request query parameters, DTO conversion, HTTP error envelopes, empty response bodies, malformed JSON |
| In-memory data | Concurrent additions and toggles preserve data |
| Retry | Retryable/non-retryable failures, exponential backoff, cancellation |
| ViewModel operations | Retained error effects, manual read retry, write replay disabled, overlapping loading counts |
| Session persistence | Replacing optional credential fields, expiration restoration, clearing expiration after save |
| Session recovery | Initial read error followed by Retry; late read errors unblock save, clear, and expire |
| Todo state | Draft restoration, failed writes preserve text, successful writes clear submitted text, newer drafts survive |
| Todo observation | Committed writes remain successful when subsequent list observation fails; list retry does not repeat a write |
| Navigation | IDs round-trip with spaces, `+`, `/`, percent signs, query/fragment characters, and Unicode |

## Local verification snapshot

The following describes the revision checked on **15 November 2026**, using JDK 21:

| Check | Result |
| --- | --- |
| Module dependency boundaries | Passed |
| Debug APK build | Passed |
| JVM and Robolectric test suite | **35 passed**, 0 failed |
| Android lint | **0 errors**, **72 warnings** |
| Real device / emulator validation | Not performed |
| Instrumented UI test suite | Not implemented |
| Runtime profiling / benchmarks | Not performed |

Lint warnings include older dependency/SDK declarations, a missing application icon, and a Compose lint API
compatibility issue. The incompatible Compose checks are skipped by lint, so a successful lint task does not
mean those checks ran. Review toolchain compatibility before claiming comprehensive lint coverage.

These results are a local snapshot. Re-run checks after changing code, dependencies, or build configuration.
The repository does not currently include a CI workflow or a published code-coverage report.

## Reports

| Output | Location |
| --- | --- |
| Domain tests | `domain/build/reports/tests/test/index.html` |
| Data tests | `data/build/reports/tests/test/index.html` |
| Shared tests | `shared/build/reports/tests/test/index.html` |
| App tests | `app/build/reports/tests/testDebugUnitTest/index.html` |
| Lint | `app/build/reports/lint-results-debug.html` |
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |

Build outputs are ignored by Git. A fresh clone will not contain them until you run the corresponding task.

## Writing tests

Inject coroutine test dispatchers for deterministic scheduling. Prefer behavior checks at the boundary where a
failure matters: a saved draft, a completed write, a waiting session operation, or a navigation argument.
Use MockWebServer for transport behavior and Robolectric when Android framework behavior is required on the JVM.

Robolectric does not establish real-device rendering, navigation gestures, memory use, or performance.
Add device tests and profiling for those claims when adapting the starter to a product.
