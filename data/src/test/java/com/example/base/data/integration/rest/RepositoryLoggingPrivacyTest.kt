package com.example.base.data.integration.rest

import com.example.base.data.api.practice.PracticeProductApi
import com.example.base.data.api.practice.auth.PracticeAuthApi
import com.example.base.data.api.practice.auth.TokenStorage
import com.example.base.data.repository.AuthRepositoryImpl
import com.example.base.data.repository.ProductRepositoryImpl
import com.example.base.domain.model.ProductFilter
import com.example.base.shared.logging.AppLogger
import com.example.base.shared.result.Result
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RepositoryLoggingPrivacyTest {
    private lateinit var server: MockWebServer
    private lateinit var retrofit: Retrofit
    private lateinit var logger: RecordingLogger

    @BeforeTest
    fun setUp() {
        server = MockWebServer().apply { start() }
        retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        logger = RecordingLogger()
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun authSuccessLogsDoNotContainProfileCredentialsOrTokens() = runTest {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                "/auth/login", "/auth/register" -> jsonResponse(
                    """{"data":{"user":$userJson,"tokens":$tokensJson}}"""
                )
                "/auth/refresh" -> jsonResponse("""{"data":{"tokens":$tokensJson}}""")
                "/auth/me" -> jsonResponse("""{"data":{"user":$userJson}}""")
                "/auth/logout", "/auth/logout-all", "/auth/change-password" ->
                    MockResponse().setResponseCode(204)
                else -> MockResponse().setResponseCode(404)
            }
        }

        val results = exerciseAuthOperations(authRepository())

        assertTrue(results.all { it is Result.Success<*> })
        assertEquals(7, server.requestCount)
        assertEquals(7, logger.entries.size)
        assertPrivateValuesAbsent()
    }

    @Test
    fun authFailureLogsDoNotForwardUntrustedExceptionPayloads() = runTest {
        val privatePayload = privateValues.joinToString(" ")
        repeat(7) {
            server.enqueue(
                jsonResponse("""{"error":{"code":"TEST_FAILURE","message":"$privatePayload"}}""")
                    // HttpException.message contains this reason phrase; never pass it to a log sink.
                    .setStatus("HTTP/1.1 500 $privatePayload")
            )
        }

        val results = exerciseAuthOperations(authRepository())

        assertTrue(results.all { it is Result.Failure })
        assertEquals(7, server.requestCount)
        assertEquals(7, logger.entries.count { it.startsWith("ERROR ") })
        assertPrivateValuesAbsent()
    }

    @Test
    fun productQueryReachesTheApiButIsNotDumpedIntoLogs() = runTest {
        server.enqueue(
            jsonResponse("""{"error":{"code":"VALIDATION_FAILED","message":"Invalid query"}}""")
                .setResponseCode(422)
        )
        val repository = ProductRepositoryImpl(
            api = retrofit.create(PracticeProductApi::class.java),
            logger = logger
        )

        val result = repository.getProducts(
            page = 1,
            pageSize = 10,
            filter = ProductFilter(query = email)
        )

        assertIs<Result.Failure>(result)
        assertEquals(email, server.takeRequest().requestUrl?.queryParameter("q"))
        assertTrue(logger.entries.any { it.contains("Loading products") })
        assertPrivateValuesAbsent()
    }

    private fun authRepository() = AuthRepositoryImpl(
        api = retrofit.create(PracticeAuthApi::class.java),
        tokenStorage = TokenStorage(),
        logger = logger
    )

    private suspend fun exerciseAuthOperations(repository: AuthRepositoryImpl): List<Result<*>> = listOf(
        repository.login(email, password),
        repository.register(email, password, name),
        repository.refreshToken(refreshToken),
        repository.getMe(),
        repository.logout(),
        repository.logoutAll(),
        repository.changePassword(password, newPassword)
    )

    private fun assertPrivateValuesAbsent() {
        val output = logger.entries.joinToString("\n")
        privateValues.forEach { value ->
            assertFalse(output.contains(value), "Private test data was passed to the logger")
        }
    }

    private fun jsonResponse(body: String) = MockResponse()
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private class RecordingLogger : AppLogger {
        val entries = mutableListOf<String>()

        override fun debug(message: String, metadata: Map<String, Any?>) {
            entries += "DEBUG $message $metadata"
        }

        override fun info(message: String, metadata: Map<String, Any?>) {
            entries += "INFO $message $metadata"
        }

        override fun warning(message: String, throwable: Throwable?) {
            entries += "WARN $message ${throwable?.stackTraceToString().orEmpty()}"
        }

        override fun error(message: String, throwable: Throwable?) {
            entries += "ERROR $message ${throwable?.stackTraceToString().orEmpty()}"
        }
    }

    companion object {
        private const val email = "private-person@example.test"
        private const val name = "Private Test Person"
        private const val password = "fake-login-password"
        private const val newPassword = "fake-new-password"
        private const val accessToken = "fake-private-access-token"
        private const val refreshToken = "fake-private-refresh-token"
        private val privateValues = listOf(email, name, password, newPassword, accessToken, refreshToken)
        private val userJson = """{"id":"test-user","email":"$email","name":"$name","role":"USER"}"""
        private val tokensJson = """
            {"accessToken":"$accessToken","refreshToken":"$refreshToken","tokenType":"Bearer",
             "expiresIn":3600,"refreshExpiresIn":604800}
        """.trimIndent()
    }
}
