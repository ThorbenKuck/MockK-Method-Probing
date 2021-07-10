package de.thorbenkuck.mockk.probe

import de.thorbenkuck.mockk.TestSubject
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.AssertionError

class ProbeContract {

    @Test
    fun `verify that with specified mock before this mock is overwritten`() {
        // Arrange
        val toProbe = mockk<TestSubject>()
        every { toProbe.passThrough(eq("Foo")) } returns "Bar"
        val probe = probe { toProbe.passThrough(eq("Foo")) }

        // Act
        val actualResult = toProbe.passThrough("Foo")

        // Assert
        val result = probe.getResult()
        Assertions.assertThat(actualResult).isEqualTo(result)
        Assertions.assertThat(result).isEqualTo("Foo")
    }

    @Test
    fun `verify that with specified mock after, the probe is not invoked at all`() {
        // Arrange
        val toProbe = mockk<TestSubject>()
        val probe = probe { toProbe.passThrough(eq("Foo")) }
        every { toProbe.passThrough(eq("Foo")) } returns "Bar"

        // Act
        val result = toProbe.passThrough("Foo")

        // Assert
        Assertions.assertThat(result).isEqualTo("Bar")
        assertThrows<AssertionError> {
            probe.getResult(0)
        }
    }
}
