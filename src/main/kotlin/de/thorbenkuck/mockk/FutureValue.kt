package de.thorbenkuck.mockk

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class FutureValue<T : Any?>(
    private val future: CompletableFuture<T>
) {

    init {
        future.whenComplete { data, throwable ->
            set(data, throwable)
        }
    }

    @Volatile
    private var unsafeFinished: Boolean = false
    @Volatile
    private lateinit var data: Content
    @Volatile
    private lateinit var exception: ExceptionContent
    @Transient
    private val readWriteLock = ReentrantReadWriteLock(true)

    fun isSet(): Boolean {
        return readWriteLock.read {
            unsafeIsSet()
        }
    }

    fun hasData(): Boolean {
        return readWriteLock.read {
            this::data.isInitialized
        }
    }

    fun get(timeout: Long, timeUnit: TimeUnit): T {
        return if (isSet()) {
            readWriteLock.read { data.t }
        } else {
            readWriteLock.write {
                if (unsafeIsSet()) {
                    return data.t
                } else {
                    try {
                        val result = future.get(timeout, timeUnit)
                        set(result, null)
                        result
                    } catch (executionException: ExecutionException) {
                        unsafeFinished = true
                        exception = ExceptionContent(executionException.cause)
                        throw executionException
                    } catch (timeoutException: TimeoutException) {
                        unsafeFinished = true
                        exception = ExceptionContent(timeoutException)
                        throw timeoutException
                    }
                }
            }
        }
    }

    private fun set(t: T, throwable: Throwable? = null) {
        if (isSet()) {
            return
        } else {
            readWriteLock.write {
                if (unsafeIsSet()) {
                    return
                } else {
                    unsafeFinished = true
                    data = Content(t)
                    exception = ExceptionContent(throwable)
                }
            }
        }
    }

    private fun unsafeIsSet() = unsafeFinished

    inner class Content(val t: T)

    inner class ExceptionContent(val throwable: Throwable?)
}
