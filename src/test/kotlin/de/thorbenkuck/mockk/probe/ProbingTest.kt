package de.thorbenkuck.mockk.probe

import de.thorbenkuck.mockk.TestSubject
import de.thorbenkuck.mockk.probing
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProbingTest {

    @Test
    fun `test that the probe works`() {
        // Arrange
        val toProbe = mockk<TestSubject>()
        val input = "Foo"
        val probing = probing { toProbe.passThrough(any()) } returns "Bar"

        // Act
        val result = toProbe.passThrough(input)

        // Assert
        val probedResult = probing.getResult()
        assertThat(result).isEqualTo(probedResult).isEqualTo("Bar").isNotEqualTo(input)
    }
}
