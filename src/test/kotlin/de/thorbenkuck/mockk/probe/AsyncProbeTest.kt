package de.thorbenkuck.mockk.probe

import de.thorbenkuck.mockk.TestSubject
import io.mockk.mockk
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class AsyncProbeTest {

    @Test
    fun `test with spyk`() {
        // Arrange
        val toProbe = spyk(TestSubject())
        val probe = probe { toProbe.passThrough(any()) }

        // Act
        thread {
            Thread.sleep(1000)
            toProbe.passThrough(null)
        }

        // Assert
        val firstArgument = probe.getArgument<Any?>(0)
        val result = probe.getResult()
        assertThat(firstArgument).isNull()
        assertThat(result).isNull()
    }

    @Test
    fun `test with relaxed mock`() {
        // Arrange
        val toProbe = mockk<TestSubject>(relaxed = true)
        val probe = probe { toProbe.passThrough(eq("Foo")) }

        // Act
        thread {
            toProbe.passThrough(null)
            Thread.sleep(1000)
            toProbe.passThrough("Bar")
            Thread.sleep(1000)
            toProbe.passThrough("Foo")
        }

        // Assert
        val firstArgument = probe.getArgument<String?>(0)
        val result = probe.getResult()
        assertThat(firstArgument).isEqualTo("Foo")
        assertThat(result).isEqualTo("Foo")
    }

    @Test
    fun `test long time taking spy execution`() {
        // Arrange
        val toProbe = mockk<TestSubject>(relaxed = true)
        val probe = probe { toProbe.timeoutThenPassThrough(eq("Foo")) }

        // Act
        thread {
            toProbe.timeoutThenPassThrough("Foo", 1000)
        }

        // Assert
        probe.assertThatExecutionTimeMillis()
            .isGreaterThanOrEqualTo(1000)
    }
}
