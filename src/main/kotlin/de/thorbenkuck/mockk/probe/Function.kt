package de.thorbenkuck.mockk.probe

import io.mockk.MockKMatcherScope
import io.mockk.MockKStubScope
import io.mockk.every

fun <T: Any?>probing(stubBlock: MockKMatcherScope.() -> T): ProbeMockKStubScope<T, T> {
    return ProbeMockKStubScope(every(stubBlock))
}

fun <T : Any?> probe(stubBlock: MockKMatcherScope.() -> T): MethodProbe<T> {
    val probe = MethodProbe<T>()

    every(stubBlock) answers {
        probe.argumentsResultFuture.complete(args)
        try {
            val result = callOriginal()
            probe.methodResultFuture.complete(result)

            result
        } catch (e: Throwable) {
            probe.methodResultFuture.completeExceptionally(e)
            throw e
        }
    }

    return probe
}