package com.example.base.shared

import com.example.base.shared.logging.ConsoleLogger
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class ConsoleLoggerTest {
    @Test
    fun consoleRetainsDeveloperEventsAndAllowedMetadata() {
        val output = captureOutput {
            ConsoleLogger().debug("operation-started", mapOf("attempt" to 2))
            ConsoleLogger().info("operation-finished")
        }
        assertContains(output, "operation-started")
        assertContains(output, "attempt=2")
        assertContains(output, "operation-finished")
    }

    @Test
    fun throwableMessagesCausesAndStackTracesAreNotPrinted() {
        val error = IllegalStateException(
            "fake-email@example.test fake-access-token",
            IllegalArgumentException("fake-password-in-cause")
        )
        val output = captureOutput {
            ConsoleLogger().warning("retry", error)
            ConsoleLogger().error("operation-failed", error)
        }
        assertContains(output, "IllegalStateException")
        assertContains(output, "operation-failed")
        listOf("fake-email@example.test", "fake-access-token", "fake-password-in-cause")
            .forEach { assertFalse(output.contains(it), "Private exception data reached console") }
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
