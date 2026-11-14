package com.example.base.shared.error

/**
 * Lớp ngoại lệ chuẩn hóa cho toàn bộ ứng dụng.
 * Giúp quy đổi mọi ngoại lệ (Network, Server, Unknown) về một kiểu dữ liệu duy nhất dễ quản lý ở UI layer.
 */
interface AppFailure {
    val retryable: Boolean
    val code: String?
}

sealed class AppException(
    override val message: String,
    override val cause: Throwable? = null,
    override val retryable: Boolean = false,
    override val code: String? = null
) : Exception(message, cause), AppFailure {

    data class NetworkError(
        override val message: String = "Lỗi kết nối mạng. Vui lòng kiểm tra lại Internet.",
        override val cause: Throwable? = null,
        val kind: NetworkErrorKind = NetworkErrorKind.Connection
    ) : AppException(message, cause, retryable = kind.retryable)

    data class ServerError(
        val httpCode: Int? = null,
        override val message: String = "Lỗi máy chủ ($httpCode). Vui lòng thử lại sau.",
        override val cause: Throwable? = null,
        val canRetry: Boolean = httpCode == null || httpCode >= 500
    ) : AppException(message, cause, retryable = canRetry, code = httpCode?.toString())

    data class ValidationError(
        override val message: String,
        override val cause: Throwable? = null,
        override val code: String? = null
    ) : AppException(message, cause, code = code)

    data class ParseError(
        override val message: String = "Dữ liệu nhận được không hợp lệ.",
        override val cause: Throwable? = null
    ) : AppException(message, cause)

    data class RefreshTokenFailed(
        override val message: String = "Phiên đăng nhập đã hết hạn.",
        override val cause: Throwable? = null
    ) : AppException(message, cause, code = "refresh_token_failed")

    data class UnknownError(
        override val message: String = "Đã xảy ra lỗi không xác định. Vui lòng thử lại.",
        override val cause: Throwable? = null
    ) : AppException(message, cause)

    companion object {
        fun from(throwable: Throwable): AppException {
            return when (throwable) {
                is AppException -> throwable
                is java.net.SocketTimeoutException -> NetworkError(
                    kind = NetworkErrorKind.Timeout,
                    cause = throwable
                )
                is java.io.IOException -> NetworkError(cause = throwable)
                else -> UnknownError(message = throwable.localizedMessage ?: "Đã xảy ra lỗi.", cause = throwable)
            }
        }
    }
}

enum class NetworkErrorKind(val retryable: Boolean) {
    NoInternet(true),
    Timeout(true),
    Connection(true),
    Cancelled(false)
}
