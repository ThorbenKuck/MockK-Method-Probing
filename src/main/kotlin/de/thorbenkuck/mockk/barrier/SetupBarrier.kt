package de.thorbenkuck.mockk.barrier

import io.mockk.Call
import io.mockk.MockKAnswerScope
import io.mockk.MockKStubScope

/**
 * This method sets up a barrier for a specific StubScope. This is used both in the [BarrierMockKStubScope]
 */
fun <T : Any?, B> MockKStubScope<T, B>.setupBarrier(
    barrier: MethodBarrier,
    validator: (methodResult: T) -> Boolean,
    doCallOriginal: MockKAnswerScope<T, B>.(Call) -> T
) {
    answers {
        try {
            val result = doCallOriginal(it)
            barrier.valid = validator(result)
            barrier.release()
            result
        } catch (e: Throwable) {
            barrier.releaseExceptionally(e)
            throw e
        }
    }
}
