package de.thorbenkuck.mockk.barrier

import allConst
import io.mockk.*

/**
 * This class is taken from the official MockK repository and enhanced, to provide the MethodBarrier afterwards.
 *
 * @see <a href="https://github.com/mockk/mockk">Mock</a>
 */
class BarrierMockKStubScope<T, B>(
    private val stubScope: MockKStubScope<T, B>,
    private val validator: (result: T) -> Boolean,
) {

    infix fun answers(answer: Answer<T>): MethodBarrier {
        val barrier = MethodBarrier()

        stubScope.setupBarrier(barrier, validator) {
            answer.answer(it)
        }

        return barrier
    }

    infix fun returns(returnValue: T) = answers(ConstantAnswer(returnValue))

    infix fun returnsMany(values: List<T>) = answers(ManyAnswersAnswer(values.allConst()))

    fun returnsMany(vararg values: T) = returnsMany(values.toList())

    infix fun returnsArgument(n: Int) = this answers { invocation.args[n] as T }

    infix fun throws(ex: Throwable) = answers(ThrowingAnswer(ex))

    infix fun answers(answer: MockKAnswerScope<T, B>.(Call) -> T): MethodBarrier {
        val barrier = MethodBarrier()

        stubScope.setupBarrier(barrier, validator) {
            answer(it)
        }

        return barrier
    }
}
