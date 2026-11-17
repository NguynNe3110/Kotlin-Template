package com.example.base.config

import com.example.base.di.AppModule
import com.example.base.shared.logging.NoOpLogger
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class LoggingPolicyTest(
    private val environment: AppEnvironment,
    private val debugBuild: Boolean,
    private val expectedLogging: Boolean
) {
    @Test
    fun policyAndInjectedProviderControlOutputAtEveryLevel() {
        // All variants deliberately use the same backend.
        val config = AppConfig(environment, "https://shared.example.test/", debugBuild)
        assertEquals(expectedLogging, config.developerLoggingEnabled)
        val logger = AppModule.logger(config)
        val output = captureOutput {
            logger.debug("diagnostic-debug", mapOf("count" to 1))
            logger.info("diagnostic-info")
            logger.warning("diagnostic-warning", IllegalStateException("fake-private-payload"))
            logger.error("diagnostic-error", IllegalStateException("fake-private-payload"))
        }

        if (expectedLogging) {
            listOf("diagnostic-debug", "diagnostic-info", "diagnostic-warning", "diagnostic-error")
                .forEach { assertContains(output, it) }
        } else {
            assertSame(NoOpLogger, logger)
            assertEquals("", output)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, debug={1}, logs={2}")
        fun variants(): List<Array<Any>> = listOf(
            arrayOf(AppEnvironment.DEV, true, true),
            arrayOf(AppEnvironment.DEV, false, false),
            arrayOf(AppEnvironment.PROD, true, false),
            arrayOf(AppEnvironment.PROD, false, false)
        )
    }
}

private fun captureOutput(action: () -> Unit): String = synchronized(System::class.java) {
    val previous = System.out
    val bytes = ByteArrayOutputStream()
    PrintStream(bytes, true, "UTF-8").use { output ->
        try {
            System.setOut(output)
            action()
        } finally {
            System.setOut(previous)
        }
    }
    bytes.toString("UTF-8")
}
