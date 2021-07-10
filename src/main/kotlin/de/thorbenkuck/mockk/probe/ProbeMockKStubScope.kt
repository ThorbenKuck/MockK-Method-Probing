package de.thorbenkuck.mockk.probe

import io.mockk.*

class ProbeMockKStubScope<T, B>(
    private val stubScope: MockKStubScope<T, B>,
    private val methodProbe: MethodProbe<T> = MethodProbe()
) {
    infix fun answers(answer: Answer<T>): MethodProbe<T> {
        stubScope.answers {
            methodProbe.argumentsResultFuture.complete(args)
            try {
                val result = answer.answer(it)
                methodProbe.methodResultFuture.complete(result)

                result
            } catch (e: Throwable) {
                methodProbe.methodResultFuture.completeExceptionally(e)
                throw e
            }
        }
        return methodProbe
    }

    infix fun returns(returnValue: T) = answers(ConstantAnswer(returnValue))

    infix fun returnsMany(values: List<T>) = answers(ManyAnswersAnswer(values.allConst()))

    fun returnsMany(vararg values: T) = returnsMany(values.toList())

    infix fun returnsArgument(n: Int) = this answers { invocation.args[n] as T }

    infix fun throws(ex: Throwable) = answers(ThrowingAnswer(ex))

    infix fun answers(answer: MockKAnswerScope<T, B>.(Call) -> T): MethodProbe<T> {
        stubScope.answers(answer)
        return methodProbe
    }

    infix fun coAnswers(answer: suspend MockKAnswerScope<T, B>.(Call) -> T): MethodProbe<T> {
        stubScope.coAnswers(answer)
        return methodProbe
    }
}

internal fun <T> List<T>.allConst() = this.map { ConstantAnswer(it) }