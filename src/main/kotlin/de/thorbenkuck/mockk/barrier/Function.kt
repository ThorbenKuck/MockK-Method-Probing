package de.thorbenkuck.mockk.barrier

import io.mockk.MockKMatcherScope
import io.mockk.every

/**
 * Creates a barrier on a provided method call.
 *
 * The constructed [MethodBarrier] is bound to the provided  stub block, overriding any other potential
 * previously set stubs and also being overwritten by different blocks. So in this example:
 *
 * ```
 * val testClass = spyk(TestClass())
 * every { testClass.testMethod(any()) } return "Foo"
 * val barrier = barrier { testClass.testMethod(any()) }
 * ```
 *
 * The barrier will overwrite the previously defined `every` block. The same is true the other way around:
 *
 * ```
 * val testClass = spyk(TestClass())
 * val barrier = barrier { testClass.testMethod(any()) }
 * every { testClass.testMethod(any()) } return "Foo"
 *
 * barrier.tryToTraverse()
 * ```
 *
 * In this example the barrier will never finish traverse, leading to a failed test after the defined timeout.
 *
 * @see barrierFor
 * @see MethodBarrier
 * @see de.thorbenkuck.mockk.probe.probe
 * @see de.thorbenkuck.mockk.probe.probing
 */
fun <T : Any?> barrier(
    validator: (t: T) -> Boolean = { true },
    stubBlock: MockKMatcherScope.() -> T
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

/**
 * This method is nearly the same as [barrier], except that it provides a way for defining answers.
 *
 * You can define answers for the method barriers to your liking. This is needed, if you want to mock the barrier
 * and still have access to when it is being called, even though this certainly is an edge case.
 *
 * <b>CoAnswers are currently not supported</b>
 *
 * If you construct a barrier like this:
 *
 * ```
 * val testClass = mockk<TestClass>()
 * val barrier = barrierFor { testClass.testMethod(any()) } returns "Foo"
 * ```
 *
 * The Barrier will behave exactly the same as when calling [barrier], just that the provided stubBlock is extended
 * with the provided answer scope.
 *
 * @see barrier
 * @see MethodBarrier
 * @see de.thorbenkuck.mockk.probe.probe
 * @see de.thorbenkuck.mockk.probe.probing
 */
fun <T : Any?> barrierFor(
    stubBlock: MockKMatcherScope.() -> T,
    validator: (t: T) -> Boolean = { true }
): BarrierMockKStubScope<T, T> {
    return BarrierMockKStubScope(every(stubBlock), validator)
}
