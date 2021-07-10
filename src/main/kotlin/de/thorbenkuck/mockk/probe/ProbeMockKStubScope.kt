package de.thorbenkuck.mockk.probe

import allConst
import io.mockk.*

/**
 * This class is taken from the official MockK repository and enhanced, to provide the MethodProbe afterwards.
 *
 * @see <a href="https://github.com/mockk/mockk">Mock</a>
 */
class ProbeMockKStubScope<T : Any?, B : Any?>(
    private val stubScope: MockKStubScope<T, B>
) {

    infix fun answers(answer: Answer<T>): MethodProbe<T> {
        val methodProbe: MethodProbe<T> = MethodProbe()

        stubScope.setupProbe(methodProbe) {
            answer.answer(it)
        }

        return methodProbe
    }

    infix fun returns(returnValue: T) = answers(ConstantAnswer(returnValue))

    infix fun returnsMany(values: List<T>) = answers(ManyAnswersAnswer(values.allConst()))

    fun returnsMany(vararg values: T) = returnsMany(values.toList())

    infix fun returnsArgument(n: Int) = this answers { invocation.args[n] as T }

    infix fun throws(ex: Throwable) = answers(ThrowingAnswer(ex))

    infix fun answers(answer: MockKAnswerScope<T, B>.(Call) -> T): MethodProbe<T> {
        val methodProbe: MethodProbe<T> = MethodProbe()

        stubScope.setupProbe(methodProbe) {
            answer(it)
        }

        return methodProbe
    }
}
