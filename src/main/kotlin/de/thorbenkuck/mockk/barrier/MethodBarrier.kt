package de.thorbenkuck.mockk.barrier

import org.assertj.core.api.Assertions.assertThat
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class MethodBarrier {

    internal var valid: Boolean = false
    private val semaphore: Semaphore = Semaphore(0)
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

    fun tryToTraverse(
        timeoutSeconds: Long = 10,
        notCalledMessage: String = "The expected method has not been called within $timeoutSeconds seconds",
        failOnException: Boolean = true
    ) {
        assertThat(semaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS)).withFailMessage(notCalledMessage).isTrue
        assertThat(valid).withFailMessage("The expected result was not correct").isTrue
        if(throwable != null) {
            onError(throwable!!)
        }
        if(failOnException) {
            assertThat(throwable).withFailMessage("Exception raised in").isNull()
        }
    }
}