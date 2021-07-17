package de.thorbenkuck.mockk.barrier

import org.assertj.core.api.Assertions.assertThat
import java.lang.IllegalStateException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * This class symbols a barrier to wait for a (successful) method invocation.
 *
 * A MethodBarrier can be constructed using [barrier] or [barrierFor]. These two functions will construct a
 * MethodBarrier bound to the provided method. For example like this:
 *
 * ```
 * // Arrange
 * val toTest = spyk(ToTest())
 * val barrier = barrier { toTest.example(any()) }
 *
 * // Act
 * ...
 *
 * // Assert
 * barrier.tryToTraverse()
 * ```
 *
 * It is an extension to a test, meaning that the method bound to a MethodBarrier is expected to be called if
 * [tryToTraverse] is called. If it is not called, or fails with an exception the test will fail.
 *
 * The main Use for this is, to test asynchronous methods, when for example testing asynchronous systems.
 * One prominent example for this might be an enterprise application with Kafka. If you want to test the whole systems
 * integration (including Kafka) this functionality can be used to wait for a certain condition without busy or active
 * waiting.
 *
 * By contract, when the spied method finishes by raising an exception, the test will fail. This behaviour can be
 * changed by passing `failOnException=false` to the [tryToTraverse] method.
 *
 * Since a barrier does not maintain values about the result or parameters of the spied method, to validate those you
 * will have to pass a validator to [barrier] or [barrierFor]. If you do this though, you might be interested to use
 * [de.thorbenkuck.mockk.probe.probe] or [de.thorbenkuck.mockk.probe.probing] instead and do those validation in the
 * assert block of your test.
 *
 * @see barrier
 * @see barrierFor
 */
class MethodBarrier(
    expectedInvocationCount: Int
) {

    init {
        if(expectedInvocationCount < 1) {
            throw IllegalStateException("At least 1 expected call has to be provided for the barrier to work")
        }
    }

    internal var valid: Boolean = true
    private val semaphore: Semaphore = Semaphore((-expectedInvocationCount) + 1)
    private var throwable: Throwable? = null
    private var onError: (throwable: Throwable) -> Unit = {}

    internal fun release() {
        semaphore.release()
    }

    internal fun releaseExceptionally(exception: Throwable) {
        throwable = exception
        release()
    }

    fun onError(consumer: (throwable: Throwable) -> Unit) {
        onError = consumer
    }

    fun raisedException(): Throwable? {
        return throwable
    }

    operator fun invoke(timeoutSeconds: Long = 10,
                        notCalledMessage: String = "The expected method has not been called within $timeoutSeconds seconds",
                        failOnException: Boolean = true) = tryToTraverse(timeoutSeconds, notCalledMessage, failOnException)

    fun tryToTraverse(
        timeoutSeconds: Long = 10,
        notCalledMessage: String = "The expected method has not been called within $timeoutSeconds seconds",
        failOnException: Boolean = true
    ) {
        assertThat(semaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS)).withFailMessage(notCalledMessage).isTrue
        assertThat(valid).withFailMessage("The expected result was not correct").isTrue
        if (throwable != null) {
            onError(throwable!!)
        }
        if (failOnException) {
            assertThat(throwable).withFailMessage("Exception raised in").isNull()
        }
    }
}
