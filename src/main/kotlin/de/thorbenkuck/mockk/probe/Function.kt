package de.thorbenkuck.mockk.probe

import io.mockk.MockKMatcherScope
import io.mockk.every

/**
 * Creates a MethodProbe on a provided stub block.
 *
 * When the provided stubBlock is called, the MethodProbe will be filled with all relevant information about the method,
 * allowing to validating it. The probe expects the stubBlock to be called, leading to the same behaviour as with
 * the barrier. If the stubBlock is never called in the tested code, the test fails.
 *
 * The constructed [MethodProbe] is bound to the provided  stub block, overriding any other potential
 * previously set stubs and also being overwritten by different blocks. So in this example:
 *
 * ```
 * val testClass = spyk(TestClass())
 * every { testClass.testMethod(any()) } return "Foo"
 * val methodProbe = probe { testClass.testMethod(any()) }
 * ```
 *
 * The probe will overwrite the previously defined `every` block. The same is true the other way around:
 *
 * ```
 * val testClass = spyk(TestClass())
 * val methodProbe = probe { testClass.testMethod(any()) }
 * every { testClass.testMethod(any()) } return "Foo"
 *
 * val methodResult = methodProbe.getResult()
 * ```
 *
 * In this example the probe will never be able to fetch the method result, leading to a failed test after the
 * defined timeout.
 *
 * @see probing
 * @see MethodProbe
 * @see de.thorbenkuck.mockk.barrier.barrierFor
 * @see de.thorbenkuck.mockk.barrier.barrier
 */
fun <T : Any?> probe(stubBlock: MockKMatcherScope.() -> T): MethodProbe<T> {
    val probe = MethodProbe<T>()

    every(stubBlock).setupProbe(probe) {
        callOriginal()
    }

    return probe
}

/**
 * This method is nearly the same as [probe], except that it provides a way for defining answers.
 *
 * You can define answers for the method probe to your liking. This is needed, if you want to mock the barrier
 * and still have access to when it is being called, even though this certainly is an edge case.
 *
 * <b>CoAnswers are currently not supported</b>
 *
 * If you construct a probe like this:
 *
 * ```
 * // Arrange
 * val testClass = mockk<TestClass>()
 * val output = "Foo
 * val methodProbe = probing { testClass.testMethod(any()) } returns output
 *
 * // Act
 * testClass.testMethod("Bar)
 *
 * // Assert
 * assertThat(methodProbe.getResult()).isEqualTo(output).isNotEqualTo(methodProbe.getParameter<String>(0))
 * ```
 *
 * The test is valid. The Probe will behave exactly the same as when calling [probe], just that the provided
 * stubBlock is extended with the provided answer scope.
 *
 * @see probe
 * @see MethodProbe
 * @see de.thorbenkuck.mockk.barrier.barrierFor
 * @see de.thorbenkuck.mockk.barrier.barrier
 */
fun <T : Any?> probing(stubBlock: MockKMatcherScope.() -> T): ProbeMockKStubScope<T, T> {
    return ProbeMockKStubScope(every(stubBlock))
}
