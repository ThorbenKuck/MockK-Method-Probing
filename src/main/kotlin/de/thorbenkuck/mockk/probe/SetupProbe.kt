package de.thorbenkuck.mockk.probe

import io.mockk.Call
import io.mockk.MockKAnswerScope
import io.mockk.MockKStubScope

fun <T : Any?, B> MockKStubScope<T, B>.setupProbe(probe: MethodProbe<T>, doCallOriginal: MockKAnswerScope<T, B>.(Call) -> T) {
    answers {
        probe.argumentsResultFuture.complete(args)
        val startMillis = System.currentTimeMillis()
        fun endExecution() {
            val endMillis = System.currentTimeMillis()
            val result = endMillis - startMillis
            probe.executionTimeFuture.complete(result)
        }

        try {
            val result = doCallOriginal(it)
            endExecution()

            probe.methodResultFuture.complete(result)

            result
        } catch (e: Throwable) {
            endExecution()
            probe.methodResultFuture.completeExceptionally(e)
            throw e
        }
    }
}
