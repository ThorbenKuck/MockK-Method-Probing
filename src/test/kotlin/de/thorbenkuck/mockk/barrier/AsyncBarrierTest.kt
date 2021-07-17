package de.thorbenkuck.mockk.barrier

import de.thorbenkuck.mockk.TestSubject
import de.thorbenkuck.mockk.barrier
import io.mockk.spyk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.concurrent.thread

class AsyncBarrierTest {

    @Test
    fun `test that the barrier traverses after short, async delay`() {
        // Arrange
        val testSubject = spyk(TestSubject())
        val barrier = barrier { testSubject.passThrough(any()) }

        // Act
        thread {
            Thread.sleep(1000)
            testSubject.passThrough("Foo")
        }

        // Assert
        assertDoesNotThrow {
            barrier.tryToTraverse()
        }
    }
}
