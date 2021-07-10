package de.thorbenkuck.mockk.probe

import org.assertj.core.api.AbstractLongAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.assertj.core.api.ObjectAssert
import java.util.concurrent.CompletableFuture

/**
 * A MethodProbe is bound to two completable futures,
 */
class MethodProbe<T : Any?>(
    internal val methodResultFuture: CompletableFuture<T> = CompletableFuture(),
    internal val argumentsResultFuture: CompletableFuture<List<Any?>> = CompletableFuture(),
    internal val executionTimeFuture: CompletableFuture<Long> = CompletableFuture()
) {

    private val synchronizer = AsyncMethodProbeSynchronizer(methodResultFuture, argumentsResultFuture, executionTimeFuture)

    fun assertThatExecutionTimeMillis(
        timeoutInSeconds: Long = 10,
        timeoutErrorMessage: String = "Method has not finished within $timeoutInSeconds seconds"
    ): AbstractLongAssert<*> {
        val executionTime = synchronizer.getExecutionTime(timeoutInSeconds, timeoutErrorMessage)

        return assertThat(executionTime)
    }

    fun assertThatResult(
        timeoutInSeconds: Long = 10,
        timeoutErrorMessage: String = "Method has not finished within $timeoutInSeconds seconds"
    ): ObjectAssert<T> {
        return assertThat(synchronizer.getMethodResult(timeoutInSeconds, timeoutErrorMessage))
    }

    fun assertThatArguments(
        timeoutInSeconds: Long = 10,
        timeoutErrorMessage: String = "Method has not finished within $timeoutInSeconds seconds"
    ): ListAssert<Any?> {
        return assertThat(synchronizer.getArguments(timeoutInSeconds, timeoutErrorMessage))
    }

    fun waitTillCalled(
        timeoutInSeconds: Long = 10,
        timeoutErrorMessage: String = "Method has not been called within $timeoutInSeconds seconds"
    ) {
        synchronizer.getArguments(timeoutInSeconds, timeoutErrorMessage)
    }

    fun alreadyExecuted(): Boolean {
        return synchronizer.receivedMethodResult()
    }

    fun <S : Any?> getArgument(
        index: Int,
        timeoutInSeconds: Long = 10,
        errorMessage: String = "Method has not been called within $timeoutInSeconds seconds"
    ): Any? {
        val args = synchronizer.getArguments(timeoutInSeconds, errorMessage)
        assertThat(args.size).withFailMessage("No argument at index $index (max:${args.size})")
            .isGreaterThan(index)

        return args[index]
    }

    fun getResultNow(errorMessage: String = "Method has not been called") = getResult(0, errorMessage)

    fun getResult(
        timeoutInSeconds: Long = 10,
        errorMessage: String = "Method has not been called within $timeoutInSeconds seconds",
    ): T {
        return synchronizer.getMethodResult(timeoutInSeconds, errorMessage)
    }
}
