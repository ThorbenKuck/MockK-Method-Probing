package de.thorbenkuck.mockk.barrier

import io.mockk.MockKMatcherScope
import io.mockk.every

fun <T : Any?> barrier(
    stubBlock: MockKMatcherScope.() -> T,
    validator: (t: T) -> Boolean = {true}
): MethodBarrier {
    val barrier = MethodBarrier()

    every(stubBlock) answers {
        try {
            val result = callOriginal()
            barrier.valid = validator(result)
            barrier.release()
            result
        } catch (e: Throwable) {
            barrier.releaseExceptionally(e)
            throw e
        }
    }

    return barrier
}