package de.thorbenkuck.mockk.probe

import io.mockk.MockKException
import io.mockk.spyk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.AssertionError

class SyncProbeTest {

    @Test
    fun `test that the probe with a non mock throws the exception`() {
        val toProbe = Subject()

        assertThrows<MockKException> {
            probe { toProbe.passThrough(any()) }
        }
    }

    @Test
    fun `test that when not executed the test fail`() {
        // Arrange
        val toProbe = spyk(Subject())
        val probe = probe { toProbe.passThrough(any()) }

        // No Act

        // Assert
        assertThrows<AssertionError> { probe.getResult(timeoutInSeconds = 3) }
    }

    @Test
    fun `test with spyk in synchronized call`() {
        // Arrange
        val toProbe = spyk(Subject())
        val probe = probe { toProbe.passThrough(any()) }

        // Act
        toProbe.passThrough(null)

        // Assert
        val firstArgument = probe.getArgument<Any?>(0)
        val result = probe.getResult()
        Assertions.assertThat(firstArgument).isNull()
        Assertions.assertThat(result).isNull()
    }
}